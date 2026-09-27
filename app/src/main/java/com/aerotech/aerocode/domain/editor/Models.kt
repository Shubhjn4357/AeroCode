package com.aerotech.aerocode.domain.editor

data class Position(
    val line: Int,
    val column: Int
)

data class Document(
    val path: String,
    val content: String
)

data class SourceRange(
    val startLine: Int,
    val startCol: Int,
    val endLine: Int,
    val endCol: Int
)

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO,
    HINT
}

data class Diagnostic(
    val message: String,
    val range: SourceRange,
    val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR,
    val code: String? = null
)

enum class CompletionKind {
    KEYWORD,
    COMPOSABLE,
    FUNCTION,
    PROPERTY,
    MODIFIER,
    TYPE,
    CLASS,
    SNIPPET,
    VARIABLE
}

data class CompletionItem(
    val label: String,
    val detail: String,
    val insertText: String,
    val kind: CompletionKind,
    val autoImport: String? = null
)

data class ImportSuggestion(
    val symbolName: String,
    val qualifiedName: String,
    val lineToInsert: Int = 3
)

data class Symbol(
    val name: String,
    val kind: String,
    val line: Int
)

data class ImportCandidate(
    val packageName: String,
    val simpleName: String,
    val isPreferred: Boolean = true
) {
    val fullyQualifiedName: String get() = "$packageName.$simpleName"
}

data class ProjectContext(
    val projectId: String,
    val activeFile: String,
    val currentPackage: String
)
