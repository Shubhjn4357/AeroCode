package com.aerotech.aerocode.data.firebase

data class UserProfile(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)

data class CloudProject(
    val id: String = "",
    val name: String = "",
    val packageName: String = "",
    val templateId: String = "empty_compose",
    val updatedAt: Long = System.currentTimeMillis(),
    val fileCount: Int = 0,
    val sizeBytes: Long = 0L,
    val description: String = "",
    val files: Map<String, String> = emptyMap()
)

data class CloudSyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncedTime: Long? = null,
    val syncedProjectCount: Int = 0,
    val message: String? = null,
    val isError: Boolean = false
)
