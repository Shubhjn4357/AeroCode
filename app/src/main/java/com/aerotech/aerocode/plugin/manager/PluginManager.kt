package com.aerotech.aerocode.plugin.manager

import com.aerotech.aerocode.data.database.AeroDatabase
import com.aerotech.aerocode.data.database.PluginEntity
import com.aerotech.aerocode.plugin.model.AeroPlugin
import com.aerotech.aerocode.plugin.model.PluginCapability
import com.aerotech.aerocode.plugin.model.PluginPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class PluginManager(
    private val database: AeroDatabase
) {
    private val defaultRegistry = listOf(
        AeroPlugin(
            id = "core.kotlin.tools",
            name = "Kotlin Language Tools",
            version = "1.0.0",
            description = "Kotlin syntax highlighting, AST parsing, diagnostics, and code completions.",
            author = "AeroCode Core Team",
            capabilities = listOf(PluginCapability.LANGUAGE, PluginCapability.FORMATTER),
            requestedPermissions = listOf(PluginPermission.PROJECT_FILES),
            isInstalled = true,
            isEnabled = true,
            grantedPermissions = setOf(PluginPermission.PROJECT_FILES),
            isBuiltIn = true
        ),
        AeroPlugin(
            id = "core.m3.components",
            name = "Material 3 Component Pack",
            version = "1.2.0",
            description = "Standard Jetpack Compose UI component catalog for the visual layout designer.",
            author = "AeroCode Core Team",
            capabilities = listOf(PluginCapability.COMPONENTS, PluginCapability.TEMPLATE),
            requestedPermissions = listOf(PluginPermission.PROJECT_FILES),
            isInstalled = true,
            isEnabled = true,
            grantedPermissions = setOf(PluginPermission.PROJECT_FILES),
            isBuiltIn = true
        ),
        AeroPlugin(
            id = "core.preview.engine",
            name = "Aero Compose Preview",
            version = "1.1.0",
            description = "Real-time layout evaluation and interactive simulated device frame renderer.",
            author = "AeroCode Core Team",
            capabilities = listOf(PluginCapability.PREVIEW_ENGINE),
            requestedPermissions = listOf(PluginPermission.PROJECT_FILES),
            isInstalled = true,
            isEnabled = true,
            grantedPermissions = setOf(PluginPermission.PROJECT_FILES),
            isBuiltIn = true
        ),
        AeroPlugin(
            id = "core.gradle.engine",
            name = "Gradle Build Engine",
            version = "2.0.0",
            description = "Local build orchestration, APK packaging, signing, and asset merging.",
            author = "AeroCode Core Team",
            capabilities = listOf(PluginCapability.BUILD_TOOL, PluginCapability.TERMINAL_TOOL),
            requestedPermissions = listOf(PluginPermission.PROJECT_FILES, PluginPermission.PROCESS_EXECUTION, PluginPermission.STORAGE),
            isInstalled = true,
            isEnabled = true,
            grantedPermissions = setOf(PluginPermission.PROJECT_FILES, PluginPermission.PROCESS_EXECUTION, PluginPermission.STORAGE),
            isBuiltIn = true
        ),
        AeroPlugin(
            id = "ext.git.tools",
            name = "Git Version Control",
            version = "1.0.0",
            description = "Git repository management, commit history, branches, and diff inspection for AeroCode projects.",
            author = "AeroTech Community",
            capabilities = listOf(PluginCapability.GIT, PluginCapability.TERMINAL_TOOL),
            requestedPermissions = listOf(PluginPermission.PROJECT_FILES, PluginPermission.PROCESS_EXECUTION, PluginPermission.STORAGE),
            isInstalled = false,
            isEnabled = false,
            grantedPermissions = emptySet(),
            isBuiltIn = false
        ),
        AeroPlugin(
            id = "ext.ktor.client",
            name = "Ktor HTTP Suite",
            version = "2.3.0",
            description = "Networking scaffolding, HTTP request inspector, and REST API client templates.",
            author = "JetBrains & AeroTech",
            capabilities = listOf(PluginCapability.COMPONENTS, PluginCapability.TEMPLATE),
            requestedPermissions = listOf(PluginPermission.PROJECT_FILES, PluginPermission.NETWORK),
            isInstalled = false,
            isEnabled = false,
            grantedPermissions = emptySet(),
            isBuiltIn = false
        ),
        AeroPlugin(
            id = "ext.compose.charts",
            name = "Compose Charts & Visuals",
            version = "1.0.4",
            description = "Line charts, bar charts, and data visualization primitives for Jetpack Compose.",
            author = "Vico & AeroCode",
            capabilities = listOf(PluginCapability.COMPONENTS),
            requestedPermissions = listOf(PluginPermission.PROJECT_FILES),
            isInstalled = false,
            isEnabled = false,
            grantedPermissions = emptySet(),
            isBuiltIn = false
        )
    )

    private val _plugins = MutableStateFlow<List<AeroPlugin>>(defaultRegistry)
    val plugins: StateFlow<List<AeroPlugin>> = _plugins.asStateFlow()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        // Synchronize with database
        val entities = defaultRegistry.map { plugin ->
            PluginEntity(
                id = plugin.id,
                name = plugin.name,
                version = plugin.version,
                description = plugin.description,
                author = plugin.author,
                capabilities = plugin.capabilities.joinToString(",") { it.name },
                isInstalled = plugin.isInstalled,
                isEnabled = plugin.isEnabled,
                grantedPermissions = plugin.grantedPermissions.joinToString(",") { it.name }
            )
        }
        database.pluginDao().insertAll(entities)
    }

    suspend fun installPlugin(pluginId: String, permissions: Set<PluginPermission>) = withContext(Dispatchers.IO) {
        _plugins.value = _plugins.value.map { plugin ->
            if (plugin.id == pluginId) {
                plugin.copy(
                    isInstalled = true,
                    isEnabled = true,
                    grantedPermissions = permissions
                )
            } else {
                plugin
            }
        }
        database.pluginDao().updatePluginState(
            id = pluginId,
            installed = true,
            enabled = true,
            permissions = permissions.joinToString(",") { it.name }
        )
    }

    suspend fun togglePlugin(pluginId: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        _plugins.value = _plugins.value.map { plugin ->
            if (plugin.id == pluginId) {
                plugin.copy(isEnabled = enabled)
            } else {
                plugin
            }
        }
        val plugin = _plugins.value.find { it.id == pluginId } ?: return@withContext
        database.pluginDao().updatePluginState(
            id = pluginId,
            installed = plugin.isInstalled,
            enabled = enabled,
            permissions = plugin.grantedPermissions.joinToString(",") { it.name }
        )
    }

    suspend fun uninstallPlugin(pluginId: String) = withContext(Dispatchers.IO) {
        _plugins.value = _plugins.value.map { plugin ->
            if (plugin.id == pluginId && !plugin.isBuiltIn) {
                plugin.copy(
                    isInstalled = false,
                    isEnabled = false,
                    grantedPermissions = emptySet()
                )
            } else {
                plugin
            }
        }
        database.pluginDao().updatePluginState(
            id = pluginId,
            installed = false,
            enabled = false,
            permissions = ""
        )
    }
}
