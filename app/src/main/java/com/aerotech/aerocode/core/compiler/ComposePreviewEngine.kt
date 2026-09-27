package com.aerotech.aerocode.core.compiler

import com.aerotech.aerocode.domain.preview.PreviewConfig
import com.aerotech.aerocode.domain.preview.PreviewEngine
import com.aerotech.aerocode.domain.preview.PreviewResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComposePreviewEngine : PreviewEngine {

    override suspend fun render(
        sourceCode: String,
        config: PreviewConfig
    ): PreviewResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        try {
            // Check for basic syntax showstoppers before rendering
            if (sourceCode.isBlank()) {
                return@withContext PreviewResult.Error("Source file is empty", 1)
            }
            val rootNode = ComposeASTParser.parse(sourceCode)
            val elapsed = System.currentTimeMillis() - startTime
            PreviewResult.Success(rootNode, elapsed)
        } catch (e: Exception) {
            PreviewResult.Error(e.message ?: "Failed to render preview", 1)
        }
    }
}
