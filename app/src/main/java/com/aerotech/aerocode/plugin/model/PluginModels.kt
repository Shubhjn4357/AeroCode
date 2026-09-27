package com.aerotech.aerocode.plugin.model

enum class PluginCapability(val displayName: String) {
    LANGUAGE("Language Tools"),
    COMPONENTS("Component Pack"),
    TEMPLATE("Project Templates"),
    BUILD_TOOL("Build Engine"),
    PREVIEW_ENGINE("Preview Renderer"),
    FORMATTER("Code Formatter"),
    TERMINAL_TOOL("Terminal Utility"),
    AI("AI Intelligence"),
    GIT("Version Control"),
    DEBUGGER("Debugger Engine")
}

enum class PluginPermission(val label: String, val description: String) {
    PROJECT_FILES("Project Files", "Read and write code in your project workspace"),
    PROCESS_EXECUTION("Process Execution", "Run compilation, shell, and build tools"),
    NETWORK("Network Access", "Download dependencies and remote artifacts"),
    STORAGE("Device Storage", "Export APKs and share files to external storage")
}

data class AeroPlugin(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val capabilities: List<PluginCapability>,
    val requestedPermissions: List<PluginPermission>,
    val isInstalled: Boolean = false,
    val isEnabled: Boolean = false,
    val grantedPermissions: Set<PluginPermission> = emptySet(),
    val isBuiltIn: Boolean = false
)
