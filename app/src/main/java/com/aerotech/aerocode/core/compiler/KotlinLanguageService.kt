package com.aerotech.aerocode.core.compiler

import com.aerotech.aerocode.domain.editor.CompletionItem
import com.aerotech.aerocode.domain.editor.CompletionKind
import com.aerotech.aerocode.domain.editor.Diagnostic
import com.aerotech.aerocode.domain.editor.DiagnosticSeverity
import com.aerotech.aerocode.domain.editor.Document
import com.aerotech.aerocode.domain.editor.ImportCandidate
import com.aerotech.aerocode.domain.editor.ImportResolver
import com.aerotech.aerocode.domain.editor.ImportSuggestion
import com.aerotech.aerocode.domain.editor.LanguageService
import com.aerotech.aerocode.domain.editor.Position
import com.aerotech.aerocode.domain.editor.ProjectContext
import com.aerotech.aerocode.domain.editor.SourceRange
import com.aerotech.aerocode.domain.editor.Symbol

class ComposeImportResolver : ImportResolver {
    private val candidateRegistry = mapOf(
        "Text" to listOf(
            ImportCandidate("androidx.compose.material3", "Text", isPreferred = true),
            ImportCandidate("android.widget", "TextView", isPreferred = false)
        ),
        "Button" to listOf(
            ImportCandidate("androidx.compose.material3", "Button", isPreferred = true),
            ImportCandidate("android.widget", "Button", isPreferred = false)
        ),
        "Card" to listOf(
            ImportCandidate("androidx.compose.material3", "Card", isPreferred = true)
        ),
        "Surface" to listOf(
            ImportCandidate("androidx.compose.material3", "Surface", isPreferred = true)
        ),
        "Scaffold" to listOf(
            ImportCandidate("androidx.compose.material3", "Scaffold", isPreferred = true)
        ),
        "TopAppBar" to listOf(
            ImportCandidate("androidx.compose.material3", "TopAppBar", isPreferred = true)
        ),
        "Column" to listOf(
            ImportCandidate("androidx.compose.foundation.layout", "Column", isPreferred = true)
        ),
        "Row" to listOf(
            ImportCandidate("androidx.compose.foundation.layout", "Row", isPreferred = true)
        ),
        "Box" to listOf(
            ImportCandidate("androidx.compose.foundation.layout", "Box", isPreferred = true)
        ),
        "Spacer" to listOf(
            ImportCandidate("androidx.compose.foundation.layout", "Spacer", isPreferred = true)
        ),
        "Modifier" to listOf(
            ImportCandidate("androidx.compose.ui", "Modifier", isPreferred = true)
        ),
        "dp" to listOf(
            ImportCandidate("androidx.compose.ui.unit", "dp", isPreferred = true)
        ),
        "sp" to listOf(
            ImportCandidate("androidx.compose.ui.unit", "sp", isPreferred = true)
        ),
        "remember" to listOf(
            ImportCandidate("androidx.compose.runtime", "remember", isPreferred = true)
        ),
        "mutableStateOf" to listOf(
            ImportCandidate("androidx.compose.runtime", "mutableStateOf", isPreferred = true)
        ),
        "Composable" to listOf(
            ImportCandidate("androidx.compose.runtime", "Composable", isPreferred = true)
        )
    )

    override suspend fun resolve(symbol: String, context: ProjectContext): List<ImportCandidate> {
        return candidateRegistry[symbol] ?: emptyList()
    }
}

