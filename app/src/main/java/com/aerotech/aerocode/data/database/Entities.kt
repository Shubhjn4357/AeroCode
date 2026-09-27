package com.aerotech.aerocode.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val packageName: String,
    val path: String,
    val template: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val fileCount: Int = 0,
    val sizeBytes: Long = 0L
)

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val filePath: String,
    val fileName: String,
    val lastOpened: Long = System.currentTimeMillis()
)

@Entity(tableName = "plugins")
data class PluginEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val capabilities: String, // Comma separated enum names
    val isInstalled: Boolean = false,
    val isEnabled: Boolean = false,
    val grantedPermissions: String = "" // Comma separated permissions
)

@Entity(tableName = "workspace_state")
data class WorkspaceEntity(
    @PrimaryKey val projectId: String,
    val activeFilePath: String?,
    val openFilesJson: String, // JSON array of paths
    val editorMode: String = "CODE",
    val cursorPosition: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
