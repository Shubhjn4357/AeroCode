package com.aerotech.aerocode.core.dependencies

import com.aerotech.aerocode.core.filesystem.FileSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class AndroidLibrary(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val dependencyNotation: String,
    val version: String,
    val isOfficial: Boolean = true,
    val isInstalled: Boolean = false
)

class DependencyManager(
    private val fileSystem: FileSystem
) {
    val officialCatalog = listOf(
        AndroidLibrary(
            id = "compose_m3",
            name = "Material 3 (M3)",
            category = "UI & Compose",
            description = "Material Design 3 expressive components, buttons, sheets, dialogs",
            dependencyNotation = "androidx.compose.material3:material3",
            version = "1.3.0"
        ),
        AndroidLibrary(
            id = "compose_icons",
            name = "Material Icons Extended",
            category = "UI & Compose",
            description = "Complete set of Material Symbols and Android Studio icons",
            dependencyNotation = "androidx.compose.material:material-icons-extended",
            version = "1.7.4"
        ),
        AndroidLibrary(
            id = "nav_compose",
            name = "Navigation Compose",
            category = "Navigation",
            description = "Type-safe navigation backstack for Jetpack Compose screens",
            dependencyNotation = "androidx.navigation:navigation-compose",
            version = "2.8.3"
        ),
        AndroidLibrary(
            id = "room_runtime",
            name = "Room Database Runtime",
            category = "Data & Storage",
            description = "SQLite persistence abstraction for local offline caching",
            dependencyNotation = "androidx.room:room-runtime",
            version = "2.6.1"
        ),
        AndroidLibrary(
            id = "room_ktx",
            name = "Room Kotlin Extensions & Coroutines",
            category = "Data & Storage",
            description = "Kotlin coroutines and Flow integration for Room queries",
            dependencyNotation = "androidx.room:room-ktx",
            version = "2.6.1"
        ),
        AndroidLibrary(
            id = "datastore",
            name = "DataStore Preferences",
            category = "Data & Storage",
            description = "Modern asynchronous key-value storage replacing SharedPreferences",
            dependencyNotation = "androidx.datastore:datastore-preferences",
            version = "1.1.1"
        ),
        AndroidLibrary(
            id = "retrofit",
            name = "Retrofit HTTP Client",
            category = "Networking",
            description = "Type-safe REST client for Android and Kotlin coroutines",
            dependencyNotation = "com.squareup.retrofit2:retrofit",
            version = "2.11.0"
        ),
        AndroidLibrary(
            id = "retrofit_gson",
            name = "Retrofit Gson Converter",
            category = "Networking",
            description = "JSON serialization parser converter for Retrofit HTTP calls",
            dependencyNotation = "com.squareup.retrofit2:converter-gson",
            version = "2.11.0"
        ),
        AndroidLibrary(
            id = "okhttp",
            name = "OkHttp & Logging Interceptor",
            category = "Networking",
            description = "HTTP client with connection pooling and transparent GZIP",
            dependencyNotation = "com.squareup.okhttp3:logging-interceptor",
            version = "4.12.0"
        ),
        AndroidLibrary(
            id = "coil_compose",
            name = "Coil Image Loader",
            category = "Media",
            description = "Fast, lightweight image loading library for Jetpack Compose (AsyncImage)",
            dependencyNotation = "io.coil-kt:coil-compose",
            version = "2.7.0"
        ),
        AndroidLibrary(
            id = "lifecycle_vm",
            name = "Lifecycle ViewModel Compose",
            category = "Architecture",
            description = "Integration between AndroidX ViewModel and Composable lifecycles",
            dependencyNotation = "androidx.lifecycle:lifecycle-viewmodel-compose",
            version = "2.8.6"
        ),
        AndroidLibrary(
            id = "serialization",
            name = "KotlinX Serialization",
            category = "Architecture",
            description = "Official Kotlin multiplatform JSON serialization and deserialization",
            dependencyNotation = "org.jetbrains.kotlinx:kotlinx-serialization-json",
            version = "1.7.3"
        ),
        AndroidLibrary(
            id = "coroutines",
            name = "Kotlin Coroutines Android",
            category = "Architecture",
            description = "Dispatchers.Main and UI scheduling for asynchronous tasks",
            dependencyNotation = "org.jetbrains.kotlinx:kotlinx-coroutines-android",
            version = "1.9.0"
        ),
        AndroidLibrary(
            id = "permissions",
            name = "Accompanist Permissions",
            category = "System & Hardware",
            description = "Runtime permissions requesting wrappers for Jetpack Compose",
            dependencyNotation = "com.google.accompanist:accompanist-permissions",
            version = "0.36.0"
        )
    )

    suspend fun getLibraries(projectPath: String): List<AndroidLibrary> = withContext(Dispatchers.IO) {
        val gradleFile = findAppBuildGradle(projectPath)
        val content = if (gradleFile != null && fileSystem.exists(gradleFile.absolutePath)) {
            fileSystem.read(gradleFile.absolutePath)
        } else ""

        officialCatalog.map { lib ->
            val isInstalled = content.contains(lib.dependencyNotation)
            lib.copy(isInstalled = isInstalled)
        }
    }

    suspend fun installLibrary(projectPath: String, library: AndroidLibrary): Boolean = withContext(Dispatchers.IO) {
        val gradleFile = findAppBuildGradle(projectPath) ?: return@withContext false
        try {
            val content = fileSystem.read(gradleFile.absolutePath)
            if (content.contains(library.dependencyNotation)) {
                return@withContext true // already installed
            }

            val depLine = "    implementation(\"${library.dependencyNotation}:${library.version}\")"
            val updatedContent = if (content.contains("dependencies {")) {
                content.replaceFirst(
                    "dependencies {",
                    "dependencies {\n    // Added from AeroCode Library Manager\n$depLine"
                )
            } else {
                content + "\n\ndependencies {\n$depLine\n}\n"
            }

            fileSystem.write(gradleFile.absolutePath, updatedContent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun findAppBuildGradle(projectPath: String): File? {
        val appGradle = File(projectPath, "app/build.gradle.kts")
        if (appGradle.exists()) return appGradle
        val rootGradle = File(projectPath, "build.gradle.kts")
        if (rootGradle.exists()) return rootGradle
        return appGradle
    }
}
