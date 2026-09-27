package com.aerotech.aerocode.feature.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.domain.editor.CompletionItem
import com.aerotech.aerocode.domain.editor.Diagnostic
import com.aerotech.aerocode.domain.editor.DiagnosticSeverity
import com.aerotech.aerocode.domain.editor.HistoryManager
import com.aerotech.aerocode.ui.theme.AeroAmber
import com.aerotech.aerocode.ui.theme.AeroCyan
import com.aerotech.aerocode.ui.theme.AeroRose
import com.aerotech.aerocode.ui.theme.AeroTheme
import com.aerotech.aerocode.ui.theme.SyntaxAnnotation
import com.aerotech.aerocode.ui.theme.SyntaxComment
import com.aerotech.aerocode.ui.theme.SyntaxKeyword
import com.aerotech.aerocode.ui.theme.SyntaxNumber
import com.aerotech.aerocode.ui.theme.SyntaxString
import com.aerotech.aerocode.ui.theme.SyntaxType
import com.aerotech.aerocode.ui.theme.SyntaxXmlAttribute
import com.aerotech.aerocode.ui.theme.SyntaxXmlTag
import com.aerotech.aerocode.ui.theme.SyntaxXmlValue
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CodeEditorView(
    initialContent: String,
    onContentChange: (String) -> Unit,
    diagnostics: List<Diagnostic>,
    completions: List<CompletionItem>,
    onSelectCompletion: (CompletionItem) -> Unit,
    activeFilePath: String = "",
    fontSizeSp: Int = 14,
    showLineNumbers: Boolean = true,
    tabSize: Int = 4,
    onCursorChange: ((line: Int, column: Int, currentText: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val colors = AeroTheme.colors

    // Stable Editor State: TextFieldValue preserves selection and composition
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(initialContent, TextRange(initialContent.length)))
    }

    // Synchronize if external content changes and is not what we currently have
    LaunchedEffect(initialContent) {
        if (initialContent != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = initialContent,
                selection = TextRange(textFieldValue.selection.start.coerceAtMost(initialContent.length))
            )
        }
    }

    val historyManager = remember { HistoryManager() }

    // Search & Diagnostics states
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var showDiagnosticsSheet by remember { mutableStateOf(false) }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    // Text layout result for calculating cursor bounding box
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val lines = remember(textFieldValue.text) {
        textFieldValue.text.lines()
    }

    // Keyboard and IME handling: bring cursor into view when typing & report cursor position
    LaunchedEffect(textFieldValue.selection, textFieldValue.text) {
        val cursor = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
        val textUpToCursor = textFieldValue.text.take(cursor)
        val currentLine = textUpToCursor.count { it == '\n' } + 1
        val lastNewline = textUpToCursor.lastIndexOf('\n')
        val currentCol = if (lastNewline == -1) cursor else cursor - (lastNewline + 1)
        onCursorChange?.invoke(currentLine, currentCol, textFieldValue.text)

        val layout = textLayoutResult
        if (layout != null) {
            val rect = try {
                layout.getCursorRect(cursor)
            } catch (e: Exception) {
                Rect.Zero
            }
            if (rect != Rect.Zero) {
                bringIntoViewRequester.bringIntoView(rect)
            }
        }
    }

    fun applyTextChange(newValue: TextFieldValue) {
        historyManager.recordEdit(textFieldValue, newValue)
        textFieldValue = newValue
        onContentChange(newValue.text)
    }

    fun handleUndo() {
        val restored = historyManager.undo(textFieldValue)
        if (restored != null) {
            textFieldValue = restored
            onContentChange(restored.text)
        }
    }

    fun handleRedo() {
        val restored = historyManager.redo(textFieldValue)
        if (restored != null) {
            textFieldValue = restored
            onContentChange(restored.text)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding() // Adjusts viewport above soft keyboard
    ) {
        // Quick Action Bar: Undo, Redo, Search, Diagnostics
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { handleUndo() },
                        enabled = historyManager.canUndo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (historyManager.canUndo) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { handleRedo() },
                        enabled = historyManager.canRedo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (historyManager.canRedo) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isSearchOpen = !isSearchOpen },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Find & Replace",
                            tint = if (isSearchOpen) colors.primary else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Diagnostics Indicator
                val errorCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
                val warningCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDiagnosticsSheet = !showDiagnosticsSheet }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (errorCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(AeroRose)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$errorCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroRose
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (warningCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(AeroAmber)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$warningCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroAmber
                        )
                    }

                    if (errorCount == 0 && warningCount == 0) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "No Issues",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "No Issues",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF34D399)
                        )
                    }
                }
            }
        }

        // Search & Replace Bar
        AnimatedVisibility(visible = isSearchOpen) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Find...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val idx = textFieldValue.text.indexOf(searchQuery, ignoreCase = true)
                                    if (idx != -1) {
                                        textFieldValue = textFieldValue.copy(
                                            selection = TextRange(idx, idx + searchQuery.length)
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Find Next", tint = colors.primary)
                        }
                        IconButton(onClick = { isSearchOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = { replaceQuery = it },
                            placeholder = { Text("Replace with...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val newText = textFieldValue.text.replace(searchQuery, replaceQuery)
                                    applyTextChange(textFieldValue.copy(text = newText))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("All", color = Color(0xFF042F2E), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Diagnostics Drawer
        AnimatedVisibility(visible = showDiagnosticsSheet) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Diagnostics (${diagnostics.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        IconButton(
                            onClick = { showDiagnosticsSheet = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (diagnostics.isEmpty()) {
                        Text("No syntax or unresolved issues found.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34D399))
                    } else {
                        diagnostics.forEach { diag ->
                            val color = if (diag.severity == DiagnosticSeverity.ERROR) AeroRose else AeroAmber
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Jump to line
                                        val lineIdx = (diag.range.startLine - 1).coerceAtLeast(0)
                                        val linesList = textFieldValue.text.lines()
                                        if (lineIdx < linesList.size) {
                                            var offset = 0
                                            for (i in 0 until lineIdx) {
                                                offset += linesList[i].length + 1
                                            }
                                            textFieldValue = textFieldValue.copy(selection = TextRange(offset))
                                        }
                                        showDiagnosticsSheet = false
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Line ${diag.range.startLine}:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = color
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = diag.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Editor Body Box: Contains Line Numbers, Code Canvas, and Cursor-Anchored Autocomplete Overlay
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                // Line Numbers Gutter
                if (showLineNumbers) {
                    val maxLineDigits = lines.size.toString().length.coerceAtLeast(2)
                    val gutterWidth = (maxLineDigits * fontSizeSp * 0.7 + 16).dp.coerceAtLeast(36.dp)
                    Column(
                        modifier = Modifier
                            .background(colors.surface)
                            .padding(horizontal = 6.dp, vertical = 8.dp)
                            .width(gutterWidth),
                        horizontalAlignment = Alignment.End
                    ) {
                        lines.indices.forEach { index ->
                            Text(
                                text = "${index + 1}",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp + 8).sp,
                                    color = colors.textSecondary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(colors.borderGlass)
                    )
                }

                // Code Input Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .horizontalScroll(horizontalScrollState)
                        .bringIntoViewRequester(bringIntoViewRequester)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            applyTextChange(newValue)
                        },
                        onTextLayout = { layout ->
                            textLayoutResult = layout
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp + 8).sp,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        visualTransformation = { text ->
                            val isXml = activeFilePath.endsWith(".xml", ignoreCase = true)
                            val annotated = if (isXml) highlightXmlSyntax(text.text) else highlightKotlinSyntax(text.text)
                            androidx.compose.ui.text.input.TransformedText(
                                annotated,
                                androidx.compose.ui.text.input.OffsetMapping.Identity
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("code_editor_input")
                    )
                }
            }

            // CRITICAL FIX: Autocomplete Popup ANCHORED TO CURSOR
            val showCompletions = completions.isNotEmpty()
            if (showCompletions && textLayoutResult != null) {
                val layout = textLayoutResult!!
                val cursorOffset = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
                val cursorRect = try {
                    layout.getCursorRect(cursorOffset)
                } catch (e: Exception) {
                    Rect.Zero
                }

                // Calculate position relative to editor viewport with scroll offsets
                val gutterWidthPx = with(density) { (if (showLineNumbers) 45.dp else 12.dp).toPx() }
                val popupXPx = (cursorRect.left + gutterWidthPx - horizontalScrollState.value).coerceAtLeast(gutterWidthPx)
                val popupYPx = (cursorRect.bottom + 8f - verticalScrollState.value).coerceAtLeast(0f)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(popupXPx.roundToInt(), popupYPx.roundToInt()) }
                        .widthIn(max = 280.dp)
                        .heightIn(max = 200.dp)
                        .shadow(12.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceGlass)
                        .border(1.dp, colors.borderGlass, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        completions.take(5).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cursor = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
                                        val textBeforeCursor = textFieldValue.text.take(cursor)
                                        val prefix = textBeforeCursor.takeLastWhile {
                                            it.isLetterOrDigit() || it == '.' || it == '_' || it == '@' || it == '<' || it == ':' || it == '/' || it == '-'
                                        }
                                        val replaceStart = cursor - prefix.length
                                        val before = textFieldValue.text.substring(0, replaceStart)
                                        val after = textFieldValue.text.substring(cursor)
                                        var updatedText = before + item.insertText + after

                                        // Auto-import preservation: inject import statement directly into code buffer
                                        item.autoImport?.let { autoImp ->
                                            if (!updatedText.contains(autoImp) && !activeFilePath.endsWith(".xml", ignoreCase = true)) {
                                                val linesList = updatedText.lines().toMutableList()
                                                val lastImportIdx = linesList.indexOfLast { it.startsWith("import ") }
                                                val insertIdx = if (lastImportIdx >= 0) {
                                                    lastImportIdx + 1
                                                } else {
                                                    val pkgIdx = linesList.indexOfFirst { it.startsWith("package ") }
                                                    if (pkgIdx >= 0) pkgIdx + 1 else 0
                                                }
                                                linesList.add(insertIdx, "import $autoImp")
                                                updatedText = linesList.joinToString("\n")
                                            }
                                        }

                                        val newSelection = TextRange(replaceStart + item.insertText.length)
                                        applyTextChange(TextFieldValue(updatedText, newSelection))
                                        onSelectCompletion(item)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colors.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.kind.name.lowercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Keyboard Accessory Toolbar (Context-Aware for Kotlin vs XML)
        val isXmlFile = activeFilePath.endsWith(".xml", ignoreCase = true)
        val tabSpaces = " ".repeat(tabSize.coerceAtLeast(1))
        val symbols = if (isXmlFile) {
            listOf(
                "Tab" to tabSpaces,
                "<" to "<",
                ">" to ">",
                "</" to "</",
                "/>" to " />",
                "\"" to "\"",
                "=" to "=\"\"",
                "android:" to "android:",
                "match" to "\"match_parent\"",
                "wrap" to "\"wrap_content\"",
                "@string/" to "\"@string/\""
            )
        } else {
            listOf(
                "Tab" to tabSpaces,
                "{" to "{",
                "}" to "}",
                "(" to "(",
                ")" to ")",
                "\"" to "\"",
                "=" to " = ",
                "." to ".",
                "Modifier." to "Modifier.",
                "fun" to "fun ",
                "val" to "val ",
                "@Composable" to "@Composable\n"
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                symbols.forEach { (label, insert) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass),
                        modifier = Modifier.clickable {
                            val cursor = textFieldValue.selection.start
                            val before = textFieldValue.text.take(cursor)
                            val after = textFieldValue.text.substring(cursor)
                            val updated = before + insert + after
                            val newSelection = TextRange(cursor + insert.length)
                            applyTextChange(TextFieldValue(updated, newSelection))
                        }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

private val KEYWORD_REGEX = "\\b(package|import|class|interface|object|fun|val|var|override|public|private|protected|internal|if|else|when|return|for|while|try|catch|finally|throw|null|true|false|is|in|by|data|sealed|companion|suspend|inline|const|lateinit)\\b".toRegex()
private val ANNOTATION_REGEX = "@[a-zA-Z0-9_]+".toRegex()
private val TYPE_REGEX = "\\b(String|Int|Boolean|Float|Double|Long|Modifier|Color|Unit|ComponentActivity|Bundle|TextStyle|Dp|Composable|List|Set|Map|StateFlow|MutableStateFlow)\\b".toRegex()
private val STRING_REGEX = "\"[^\"]*\"".toRegex()
private val NUMBER_REGEX = "\\b\\d+(\\.\\d+)?[fFdDlL]?\\b".toRegex()
private val COMMENT_REGEX = "//.*|/\\*[\\s\\S]*?\\*/".toRegex()

// XML Regex Patterns
private val XML_PROLOG_REGEX = "<\\?xml[^>]*\\?>".toRegex()
private val XML_COMMENT_REGEX = "<!--[\\s\\S]*?-->".toRegex()
private val XML_TAG_REGEX = "</?[a-zA-Z0-9_\\-\\.:]+|/?>".toRegex()
private val XML_ATTR_NAME_REGEX = "\\b([a-zA-Z0-9_\\-:]+)(?=\\s*=)".toRegex()
private val XML_ATTR_VALUE_REGEX = "\"([^\"]*)\"|'([^']*)'".toRegex()

private var lastCodeInput: String? = null
private var lastHighlightedOutput: AnnotatedString? = null

private fun highlightKotlinSyntax(code: String): AnnotatedString {
    if (code == lastCodeInput && lastHighlightedOutput != null) {
        return lastHighlightedOutput!!
    }

    val result = buildAnnotatedString {
        append(code)

        // Keywords
        KEYWORD_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }

        // Annotations (@Composable, etc.)
        ANNOTATION_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxAnnotation, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
        }

        // Common Types
        TYPE_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxType), match.range.first, match.range.last + 1)
        }

        // String Literals
        STRING_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxString), match.range.first, match.range.last + 1)
        }

        // Numbers
        NUMBER_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxNumber), match.range.first, match.range.last + 1)
        }

        // Comments
        COMMENT_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxComment), match.range.first, match.range.last + 1)
        }
    }

    lastCodeInput = code
    lastHighlightedOutput = result
    return result
}

private var lastXmlInput: String? = null
private var lastXmlHighlightedOutput: AnnotatedString? = null

private fun highlightXmlSyntax(code: String): AnnotatedString {
    if (code == lastXmlInput && lastXmlHighlightedOutput != null) {
        return lastXmlHighlightedOutput!!
    }

    val result = buildAnnotatedString {
        append(code)

        // 1. Tag Names (<LinearLayout, </TextView>, />, <)
        XML_TAG_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxXmlTag, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }

        // 2. Attribute Names (android:id, xmlns:android, app:...)
        XML_ATTR_NAME_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxXmlAttribute), match.range.first, match.range.last + 1)
        }

        // 3. Attribute String Values ("match_parent", "@string/app_name")
        XML_ATTR_VALUE_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxXmlValue), match.range.first, match.range.last + 1)
        }

        // 4. XML Prologue <?xml ... ?>
        XML_PROLOG_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxAnnotation, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
        }

        // 5. XML Comments <!-- ... -->
        XML_COMMENT_REGEX.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = SyntaxComment), match.range.first, match.range.last + 1)
        }
    }

    lastXmlInput = code
    lastXmlHighlightedOutput = result
    return result
}