class KotlinLanguageService(
    private val importResolver: ImportResolver = ComposeImportResolver(),
    var repoIndexer: RepoSymbolIndexer? = null
) : LanguageService {

    private val baseCatalog = listOf(
        // Compose Components
        CompletionItem("Text", "Composable text display", "Text(\"Hello\")", CompletionKind.COMPOSABLE, "androidx.compose.material3.Text"),
        CompletionItem("TextField", "Editable text input field", "TextField(value = text, onValueChange = { text = it })", CompletionKind.COMPOSABLE, "androidx.compose.material3.TextField"),
        CompletionItem("OutlinedTextField", "Outlined text field", "OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(\"Label\") })", CompletionKind.COMPOSABLE, "androidx.compose.material3.OutlinedTextField"),
        CompletionItem("Button", "Material 3 standard button", "Button(onClick = { /* action */ }) {\n    Text(\"Button\")\n}", CompletionKind.COMPOSABLE, "androidx.compose.material3.Button"),
        CompletionItem("OutlinedButton", "Outlined button component", "OutlinedButton(onClick = { /* action */ }) {\n    Text(\"Action\")\n}", CompletionKind.COMPOSABLE, "androidx.compose.material3.OutlinedButton"),
        CompletionItem("IconButton", "Clickable icon button", "IconButton(onClick = { /* action */ }) {\n    Icon(Icons.Default.Add, contentDescription = null)\n}", CompletionKind.COMPOSABLE, "androidx.compose.material3.IconButton"),
        CompletionItem("Column", "Vertical layout container", "Column(\n    modifier = Modifier.fillMaxWidth()\n) {\n    \n}", CompletionKind.COMPOSABLE, "androidx.compose.foundation.layout.Column"),
        CompletionItem("Row", "Horizontal layout container", "Row(\n    modifier = Modifier.fillMaxWidth(),\n    verticalAlignment = Alignment.CenterVertically\n) {\n    \n}", CompletionKind.COMPOSABLE, "androidx.compose.foundation.layout.Row"),
        CompletionItem("Box", "Layering stack container", "Box(\n    modifier = Modifier.fillMaxSize()\n) {\n    \n}", CompletionKind.COMPOSABLE, "androidx.compose.foundation.layout.Box"),
        CompletionItem("Card", "Surface card container", "Card(\n    modifier = Modifier.fillMaxWidth()\n) {\n    \n}", CompletionKind.COMPOSABLE, "androidx.compose.material3.Card"),
        CompletionItem("Surface", "Material container surface", "Surface(\n    modifier = Modifier.fillMaxSize()\n) {\n    \n}", CompletionKind.COMPOSABLE, "androidx.compose.material3.Surface"),
        CompletionItem("Spacer", "Flexible space separator", "Spacer(modifier = Modifier.height(16.dp))", CompletionKind.COMPOSABLE, "androidx.compose.foundation.layout.Spacer"),
        CompletionItem("LazyColumn", "Scrollable vertical list", "LazyColumn {\n    items(items) { item ->\n        Text(item)\n    }\n}", CompletionKind.COMPOSABLE, "androidx.compose.foundation.lazy.LazyColumn"),
        CompletionItem("Scaffold", "Screen structure scaffold", "Scaffold(\n    topBar = { /* TopBar */ }\n) { padding ->\n    Box(modifier = Modifier.padding(padding)) {\n        \n    }\n}", CompletionKind.COMPOSABLE, "androidx.compose.material3.Scaffold"),
        CompletionItem("Switch", "Toggle switch", "Switch(checked = isChecked, onCheckedChange = { isChecked = it })", CompletionKind.COMPOSABLE, "androidx.compose.material3.Switch"),
        CompletionItem("Slider", "Continuous slider", "Slider(value = sliderValue, onValueChange = { sliderValue = it })", CompletionKind.COMPOSABLE, "androidx.compose.material3.Slider"),
        CompletionItem("Checkbox", "Selection checkbox", "Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })", CompletionKind.COMPOSABLE, "androidx.compose.material3.Checkbox"),

        // Modifiers
        CompletionItem("Modifier.fillMaxWidth()", "Fill parent width", "Modifier.fillMaxWidth()", CompletionKind.MODIFIER, "androidx.compose.foundation.layout.fillMaxWidth"),
        CompletionItem("Modifier.fillMaxSize()", "Fill entire parent size", "Modifier.fillMaxSize()", CompletionKind.MODIFIER, "androidx.compose.foundation.layout.fillMaxSize"),
        CompletionItem("Modifier.padding()", "Apply margin or padding", "Modifier.padding(16.dp)", CompletionKind.MODIFIER, "androidx.compose.foundation.layout.padding"),
        CompletionItem("Modifier.height()", "Explicit height", "Modifier.height(48.dp)", CompletionKind.MODIFIER, "androidx.compose.foundation.layout.height"),
        CompletionItem("Modifier.width()", "Explicit width", "Modifier.width(48.dp)", CompletionKind.MODIFIER, "androidx.compose.foundation.layout.width"),
        CompletionItem("Modifier.clickable {}", "Make clickable", "Modifier.clickable { /* action */ }", CompletionKind.MODIFIER, "androidx.compose.foundation.clickable"),
        CompletionItem("Modifier.background()", "Draw background", "Modifier.background(MaterialTheme.colorScheme.surface)", CompletionKind.MODIFIER, "androidx.compose.foundation.background"),
        CompletionItem("Modifier.statusBarsPadding()", "Safe top inset padding", "Modifier.statusBarsPadding()", CompletionKind.MODIFIER, "androidx.compose.foundation.layout.statusBarsPadding"),
        CompletionItem("Modifier.navigationBarsPadding()", "Safe bottom inset padding", "Modifier.navigationBarsPadding()", CompletionKind.MODIFIER, "androidx.compose.foundation.layout.navigationBarsPadding"),

        // Compose Runtime
        CompletionItem("remember { mutableStateOf() }", "Mutable state holder", "remember { mutableStateOf(\"\") }", CompletionKind.FUNCTION, "androidx.compose.runtime.remember"),
        CompletionItem("rememberCoroutineScope()", "Coroutine scope for UI", "rememberCoroutineScope()", CompletionKind.FUNCTION, "androidx.compose.runtime.rememberCoroutineScope"),
        CompletionItem("LaunchedEffect(Unit)", "Side effect coroutine launcher", "LaunchedEffect(Unit) {\n    \n}", CompletionKind.FUNCTION, "androidx.compose.runtime.LaunchedEffect"),
        CompletionItem("@Composable", "Marks composable function", "@Composable\nfun CustomView() {\n    \n}", CompletionKind.KEYWORD, "androidx.compose.runtime.Composable"),
        CompletionItem("Icon", "Material symbol icon", "Icon(Icons.Default.Add, contentDescription = null)", CompletionKind.COMPOSABLE, "androidx.compose.material3.Icon"),
        CompletionItem("TopAppBar", "Screen header app bar", "TopAppBar(title = { Text(\"Title\") })", CompletionKind.COMPOSABLE, "androidx.compose.material3.TopAppBar"),
        CompletionItem("HorizontalDivider", "Horizontal divider line", "HorizontalDivider()", CompletionKind.COMPOSABLE, "androidx.compose.material3.HorizontalDivider"),
        CompletionItem("CircularProgressIndicator", "Loading progress spinner", "CircularProgressIndicator()", CompletionKind.COMPOSABLE, "androidx.compose.material3.CircularProgressIndicator"),

        // Kotlin Keywords & Snippets
        CompletionItem("fun", "Function definition", "fun myFunc() {\n    \n}", CompletionKind.KEYWORD),
        CompletionItem("val", "Immutable variable", "val name = \"\"", CompletionKind.KEYWORD),
        CompletionItem("var", "Mutable variable", "var count = 0", CompletionKind.KEYWORD),
        CompletionItem("class", "Class definition", "class MyClass {\n    \n}", CompletionKind.KEYWORD),
        CompletionItem("data class", "Data class definition", "data class User(val id: String, val name: String)", CompletionKind.KEYWORD),
        CompletionItem("when", "Pattern matching expression", "when (state) {\n    else -> {}\n}", CompletionKind.KEYWORD),
        CompletionItem("println()", "Standard output stream print", "println(\"\")", CompletionKind.FUNCTION),
        CompletionItem("ViewModel", "AndroidX ViewModel template", "class MainViewModel : ViewModel() {\n    private val _uiState = MutableStateFlow(\"\")\n    val uiState = _uiState.asStateFlow()\n}", CompletionKind.CLASS, "androidx.lifecycle.ViewModel")
    )

    private val xmlCatalog = listOf(
        CompletionItem("<LinearLayout>", "Vertical/Horizontal Linear layout", "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\"\n    android:orientation=\"vertical\">\n\n</LinearLayout>", CompletionKind.SNIPPET),
        CompletionItem("<TextView>", "Text display widget", "<TextView\n    android:layout_width=\"wrap_content\"\n    android:layout_height=\"wrap_content\"\n    android:text=\"Hello World\" />", CompletionKind.SNIPPET),
        CompletionItem("<Button>", "Interactive button widget", "<Button\n    android:layout_width=\"wrap_content\"\n    android:layout_height=\"wrap_content\"\n    android:text=\"Click Me\" />", CompletionKind.SNIPPET),
        CompletionItem("<ImageView>", "Image visual widget", "<ImageView\n    android:layout_width=\"wrap_content\"\n    android:layout_height=\"wrap_content\"\n    android:contentDescription=\"@null\" />", CompletionKind.SNIPPET),
        CompletionItem("<FrameLayout>", "FrameLayout container", "<FrameLayout\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\">\n\n</FrameLayout>", CompletionKind.SNIPPET),
        CompletionItem("<ScrollView>", "Scrollable container", "<ScrollView\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\">\n\n</ScrollView>", CompletionKind.SNIPPET),
        CompletionItem("<androidx.compose.ui.platform.ComposeView>", "Compose container inside XML", "<androidx.compose.ui.platform.ComposeView\n    android:id=\"@+id/compose_view\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\" />", CompletionKind.SNIPPET),
        CompletionItem("<resources>", "Android resource root", "<resources>\n    <string name=\"app_name\">My Application</string>\n</resources>", CompletionKind.SNIPPET),
        CompletionItem("<string>", "String resource item", "<string name=\"title\">Title</string>", CompletionKind.SNIPPET),
        CompletionItem("<color>", "Color resource item", "<color name=\"primary\">#3574F0</color>", CompletionKind.SNIPPET),

        // XML Attributes
        CompletionItem("android:layout_width=\"match_parent\"", "Match parent width", "android:layout_width=\"match_parent\"", CompletionKind.PROPERTY),
        CompletionItem("android:layout_width=\"wrap_content\"", "Wrap content width", "android:layout_width=\"wrap_content\"", CompletionKind.PROPERTY),
        CompletionItem("android:layout_height=\"wrap_content\"", "Wrap content height", "android:layout_height=\"wrap_content\"", CompletionKind.PROPERTY),
        CompletionItem("android:layout_height=\"match_parent\"", "Match parent height", "android:layout_height=\"match_parent\"", CompletionKind.PROPERTY),
        CompletionItem("android:id=\"@+id/\"", "View ID attribute", "android:id=\"@+id/\"", CompletionKind.PROPERTY),
        CompletionItem("android:orientation=\"vertical\"", "Vertical orientation", "android:orientation=\"vertical\"", CompletionKind.PROPERTY),
        CompletionItem("android:orientation=\"horizontal\"", "Horizontal orientation", "android:orientation=\"horizontal\"", CompletionKind.PROPERTY),
        CompletionItem("android:text=\"\"", "Text string value", "android:text=\"\"", CompletionKind.PROPERTY),
        CompletionItem("android:padding=\"16dp\"", "Padding attribute", "android:padding=\"16dp\"", CompletionKind.PROPERTY),
        CompletionItem("android:layout_margin=\"16dp\"", "Margin attribute", "android:layout_margin=\"16dp\"", CompletionKind.PROPERTY),
        CompletionItem("xmlns:android=\"http://schemas.android.com/apk/res/android\"", "Android XML namespace", "xmlns:android=\"http://schemas.android.com/apk/res/android\"", CompletionKind.PROPERTY)
    )

    override suspend fun completions(document: Document, position: Position): List<CompletionItem> {
        val lines = document.content.lines()
        if (position.line > lines.size || position.line <= 0) return emptyList()

        val line = lines[position.line - 1]
        val col = position.column.coerceIn(0, line.length)
        val textBeforeCursor = line.take(col)

        // Find the identifier prefix currently being typed (support Kotlin tokens, XML tags '<' and attributes 'android:')
        val prefix = textBeforeCursor.takeLastWhile {
            it.isLetterOrDigit() || it == '.' || it == '_' || it == '@' || it == '<' || it == ':' || it == '/' || it == '-'
        }

        // If prefix is empty or whitespace preceded without prefix, return empty to hide suggestion popup
        if (prefix.isBlank() || prefix.length < 1) {
            return emptyList()
        }

        val isXml = document.path.endsWith(".xml", ignoreCase = true)
        val targetCatalog = if (isXml) xmlCatalog else baseCatalog

        // Dynamically query full repository symbols (custom composables, classes, functions across all files)
        val repoSymbols = repoIndexer?.getCompletions(prefix, document.path) ?: emptyList()

        // Dynamically extract user-defined identifiers from the current document
        val dynamicDocItems = extractDocumentSymbols(document.content, prefix)

        val allCandidates = (repoSymbols + dynamicDocItems + targetCatalog)

        // Filter and sort by relevance: custom composables and repo symbols first, then prefix matches
        return allCandidates
            .filter { item ->
                item.label.contains(prefix, ignoreCase = true) ||
                item.insertText.contains(prefix, ignoreCase = true)
            }
            .distinctBy { it.label }
            .sortedWith(
                compareByDescending<CompletionItem> { it.label.startsWith(prefix, ignoreCase = true) }
                    .thenByDescending { it.kind == CompletionKind.COMPOSABLE }
                    .thenBy { it.label }
            )
            .take(12)
    }

    private fun extractDocumentSymbols(content: String, currentPrefix: String): List<CompletionItem> {
        val list = mutableListOf<CompletionItem>()
        val identifierRegex = Regex("""\b([a-zA-Z_][a-zA-Z0-9_]{2,})\b""")
        val matches = identifierRegex.findAll(content)

        matches.map { it.value }.distinct().take(40).forEach { word ->
            if (word.startsWith(currentPrefix, ignoreCase = true) && word != currentPrefix) {
                list.add(
                    CompletionItem(
                        label = word,
                        detail = "Identifier in document",
                        insertText = word,
                        kind = CompletionKind.VARIABLE
                    )
                )
            }
        }
        return list
    }

    override suspend fun diagnostics(document: Document): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()
        val lines = document.content.lines()

        var openBraces = 0
        var openParens = 0
        var openBrackets = 0

        lines.forEachIndexed { index, line ->
            val lineNum = index + 1
            for (col in line.indices) {
                when (line[col]) {
                    '{' -> openBraces++
                    '}' -> {
                        openBraces--
                        if (openBraces < 0) {
                            diagnostics.add(
                                Diagnostic(
                                    message = "Unexpected closing brace '}'",
                                    range = SourceRange(lineNum, col, lineNum, col + 1),
                                    severity = DiagnosticSeverity.ERROR
                                )
                            )
                            openBraces = 0
                        }
                    }
                    '(' -> openParens++
                    ')' -> {
                        openParens--
                        if (openParens < 0) {
                            diagnostics.add(
                                Diagnostic(
                                    message = "Unexpected closing parenthesis ')'",
                                    range = SourceRange(lineNum, col, lineNum, col + 1),
                                    severity = DiagnosticSeverity.ERROR
                                )
                            )
                            openParens = 0
                        }
                    }
                    '[' -> openBrackets++
                    ']' -> {
                        openBrackets--
                        if (openBrackets < 0) {
                            diagnostics.add(
                                Diagnostic(
                                    message = "Unexpected closing bracket ']'",
                                    range = SourceRange(lineNum, col, lineNum, col + 1),
                                    severity = DiagnosticSeverity.ERROR
                                )
                            )
                            openBrackets = 0
                        }
                    }
                }
            }

            val quoteCount = line.count { it == '"' }
            if (quoteCount % 2 != 0 && !line.contains("\"\"\"")) {
                diagnostics.add(
                    Diagnostic(
                        message = "Unclosed string literal",
                        range = SourceRange(lineNum, 0, lineNum, line.length),
                        severity = DiagnosticSeverity.ERROR
                    )
                )
            }
        }

        if (openBraces > 0) {
            diagnostics.add(
                Diagnostic(
                    message = "Missing closing brace '}'",
                    range = SourceRange(lines.size, 0, lines.size, 1),
                    severity = DiagnosticSeverity.ERROR
                )
            )
        }
        if (openParens > 0) {
            diagnostics.add(
                Diagnostic(
                    message = "Missing closing parenthesis ')'",
                    range = SourceRange(lines.size, 0, lines.size, 1),
                    severity = DiagnosticSeverity.ERROR
                )
            )
        }

        return diagnostics
    }

    override suspend fun imports(document: Document): List<ImportSuggestion> {
        val suggestions = mutableListOf<ImportSuggestion>()
        val content = document.content
        val symbols = listOf("Text", "Button", "Card", "Column", "Row", "Box", "Spacer", "remember", "mutableStateOf")

        symbols.forEach { symbol ->
            if (content.contains("\\b$symbol\\b".toRegex()) && !content.contains("import .+$symbol".toRegex())) {
                val candidates = importResolver.resolve(symbol, ProjectContext("", document.path, ""))
                val preferred = candidates.firstOrNull { it.isPreferred } ?: candidates.firstOrNull()
                if (preferred != null) {
                    suggestions.add(
                        ImportSuggestion(
                            symbolName = symbol,
                            qualifiedName = preferred.fullyQualifiedName
                        )
                    )
                }
            }
        }
        return suggestions
    }

    override suspend fun symbols(document: Document): List<Symbol> {
        val symbols = mutableListOf<Symbol>()
        val lines = document.content.lines()

        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("fun ") || trimmed.startsWith("@Composable fun ")) {
                val name = trimmed.substringAfter("fun ").substringBefore('(').trim()
                symbols.add(Symbol(name, "Function", index + 1))
            } else if (trimmed.startsWith("class ")) {
                val name = trimmed.substringAfter("class ").substringBefore(' ').substringBefore(':').trim()
                symbols.add(Symbol(name, "Class", index + 1))
            }
        }
        return symbols
    }
}
