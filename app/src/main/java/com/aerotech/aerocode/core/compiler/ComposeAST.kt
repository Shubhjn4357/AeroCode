package com.aerotech.aerocode.core.compiler

import com.aerotech.aerocode.domain.editor.SourceRange
import com.aerotech.aerocode.domain.visual.ComponentNode
import com.aerotech.aerocode.domain.visual.ModifierModel
import java.util.UUID

object ComposeASTParser {

    /**
     * Parses Kotlin source code, extracts @Composable functions, inlines custom composable calls,
     * and produces a clean, real Compose component tree.
     */
    fun parse(
        source: String,
        externalDefs: Map<String, List<ComponentNode>> = emptyMap()
    ): ComponentNode {
        val lines = source.lines()
        val root = ComponentNode(
            id = "root",
            type = "Root",
            label = "App Root",
            sourceRange = SourceRange(1, 0, lines.size.coerceAtLeast(1), 0)
        )

        // 1. First Pass: Collect all @Composable function definitions
        val localDefs = collectComposableFunctions(lines)
        val allDefs = externalDefs + localDefs

        // 2. Identify the main Composable entrypoint
        val mainEntry = findMainEntrypoint(lines, allDefs)

        if (mainEntry != null && allDefs.containsKey(mainEntry)) {
            val entryNodes = allDefs[mainEntry] ?: emptyList()
            entryNodes.forEach { root.children.add(it.deepCopy()) }
        } else {
            // Check for setContent { ... }
            var setContentStart = -1
            for (i in lines.indices) {
                if (lines[i].contains("setContent")) {
                    setContentStart = i
                    break
                }
            }

            if (setContentStart != -1) {
                val block = extractBraceBlock(lines, setContentStart)
                parseBlock(block, 0, root, allDefs)
            } else if (allDefs.isNotEmpty()) {
                // Use first defined composable
                val firstNodes = allDefs.values.firstOrNull() ?: emptyList()
                firstNodes.forEach { root.children.add(it.deepCopy()) }
            } else {
                parseBlock(lines, 0, root, allDefs)
            }
        }

        // Expand any custom composable calls (like ProductCard, Greeting, CustomItem, etc.)
        expandCustomComposables(root, allDefs)

        // Fallback default container if empty
        if (root.children.isEmpty()) {
            val defaultColumn = ComponentNode(
                id = UUID.randomUUID().toString(),
                type = "Column",
                label = "Column",
                modifier = ModifierModel(fillMaxWidth = true, fillMaxHeight = true, paddingAll = 16),
                sourceRange = SourceRange(1, 0, lines.size.coerceAtLeast(1), 0)
            )
            val defaultText = ComponentNode(
                id = UUID.randomUUID().toString(),
                type = "Text",
                label = "Text",
                properties = mutableMapOf("text" to "Welcome to AeroCode"),
                parentId = defaultColumn.id
            )
            val defaultButton = ComponentNode(
                id = UUID.randomUUID().toString(),
                type = "Button",
                label = "Button",
                properties = mutableMapOf("label" to "Get Started"),
                parentId = defaultColumn.id
            )
            defaultColumn.children.add(defaultText)
            defaultColumn.children.add(defaultButton)
            root.children.add(defaultColumn)
        }

        return root
    }

