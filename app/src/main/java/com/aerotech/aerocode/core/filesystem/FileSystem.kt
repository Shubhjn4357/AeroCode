package com.aerotech.aerocode.core.filesystem

data class FileEntry(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0L,
    val lastModified: Long = 0L,
    val children: List<FileEntry> = emptyList()
) {
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "")
}

interface FileSystem {
    suspend fun read(path: String): String
    suspend fun write(path: String, content: String)
    suspend fun delete(path: String): Boolean
    suspend fun exists(path: String): Boolean
    suspend fun list(path: String): List<FileEntry>
    suspend fun createDirectory(path: String): Boolean
    suspend fun copy(sourcePath: String, destPath: String): Boolean
    suspend fun rename(oldPath: String, newPath: String): Boolean
    fun getRootPath(): String
}
