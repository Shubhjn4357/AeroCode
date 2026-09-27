package com.aerotech.aerocode.core.filesystem

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class LocalFileSystem(private val context: Context) : FileSystem {

    private val baseDir: File = File(context.filesDir, "aerocode_workspace").apply {
        if (!exists()) mkdirs()
    }

    override fun getRootPath(): String = baseDir.absolutePath

    override suspend fun read(path: String): String = withContext(Dispatchers.IO) {
        val file = resolveFile(path)
        if (!file.exists()) {
            throw IOException("File does not exist: $path")
        }
        file.readText()
    }

    override suspend fun write(path: String, content: String): Unit = withContext(Dispatchers.IO) {
        val file = resolveFile(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    override suspend fun delete(path: String): Boolean = withContext(Dispatchers.IO) {
        val file = resolveFile(path)
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    override suspend fun exists(path: String): Boolean = withContext(Dispatchers.IO) {
        resolveFile(path).exists()
    }

    override suspend fun list(path: String): List<FileEntry> = withContext(Dispatchers.IO) {
        val dir = resolveFile(path)
        if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

        val files = dir.listFiles() ?: return@withContext emptyList()
        files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            .map { toFileEntry(it) }
    }

    override suspend fun createDirectory(path: String): Boolean = withContext(Dispatchers.IO) {
        val dir = resolveFile(path)
        dir.mkdirs()
    }

    override suspend fun copy(sourcePath: String, destPath: String): Boolean = withContext(Dispatchers.IO) {
        val src = resolveFile(sourcePath)
        val dst = resolveFile(destPath)
        if (src.isDirectory) {
            src.copyRecursively(dst, overwrite = true)
        } else {
            src.copyTo(dst, overwrite = true)
            true
        }
    }

    override suspend fun rename(oldPath: String, newPath: String): Boolean = withContext(Dispatchers.IO) {
        val oldFile = resolveFile(oldPath)
        val newFile = resolveFile(newPath)
        oldFile.renameTo(newFile)
    }

    private fun resolveFile(path: String): File {
        return if (path.startsWith("/")) {
            File(path)
        } else {
            File(baseDir, path)
        }
    }

    private fun toFileEntry(file: File): FileEntry {
        val children = if (file.isDirectory) {
            file.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                ?.map { toFileEntry(it) } ?: emptyList()
        } else {
            emptyList()
        }
        return FileEntry(
            path = file.absolutePath,
            name = file.name,
            isDirectory = file.isDirectory,
            sizeBytes = if (file.isFile) file.length() else 0L,
            lastModified = file.lastModified(),
            children = children
        )
    }
}
