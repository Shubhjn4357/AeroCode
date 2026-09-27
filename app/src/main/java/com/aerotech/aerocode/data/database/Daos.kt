package com.aerotech.aerocode.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files WHERE projectId = :projectId ORDER BY lastOpened DESC LIMIT 10")
    fun getRecentFiles(projectId: String): Flow<List<RecentFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentFile(recentFile: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE projectId = :projectId")
    suspend fun clearRecentFilesForProject(projectId: String)
}

@Dao
interface PluginDao {
    @Query("SELECT * FROM plugins")
    fun getAllPlugins(): Flow<List<PluginEntity>>

    @Query("SELECT * FROM plugins WHERE id = :id LIMIT 1")
    suspend fun getPluginById(id: String): PluginEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(plugin: PluginEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(plugins: List<PluginEntity>)

    @Query("UPDATE plugins SET isInstalled = :installed, isEnabled = :enabled, grantedPermissions = :permissions WHERE id = :id")
    suspend fun updatePluginState(id: String, installed: Boolean, enabled: Boolean, permissions: String)
}

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspace_state WHERE projectId = :projectId LIMIT 1")
    suspend fun getWorkspace(projectId: String): WorkspaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWorkspace(workspace: WorkspaceEntity)

    @Query("DELETE FROM workspace_state WHERE projectId = :projectId")
    suspend fun deleteWorkspace(projectId: String)
}
