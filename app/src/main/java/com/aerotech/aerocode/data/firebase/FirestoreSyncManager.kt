package com.aerotech.aerocode.data.firebase

import android.content.Context
import com.aerotech.aerocode.data.database.ProjectEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncManager(
    private val context: Context,
    private val authService: FirebaseAuthService
) {
    private var firestore: FirebaseFirestore? = null

    private val _syncStatus = MutableStateFlow(CloudSyncStatus())
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _cloudProjects = MutableStateFlow<List<CloudProject>>(emptyList())
    val cloudProjects: StateFlow<List<CloudProject>> = _cloudProjects.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            // Firestore not ready or offline
        }
    }

    val isConfigured: Boolean
        get() = firestore != null

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized."))
        try {
            val userDoc = db.collection("users").document(profile.uid)
            val data = hashMapOf(
                "uid" to profile.uid,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "photoUrl" to profile.photoUrl,
                "isAnonymous" to profile.isAnonymous,
                "lastActive" to System.currentTimeMillis()
            )
            userDoc.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncProjectToCloud(
        project: ProjectEntity,
        files: Map<String, String> = emptyMap()
    ): Result<CloudProject> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized."))
        val currentUser = authService.currentUser.value
            ?: return@withContext Result.failure(IllegalStateException("User is not signed in."))

        _syncStatus.value = _syncStatus.value.copy(isSyncing = true, message = "Syncing ${project.name} to Firestore...")

        try {
            val projectDoc = db.collection("users")
                .document(currentUser.uid)
                .collection("projects")
                .document(project.id)

            val cloudProject = CloudProject(
                id = project.id,
                name = project.name,
                packageName = project.packageName,
                templateId = project.template,
                updatedAt = System.currentTimeMillis(),
                fileCount = project.fileCount,
                sizeBytes = project.sizeBytes,
                description = "Jetpack Compose Project",
                files = files
            )

            val payload = hashMapOf(
                "id" to cloudProject.id,
                "name" to cloudProject.name,
                "packageName" to cloudProject.packageName,
                "templateId" to cloudProject.templateId,
                "updatedAt" to cloudProject.updatedAt,
                "fileCount" to cloudProject.fileCount,
                "sizeBytes" to cloudProject.sizeBytes,
                "description" to cloudProject.description,
                "files" to cloudProject.files
            )

            projectDoc.set(payload, SetOptions.merge()).await()

            // Refresh cloud projects list
            fetchCloudProjects()

            _syncStatus.value = CloudSyncStatus(
                isSyncing = false,
                lastSyncedTime = System.currentTimeMillis(),
                syncedProjectCount = _cloudProjects.value.size,
                message = "Synced '${project.name}' successfully",
                isError = false
            )

            Result.success(cloudProject)
        } catch (e: Exception) {
            _syncStatus.value = CloudSyncStatus(
                isSyncing = false,
                lastSyncedTime = _syncStatus.value.lastSyncedTime,
                message = "Cloud sync failed: ${e.localizedMessage}",
                isError = true
            )
            Result.failure(e)
        }
    }

    suspend fun fetchCloudProjects(): Result<List<CloudProject>> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized."))
        val currentUser = authService.currentUser.value
            ?: return@withContext Result.failure(IllegalStateException("User is not signed in."))

        try {
            val snapshot = db.collection("users")
                .document(currentUser.uid)
                .collection("projects")
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                val packageName = doc.getString("packageName") ?: "com.example"
                val templateId = doc.getString("templateId") ?: "empty_compose"
                val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                val fileCount = doc.getLong("fileCount")?.toInt() ?: 0
                val sizeBytes = doc.getLong("sizeBytes") ?: 0L
                val description = doc.getString("description") ?: ""
                @Suppress("UNCHECKED_CAST")
                val files = (doc.get("files") as? Map<String, String>) ?: emptyMap()

                CloudProject(
                    id = id,
                    name = name,
                    packageName = packageName,
                    templateId = templateId,
                    updatedAt = updatedAt,
                    fileCount = fileCount,
                    sizeBytes = sizeBytes,
                    description = description,
                    files = files
                )
            }

            _cloudProjects.value = list
            _syncStatus.value = _syncStatus.value.copy(
                syncedProjectCount = list.size,
                lastSyncedTime = System.currentTimeMillis()
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCloudProject(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not initialized."))
        val currentUser = authService.currentUser.value
            ?: return@withContext Result.failure(IllegalStateException("User is not signed in."))

        try {
            db.collection("users")
                .document(currentUser.uid)
                .collection("projects")
                .document(projectId)
                .delete()
                .await()

            fetchCloudProjects()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