    private fun collectComposableFunctions(lines: List<String>): Map<String, List<ComponentNode>> {
        val defs = mutableMapOf<String, List<ComponentNode>>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            val isComposable = line.contains("@Composable") || (i > 0 && lines[i - 1].trim().contains("@Composable"))
            if (isComposable && line.contains("fun ")) {
                val funName = "fun\\s+([A-Za-z0-9_]+)".toRegex().find(line)?.groupValues?.get(1)
                if (funName != null) {
                    val openBraceLine = if (line.contains("{")) i else {
                        var next = i + 1
                        while (next < lines.size && !lines[next].contains("{")) next++
                        if (next < lines.size) next else i
                    }
                    val blockLines = extractBraceBlock(lines, openBraceLine)
                    val dummyParent = ComponentNode(id = UUID.randomUUID().toString(), type = "Group")
                    parseBlock(blockLines, 0, dummyParent, emptyMap())
                    defs[funName] = dummyParent.children
                }
            }
            i++
        }
        return defs
    }

    private fun findMainEntrypoint(lines: List<String>, defs: Map<String, List<ComponentNode>>): String? {
        // Priority 1: Function with @Preview
        for (i in lines.indices) {
            if (lines[i].contains("@Preview")) {
                var j = i + 1
                while (j < lines.size && j <= i + 3) {
                    val match = "fun\\s+([A-Za-z0-9_]+)".toRegex().find(lines[j])
                    if (match != null) {
                        val name = match.groupValues[1]
                        if (defs.containsKey(name)) return name
                    }
                    j++
                }
            }
        }

        // Priority 2: Standard main names: App, MainScreen, HomeScreen, *Screen
        val preferred = listOf("App", "MainScreen", "HomeScreen", "StoreScreen", "CounterApp")
        preferred.forEach { if (defs.containsKey(it)) return it }

        for (name in defs.keys) {
            if (name.endsWith("Screen") || name.endsWith("Content") || name.endsWith("View")) {
                return name
            }
        }

        return defs.keys.firstOrNull()
    }

    private fun extractBraceBlock(lines: List<String>, openLineIndex: Int): List<String> {
        var count = 0
        var foundFirst = false
        val block = mutableListOf<String>()

        for (i in openLineIndex until lines.size) {
            val line = lines[i]
            for (char in line) {
                if (char == '{') {
                    count++
                    foundFirst = true
                } else if (char == '}') {
                    count--
                }
            }
            block.add(line)
            if (foundFirst && count <= 0) break
        }
        return block
    }

    fun parseDirectBlock(
        lines: List<String>,
        parent: ComponentNode,
        functionDefs: Map<String, List<ComponentNode>>
    ) {
        parseBlock(lines, 0, parent, functionDefs)
    }

    private fun parseBlock(
        lines: List<String>,
        startLine: Int,
        parent: ComponentNode,
        functionDefs: Map<String, List<ComponentNode>>
    ) {
        var i = startLine
        while (i < lines.size) {
            val rawLine = lines[i]
            val line = rawLine.trim()

            // Skip boilerplate
            if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("import ") ||
                line.startsWith("package ") || line.startsWith("@Preview") || line.startsWith("@Composable") ||
                line.startsWith("fun ")
            ) {
                i++
                continue
            }

            // Extract call expression
            val callMatch = extractCallExpression(line)
            if (callMatch != null) {
                val (componentType, rawArgs) = callMatch
                val node = createNodeFromCall(componentType, rawArgs, line, i + 1)
                node.parentId = parent.id

                // Check for trailing lambda
                if (line.endsWith("{") || (i + 1 < lines.size && lines[i + 1].trim() == "{")) {
                    val openLineIndex = if (line.endsWith("{")) i else i + 1
                    val endBlockIndex = findMatchingBrace(lines, openLineIndex)
                    val bodyStart = openLineIndex + 1

                    if (bodyStart <= endBlockIndex) {
                        parseBlock(lines.subList(0, endBlockIndex), bodyStart, node, functionDefs)
                    }

                    node.sourceRange = SourceRange(
                        startLine = i + 1,
                        startCol = 0,
                        endLine = endBlockIndex + 1,
                        endCol = lines.getOrNull(endBlockIndex)?.length ?: 0
                    )
                    parent.children.add(node)
                    i = endBlockIndex + 1
                    continue
                } else {
                    node.sourceRange = SourceRange(i + 1, 0, i + 1, rawLine.length)
                    parent.children.add(node)
                }
            }
            i++
        }
    }

    private fun extractCallExpression(line: String): Pair<String, String>? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("//")) return null

        // Support standard calls: Column(...), Text("Hello"), Spacer(modifier = ...)
        val regex = "^([A-Z][a-zA-Z0-9_]*)\\s*(\\((.*)\\))?.*".toRegex()
        val match = regex.find(trimmed) ?: return null
        val type = match.groupValues[1]
        val args = match.groupValues.getOrNull(3) ?: ""
        return Pair(type, args)
    }

    private fun createNodeFromCall(type: String, args: String, fullLine: String, lineNumber: Int): ComponentNode {
        val props = mutableMapOf<String, String>()
        var modifier = ModifierModel()

        when (type) {
            "Text" -> {
                val textMatch = "\"([^\"]*)\"".toRegex().find(args.ifEmpty { fullLine })
                props["text"] = textMatch?.groupValues?.get(1) ?: "Text"
                if (args.contains("fontSize")) {
                    val fs = "fontSize\\s*=\\s*([0-9]+)".toRegex().find(args)?.groupValues?.get(1)
                    if (fs != null) props["fontSize"] = "${fs}sp"
                }
            }

            "Button", "ElevatedButton", "FilledTonalButton", "OutlinedButton", "TextButton" -> {
                val labelMatch = "\"([^\"]*)\"".toRegex().find(args.ifEmpty { fullLine })
                props["label"] = labelMatch?.groupValues?.get(1) ?: "Button"
                props["enabled"] = (!args.contains("enabled = false")).toString()
            }

            "IconButton", "FilledIconButton" -> {
                val iconMatch = "Icons\\.(?:Default|AutoMirrored\\.Filled|Outlined)\\.([A-Za-z0-9_]+)".toRegex().find(fullLine)
                props["icon"] = iconMatch?.groupValues?.get(1) ?: "Star"
            }

            "TextField", "OutlinedTextField" -> {
                val valueMatch = "value\\s*=\\s*\"([^\"]*)\"".toRegex().find(args) ?: "\"([^\"]*)\"".toRegex().find(args)
                props["value"] = valueMatch?.groupValues?.get(1) ?: ""
                val labelMatch = "Text\\(\"([^\"]*)\"\\)".toRegex().find(fullLine)
                props["label"] = labelMatch?.groupValues?.get(1) ?: "Input field"
            }

            "Switch", "Checkbox", "TriStateCheckbox" -> {
                props["checked"] = (!args.contains("checked = false")).toString()
            }

            "Slider" -> {
                val valMatch = "value\\s*=\\s*([0-9.]+)[fF]?".toRegex().find(args)
                props["value"] = valMatch?.groupValues?.get(1) ?: "0.5"
            }

            "Spacer" -> {
                val hMatch = "height\\((\\d+)\\.dp\\)".toRegex().find(fullLine)
                val wMatch = "width\\((\\d+)\\.dp\\)".toRegex().find(fullLine)
                val szMatch = "size\\((\\d+)\\.dp\\)".toRegex().find(fullLine)
                val wtMatch = "weight\\(([0-9.]+)[fF]?\\)".toRegex().find(fullLine)

                if (hMatch != null) props["height"] = hMatch.groupValues[1]
                if (wMatch != null) props["width"] = wMatch.groupValues[1]
                if (szMatch != null) props["size"] = szMatch.groupValues[1]
                if (wtMatch != null) props["weight"] = wtMatch.groupValues[1]
            }

            "Icon" -> {
                val iconMatch = "Icons\\.(?:Default|AutoMirrored\\.Filled|Outlined)\\.([A-Za-z0-9_]+)".toRegex().find(fullLine)
                props["icon"] = iconMatch?.groupValues?.get(1) ?: "Star"
                val descMatch = "\"([^\"]*)\"".toRegex().find(args)
                props["contentDescription"] = descMatch?.groupValues?.get(1) ?: "Icon"
            }

            "HorizontalDivider", "Divider", "VerticalDivider" -> {
                val thickMatch = "(\\d+)\\.dp".toRegex().find(args)
                props["thickness"] = thickMatch?.groupValues?.get(1) ?: "1"
            }

            "CircularProgressIndicator", "LinearProgressIndicator" -> {
                val progMatch = "([0-9.]+)[fF]?".toRegex().find(args)
                if (progMatch != null) props["progress"] = progMatch.groupValues[1]
            }

            "FilterChip", "AssistChip", "SuggestionChip", "InputChip", "Badge" -> {
                val labelMatch = "\"([^\"]*)\"".toRegex().find(args.ifEmpty { fullLine })
                props["label"] = labelMatch?.groupValues?.get(1) ?: "Chip"
            }

            "TopAppBar", "CenterAlignedTopAppBar", "MediumTopAppBar", "LargeTopAppBar" -> {
                val titleMatch = "\"([^\"]*)\"".toRegex().find(fullLine)
                props["title"] = titleMatch?.groupValues?.get(1) ?: "App Header"
            }

            "FloatingActionButton", "ExtendedFloatingActionButton" -> {
                val textMatch = "\"([^\"]*)\"".toRegex().find(fullLine)
                if (textMatch != null) props["label"] = textMatch.groupValues[1]
            }

            "ProductCard" -> {
                val titleMatch = "title\\s*=\\s*\"([^\"]*)\"".toRegex().find(args)
                val priceMatch = "price\\s*=\\s*\"([^\"]*)\"".toRegex().find(args)
                val categoryMatch = "category\\s*=\\s*\"([^\"]*)\"".toRegex().find(args)
                props["title"] = titleMatch?.groupValues?.get(1) ?: "Product Item"
                props["price"] = priceMatch?.groupValues?.get(1) ?: "$0.00"
                props["category"] = categoryMatch?.groupValues?.get(1) ?: "Item"
            }

            else -> {
                // Generic argument extractor
                val titleMatch = "title\\s*=\\s*\"([^\"]*)\"".toRegex().find(args)
                val nameMatch = "name\\s*=\\s*\"([^\"]*)\"".toRegex().find(args)
                val textMatch = "\"([^\"]*)\"".toRegex().find(args)

                if (titleMatch != null) props["title"] = titleMatch.groupValues[1]
                else if (nameMatch != null) props["name"] = nameMatch.groupValues[1]
                else if (textMatch != null) props["text"] = textMatch.groupValues[1]
            }
        }

        // Modifiers extraction
        if (fullLine.contains("fillMaxWidth()")) modifier = modifier.copy(fillMaxWidth = true)
        if (fullLine.contains("fillMaxSize()")) modifier = modifier.copy(fillMaxWidth = true, fillMaxHeight = true)
        val padMatch = "padding\\((\\d+)\\.dp\\)".toRegex().find(fullLine)
        if (padMatch != null) {
            modifier = modifier.copy(paddingAll = padMatch.groupValues[1].toIntOrNull() ?: 16)
        }
        val heightMatch = "height\\((\\d+)\\.dp\\)".toRegex().find(fullLine)
        if (heightMatch != null) {
            modifier = modifier.copy(height = heightMatch.groupValues[1].toIntOrNull() ?: 0)
        }
        val widthMatch = "width\\((\\d+)\\.dp\\)".toRegex().find(fullLine)
        if (widthMatch != null) {
            modifier = modifier.copy(width = widthMatch.groupValues[1].toIntOrNull() ?: 0)
        }
        val sizeMatch = "size\\((\\d+)\\.dp\\)".toRegex().find(fullLine)
        if (sizeMatch != null) {
            val sz = sizeMatch.groupValues[1].toIntOrNull() ?: 0
            modifier = modifier.copy(height = sz, width = sz, size = sz)
        }
        val weightMatch = "weight\\(([0-9.]+)[fF]?\\)".toRegex().find(fullLine)
        if (weightMatch != null) {
            modifier = modifier.copy(weight = weightMatch.groupValues[1].toFloatOrNull() ?: 0f)
        }

        val displayLabel = when {
            props["text"] != null -> props["text"]!!
            props["title"] != null -> props["title"]!!
            props["label"] != null -> props["label"]!!
            props["name"] != null -> props["name"]!!
            else -> type
        }

        return ComponentNode(
            id = UUID.randomUUID().toString(),
            type = type,
            label = "$type: $displayLabel",
            properties = props,
            modifier = modifier,
            sourceRange = SourceRange(lineNumber, 0, lineNumber, fullLine.length)
        )
    }

    private fun expandCustomComposables(node: ComponentNode, defs: Map<String, List<ComponentNode>>) {
        val expandedChildren = mutableListOf<ComponentNode>()
        node.children.forEach { child ->
            if (defs.containsKey(child.type) && child.children.isEmpty()) {
                val definedChildren = defs[child.type] ?: emptyList()
                val container = child.copy(children = definedChildren.map { it.deepCopy() }.toMutableList())
                expandCustomComposables(container, defs)
                expandedChildren.add(container)
            } else {
                expandCustomComposables(child, defs)
                expandedChildren.add(child)
            }
        }
        node.children.clear()
        node.children.addAll(expandedChildren)
    }

    private fun findMatchingBrace(lines: List<String>, openLineIndex: Int): Int {
        var count = 0
        for (i in openLineIndex until lines.size) {
            for (char in lines[i]) {
                if (char == '{') count++
                else if (char == '}') {
                    count--
                    if (count == 0) return i
                }
            }
        }
        return lines.size - 1
    }
}

private fun ComponentNode.deepCopy(): ComponentNode {
    return ComponentNode(
        id = UUID.randomUUID().toString(),
        type = this.type,
        label = this.label,
        properties = this.properties.toMutableMap(),
        modifier = this.modifier.copy(),
        children = this.children.map { it.deepCopy() }.toMutableList(),
        sourceRange = this.sourceRange,
        parentId = this.parentId
    )
}
