package com.aerotech.aerocode.core.gradle

import com.aerotech.aerocode.core.filesystem.FileSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class GradleVersionInfo(
    val version: String,
    val buildTime: String = "",
    val isCurrent: Boolean = false,
    val isSnapshot: Boolean = false,
    val isNightly: Boolean = false,
    val isReleaseCandidate: Boolean = false,
    val downloadUrl: String = "",
    val checksumUrl: String = "",
    val isInstalled: Boolean = false
)

class GradleDistributionRepository(
    private val fileSystem: FileSystem,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    private var cachedVersions: List<GradleVersionInfo>? = null

    private val toolsBaseDir: File
        get() = File(fileSystem.getRootPath(), ".tools/gradle").apply { if (!exists()) mkdirs() }

    fun isVersionInstalled(version: String): Boolean {
        val destDir = File(toolsBaseDir, "gradle-$version")
        val binGradle = File(destDir, "bin/gradle")
        return destDir.exists() && (binGradle.exists() || destDir.listFiles()?.isNotEmpty() == true)
    }

    fun getInstalledVersions(): List<String> {
        val list = mutableListOf<String>()
        toolsBaseDir.listFiles()?.forEach { file ->
            if (file.isDirectory && file.name.startsWith("gradle-")) {
                list.add(file.name.removePrefix("gradle-"))
            }
        }
        return list
    }

    /**
     * Download and install Gradle distribution on demand from official services.gradle.org server.
     */
    suspend fun downloadAndInstallGradle(
        version: String,
        downloadUrl: String?,
        onProgress: (Float, String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val url = downloadUrl?.ifBlank { null }
                ?: "https://services.gradle.org/distributions/gradle-$version-bin.zip"

            val targetDir = File(toolsBaseDir, "gradle-$version")
            if (isVersionInstalled(version)) {
                onProgress(1.0f, "Gradle $version is already installed.")
                return@withContext Result.success(targetDir)
            }

            onProgress(0.05f, "Connecting to official Gradle server: $url...")
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AeroCode-Android-IDE")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
            val contentLength = body.contentLength()
            val tempZip = File(toolsBaseDir, "download_gradle_$version.zip")

            onProgress(0.15f, "Downloading Gradle distribution ($version)...")
            val input: InputStream = body.byteStream()
            val output = FileOutputStream(tempZip)
            val buffer = ByteArray(8192)
            var totalBytesRead = 0L
            var read: Int

            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                totalBytesRead += read
                if (contentLength > 0) {
                    val progress = 0.15f + (totalBytesRead.toFloat() / contentLength) * 0.65f
                    val mb = totalBytesRead / (1024 * 1024)
                    val totalMb = contentLength / (1024 * 1024)
                    onProgress(progress, "Downloading: $mb MB / $totalMb MB")
                }
            }
            output.flush()
            output.close()
            input.close()

            onProgress(0.85f, "Extracting Gradle package to workspace...")
            targetDir.mkdirs()
            ZipInputStream(tempZip.inputStream()).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    // The zip contains a top-level directory like "gradle-8.7/..."
                    val cleanPath = if (entryName.contains('/')) {
                        entryName.substringAfter('/')
                    } else entryName

                    if (cleanPath.isNotEmpty()) {
                        val outFile = File(targetDir, cleanPath)
                        if (entry.isDirectory) {
                            outFile.mkdirs()
                        } else {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { fos ->
                                val buf = ByteArray(4096)
                                var len: Int
                                while (zis.read(buf).also { len = it } > 0) {
                                    fos.write(buf, 0, len)
                                }
                            }
                            if (outFile.name == "gradle" || outFile.name.endsWith(".sh")) {
                                outFile.setExecutable(true, false)
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            tempZip.delete()
            onProgress(1.0f, "Gradle $version successfully installed and ready.")
            Result.success(targetDir)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch the official latest stable Gradle release from services.gradle.org/versions/current
     */
    suspend fun fetchCurrentStableVersion(): GradleVersionInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://services.gradle.org/versions/current")
            .header("User-Agent", "AeroCode-Android-IDE")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                GradleVersionInfo(
                    version = json.optString("version", "8.12.1"),
                    buildTime = json.optString("buildTime", ""),
                    isCurrent = json.optBoolean("current", true),
                    downloadUrl = json.optString("downloadUrl", "https://services.gradle.org/distributions/gradle-8.12.1-bin.zip"),
                    checksumUrl = json.optString("checksumUrl", ""),
                    isInstalled = isVersionInstalled(json.optString("version", "8.12.1"))
                )
            } else {
                // Fallback to known stable release
                getDefaultStableVersion()
            }
        } catch (_: Exception) {
            getDefaultStableVersion()
        }
    }

    /**
     * Fetch all Gradle versions from services.gradle.org/versions/all
     */
    suspend fun fetchAllVersions(): List<GradleVersionInfo> = withContext(Dispatchers.IO) {
        cachedVersions?.let { return@withContext it }

        val request = Request.Builder()
            .url("https://services.gradle.org/versions/all")
            .header("User-Agent", "AeroCode-Android-IDE")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(body)
                val list = mutableListOf<GradleVersionInfo>()

                for (i in 0 until jsonArray.length().coerceAtMost(50)) {
                    val obj = jsonArray.getJSONObject(i)
                    val ver = obj.optString("version")
                    list.add(
                        GradleVersionInfo(
                            version = ver,
                            buildTime = obj.optString("buildTime"),
                            isCurrent = obj.optBoolean("current", false),
                            isSnapshot = obj.optBoolean("snapshot", false),
                            isNightly = obj.optBoolean("nightly", false),
                            isReleaseCandidate = obj.optBoolean("releaseCandidate", false),
                            downloadUrl = obj.optString("downloadUrl"),
                            checksumUrl = obj.optString("checksumUrl"),
                            isInstalled = isVersionInstalled(ver)
                        )
                    )
                }
                cachedVersions = list
                list
            } else {
                listOf(getDefaultStableVersion())
            }
        } catch (_: Exception) {
            listOf(getDefaultStableVersion())
        }
    }

    /**
     * Read the current project's Gradle version from gradle-wrapper.properties
     */
    suspend fun getProjectGradleVersion(projectPath: String): String = withContext(Dispatchers.IO) {
        val wrapperPath = "$projectPath/gradle/wrapper/gradle-wrapper.properties"
        try {
            if (fileSystem.exists(wrapperPath)) {
                val content = fileSystem.read(wrapperPath)
                val match = Regex("""gradle-(\d+\.\d+(\.\d+)?)-""").find(content)
                return@withContext match?.groupValues?.get(1) ?: "8.11.1"
            }
        } catch (_: Exception) {}
        "8.11.1"
    }

    /**
     * Update project's gradle-wrapper.properties to use the specified Gradle version
     */
    suspend fun updateProjectGradleWrapper(projectPath: String, gradleVersion: String): Boolean = withContext(Dispatchers.IO) {
        val wrapperDir = "$projectPath/gradle/wrapper"
        val wrapperPath = "$wrapperDir/gradle-wrapper.properties"
        val newContent = """
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-${gradleVersion}-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
""".trimIndent()

        try {
            fileSystem.createDirectory(wrapperDir)
            fileSystem.write(wrapperPath, newContent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun getDefaultStableVersion(): GradleVersionInfo {
        return GradleVersionInfo(
            version = "8.12.1",
            buildTime = "20250123000000+0000",
            isCurrent = true,
            downloadUrl = "https://services.gradle.org/distributions/gradle-8.12.1-bin.zip",
            checksumUrl = "https://services.gradle.org/distributions/gradle-8.12.1-bin.zip.sha256",
            isInstalled = isVersionInstalled("8.12.1")
        )
    }
}
