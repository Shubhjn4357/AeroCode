package com.aerotech.aerocode.core.compiler

import com.aerotech.aerocode.core.filesystem.FileSystem
import com.aerotech.aerocode.domain.editor.CompletionItem
import com.aerotech.aerocode.domain.editor.CompletionKind
import com.aerotech.aerocode.domain.visual.ComponentNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

data class IndexedSymbol(
    val name: String,
    val kind: CompletionKind,
    val detail: String,
    val insertText: String,
    val filePath: String,
    val packageName: String,
    val autoImport: String? = null,
    val isComposable: Boolean = false,
    val bodyNodes: List<ComponentNode> = emptyList()
)

class RepoSymbolIndexer {

    // Map of filePath -> List<IndexedSymbol>
    private val fileSymbols = ConcurrentHashMap<String, List<IndexedSymbol>>()

    // Cache of custom composable AST definitions: composableName -> List<ComponentNode>
    private val composableAstCache = ConcurrentHashMap<String, List<ComponentNode>>()

    /**
     * Recursively indexes all Kotlin and Java source files in the project.
     */
    suspend fun indexProject(projectPath: String, fileSystem: FileSystem) = withContext(Dispatchers.IO) {
        val files = mutableListOf<String>()
        collectSourceFiles(projectPath, fileSystem, files)

        for (filePath in files) {
            try {
                if (fileSystem.exists(filePath)) {
                    val content = fileSystem.read(filePath)
                    indexFileContent(filePath, content)
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun collectSourceFiles(dirPath: String, fileSystem: FileSystem, result: MutableList<String>) {
        val entries = fileSystem.list(dirPath)
        for (entry in entries) {
            if (entry.isDirectory) {
                // Ignore build and hidden directories
                if (!entry.name.startsWith(".") && entry.name != "build" && entry.name != "bin") {
                    collectSourceFiles(entry.path, fileSystem, result)
                }
            } else if (entry.name.endsWith(".kt") || entry.name.endsWith(".java")) {
                result.add(entry.path)
            }
        }
    }

    /**
     * Re-indexes a single file in memory immediately (e.g. on editor code update).
     */
    fun updateFile(filePath: String, content: String) {
        indexFileContent(filePath, content)
    }

    private fun indexFileContent(filePath: String, content: String) {
        val lines = content.lines()
        val symbols = mutableListOf<IndexedSymbol>()

        var packageName = ""
        val packageMatch = Regex("""^package\s+([a-zA-Z0-9_.]+)""").find(content)
        if (packageMatch != null) {
            packageName = packageMatch.groupValues[1]
        }

        // 1. Scan for @Composable functions
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            val isComposable = line.contains("@Composable") || (i > 0 && lines[i - 1].trim().contains("@Composable"))

            if (isComposable && line.contains("fun ")) {
                val funMatch = Regex("""fun\s+([A-Za-z0-9_]+)\s*(?:<.*?>)?\s*\((.*?)\)""").find(line)
                if (funMatch != null) {
                    val funName = funMatch.groupValues[1]
                    val params = funMatch.groupValues[2].trim()

                    // Extract AST body for preview
                    val openBraceLine = if (line.contains("{")) i else {
                        var next = i + 1
                        while (next < lines.size && !lines[next].contains("{")) next++
                        if (next < lines.size) next else i
                    }
                    val bodyAst = extractComposableAst(lines, openBraceLine)
                    if (bodyAst.isNotEmpty()) {
                        composableAstCache[funName] = bodyAst
                    }

                    val insertText = if (params.isBlank()) "$funName()" else "$funName()"
                    symbols.add(
                        IndexedSymbol(
                            name = funName,
                            kind = CompletionKind.COMPOSABLE,
                            detail = if (params.isBlank()) "@Composable fun $funName()" else "@Composable fun $funName($params)",
                            insertText = insertText,
                            filePath = filePath,
                            packageName = packageName,
                            autoImport = if (packageName.isNotBlank()) "$packageName.$funName" else null,
                            isComposable = true,
                            bodyNodes = bodyAst
                        )
                    )
                }
            } else if (line.contains("fun ") && !line.startsWith("//")) {
                // Standard non-composable function
                val funMatch = Regex("""fun\s+([A-Za-z0-9_]+)\s*(?:<.*?>)?\s*\((.*?)\)""").find(line)
                if (funMatch != null) {
                    val funName = funMatch.groupValues[1]
                    val params = funMatch.groupValues[2].trim()
                    symbols.add(
                        IndexedSymbol(
                            name = funName,
                            kind = CompletionKind.FUNCTION,
                            detail = "fun $funName($params)",
                            insertText = "$funName()",
                            filePath = filePath,
                            packageName = packageName,
                            autoImport = if (packageName.isNotBlank()) "$packageName.$funName" else null
                        )
                    )
                }
            }

            // 2. Scan for classes & data classes
            if ((line.contains("class ") || line.contains("interface ")) && !line.startsWith("//")) {
                val classMatch = Regex("""(?:data\s+class|class|interface|enum\s+class)\s+([A-Za-z0-9_]+)""").find(line)
                if (classMatch != null) {
                    val className = classMatch.groupValues[1]
                    val isData = line.contains("data class")
                    val isInterface = line.contains("interface")
                    symbols.add(
                        IndexedSymbol(
                            name = className,
                            kind = if (isInterface) CompletionKind.INTERFACE else CompletionKind.CLASS,
                            detail = if (isData) "data class $className" else if (isInterface) "interface $className" else "class $className",
                            insertText = className,
                            filePath = filePath,
                            packageName = packageName,
                            autoImport = if (packageName.isNotBlank()) "$packageName.$className" else null
                        )
                    )
                }
            }

            // 3. Scan for top-level / property vals & vars
            if ((line.startsWith("val ") || line.startsWith("var ") || line.startsWith("const val ")) && !line.startsWith("//")) {
                val propMatch = Regex("""(?:const\s+val|val|var)\s+([A-Za-z0-9_]+)""").find(line)
                if (propMatch != null) {
                    val propName = propMatch.groupValues[1]
                    symbols.add(
                        IndexedSymbol(
                            name = propName,
                            kind = CompletionKind.PROPERTY,
                            detail = "val $propName",
                            insertText = propName,
                            filePath = filePath,
                            packageName = packageName
                        )
                    )
                }
            }

            i++
        }

        fileSymbols[filePath] = symbols
    }

    private fun extractComposableAst(lines: List<String>, openLineIndex: Int): List<ComponentNode> {
        return try {
            val block = mutableListOf<String>()
            var count = 0
            var foundFirst = false
            for (j in openLineIndex until lines.size) {
                val l = lines[j]
                for (ch in l) {
                    if (ch == '{') { count++; foundFirst = true }
                    else if (ch == '}') count--
                }
                block.add(l)
                if (foundFirst && count <= 0) break
            }
            if (block.isNotEmpty()) {
                val dummyParent = ComponentNode(id = java.util.UUID.randomUUID().toString(), type = "Group")
                ComposeASTParser.parseDirectBlock(block, dummyParent, emptyMap())
                dummyParent.children
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Query completions for a given prefix. Prioritizes custom composables and indexed repo symbols.
     */
    fun getCompletions(prefix: String, currentFilePath: String): List<CompletionItem> {
        if (prefix.isBlank()) return emptyList()

        val results = mutableListOf<CompletionItem>()
        val seen = mutableSetOf<String>()

        fileSymbols.values.flatten().forEach { sym ->
            if (sym.name.contains(prefix, ignoreCase = true) && seen.add(sym.name)) {
                results.add(
                    CompletionItem(
                        label = sym.name,
                        detail = sym.detail,
                        insertText = sym.insertText,
                        kind = sym.kind,
                        autoImport = sym.autoImport
                    )
                )
            }
        }

        return results.sortedWith(
            compareByDescending<CompletionItem> { it.label.startsWith(prefix, ignoreCase = true) }
                .thenByDescending { it.kind == CompletionKind.COMPOSABLE }
                .thenBy { it.label }
        )
    }

    /**
     * Map of all custom composables across the project for AST inlining.
     */
    fun getAllComposableAsts(): Map<String, List<ComponentNode>> {
        return composableAstCache
    }

    fun clear() {
        fileSymbols.clear()
        composableAstCache.clear()
    }
}
