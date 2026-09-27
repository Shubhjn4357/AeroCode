package com.aerotech.aerocode.domain.editor

interface CodeEditor {
    fun setContent(text: String)
    fun getContent(): String
    fun setSelection(start: Int, end: Int)
    fun replace(start: Int, end: Int, text: String)
    fun showDiagnostics(diagnostics: List<Diagnostic>)
}

interface LanguageService {
    suspend fun completions(
        document: Document,
        position: Position
    ): List<CompletionItem>

    suspend fun diagnostics(
        document: Document
    ): List<Diagnostic>

    suspend fun imports(
        document: Document
    ): List<ImportSuggestion>

    suspend fun symbols(
        document: Document
    ): List<Symbol>
}

interface ImportResolver {
    suspend fun resolve(
        symbol: String,
        context: ProjectContext
    ): List<ImportCandidate>
}
