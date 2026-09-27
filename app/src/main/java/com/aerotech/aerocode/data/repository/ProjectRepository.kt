package com.aerotech.aerocode.data.repository

import com.aerotech.aerocode.core.filesystem.FileSystem
import com.aerotech.aerocode.core.project.ProjectGenerator
import com.aerotech.aerocode.core.project.ProjectTemplates
import com.aerotech.aerocode.data.database.AeroDatabase
import com.aerotech.aerocode.data.database.ProjectEntity
import com.aerotech.aerocode.data.database.RecentFileEntity
import com.aerotech.aerocode.data.database.WorkspaceEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

class ProjectRepository(
    private val database: AeroDatabase,
    private val fileSystem: FileSystem
) {
    val allProjects: Flow<List<ProjectEntity>> = database.projectDao().getAllProjects()

    suspend fun getProject(id: String): ProjectEntity? = database.projectDao().getProjectById(id)

    suspend fun createProject(
        name: String,
        packageName: String,
        templateId: String,
        javaVersion: Int = 17,
        gradleVersion: String = "8.7",
        minSdk: Int = 26
    ): ProjectEntity {
        val id = UUID.randomUUID().toString()
        val sanitizedFolderName = name.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val projectDirPath = "${fileSystem.getRootPath()}/projects/$sanitizedFolderName"

        val template = ProjectTemplates.ALL.find { it.id == templateId } ?: ProjectTemplates.EMPTY_COMPOSE
        val mainActivityPath = ProjectGenerator.generateProject(
            fileSystem = fileSystem,
            projectDir = projectDirPath,
            appName = name,
            packageName = packageName,
            template = template,
            javaVersion = javaVersion,
            gradleVersion = gradleVersion,
            minSdk = minSdk
        )

        val project = ProjectEntity(
            id = id,
            name = name,
            packageName = packageName,
            path = projectDirPath,
            template = template.name,
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis(),
            fileCount = 8,
            sizeBytes = 12400L
        )
        database.projectDao().insertProject(project)

        // Pre-seed workspace state pointing to MainActivity.kt
        val workspace = WorkspaceEntity(
            projectId = id,
            activeFilePath = mainActivityPath,
            openFilesJson = "[\"$mainActivityPath\"]",
            editorMode = "CODE"
        )
        database.workspaceDao().saveWorkspace(workspace)

        // Seed recent file
        recordRecentFile(id, mainActivityPath, "MainActivity.kt")

        return project
    }

    suspend fun duplicateProject(id: String, newName: String): ProjectEntity? {
        val original = getProject(id) ?: return null
        val newId = UUID.randomUUID().toString()
        val sanitizedNewFolder = newName.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val newPath = "${fileSystem.getRootPath()}/projects/$sanitizedNewFolder"

        fileSystem.copy(original.path, newPath)

        val duplicated = original.copy(
            id = newId,
            name = newName,
            path = newPath,
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        database.projectDao().insertProject(duplicated)
        return duplicated
    }

    suspend fun deleteProject(id: String) {
        val project = getProject(id) ?: return
        fileSystem.delete(project.path)
        database.projectDao().deleteProjectById(id)
        database.workspaceDao().deleteWorkspace(id)
        database.recentFileDao().clearRecentFilesForProject(id)
    }

    suspend fun updateProject(project: ProjectEntity) {
        database.projectDao().updateProject(project.copy(lastModified = System.currentTimeMillis()))
    }

    fun getRecentFiles(projectId: String): Flow<List<RecentFileEntity>> {
        return database.recentFileDao().getRecentFiles(projectId)
    }

    suspend fun recordRecentFile(projectId: String, filePath: String, fileName: String) {
        val id = "$projectId:$filePath"
        database.recentFileDao().insertRecentFile(
            RecentFileEntity(
                id = id,
                projectId = projectId,
                filePath = filePath,
                fileName = fileName,
                lastOpened = System.currentTimeMillis()
            )
        )
    }

    suspend fun getWorkspace(projectId: String): WorkspaceEntity? = database.workspaceDao().getWorkspace(projectId)

    suspend fun getMainFilePath(projectId: String): String? {
        val project = getProject(projectId) ?: return null
        val ws = getWorkspace(projectId)
        if (ws?.activeFilePath != null && fileSystem.exists(ws.activeFilePath)) {
            return ws.activeFilePath
        }
        val defaultMain = "${project.path}/app/src/main/kotlin/${project.packageName.replace('.', '/')}/MainActivity.kt"
        return if (fileSystem.exists(defaultMain)) defaultMain else null
    }

    suspend fun saveWorkspace(workspace: WorkspaceEntity) {
        database.workspaceDao().saveWorkspace(workspace.copy(updatedAt = System.currentTimeMillis()))
    }
}
