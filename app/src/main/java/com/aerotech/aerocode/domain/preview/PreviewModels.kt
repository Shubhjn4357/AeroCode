package com.aerotech.aerocode.domain.preview

import com.aerotech.aerocode.domain.visual.ComponentNode

enum class DeviceType(val label: String, val widthDp: Int, val heightDp: Int) {
    PHONE("Pixel 9", 412, 892),
    FOLDABLE("Foldable", 673, 841),
    TABLET("Tablet 11\"", 800, 1280)
}

data class PreviewConfig(
    val device: DeviceType = DeviceType.PHONE,
    val scale: Float = 1.0f,
    val isDarkTheme: Boolean = true,
    val isInspectMode: Boolean = false,
    val selectedComponentId: String? = null
)

sealed interface PreviewResult {
    data class Success(
        val rootNode: ComponentNode,
        val renderTimeMs: Long
    ) : PreviewResult

    data class Error(
        val message: String,
        val line: Int = 1
    ) : PreviewResult
}

interface PreviewEngine {
    suspend fun render(
        sourceCode: String,
        config: PreviewConfig
    ): PreviewResult
}
