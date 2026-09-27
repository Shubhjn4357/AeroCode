package com.aerotech.aerocode

import android.app.Application
import com.aerotech.aerocode.core.adb.AdbPlatformTools
import com.aerotech.aerocode.core.build.GradleBuildEngine
import com.aerotech.aerocode.core.compiler.ComposePreviewEngine
import com.aerotech.aerocode.core.compiler.KotlinLanguageService
import com.aerotech.aerocode.core.filesystem.LocalFileSystem
import com.aerotech.aerocode.core.git.GitEngine
import com.aerotech.aerocode.core.gradle.GradleDistributionRepository
import com.aerotech.aerocode.core.storage.AeroPreferences
import com.aerotech.aerocode.core.terminal.TerminalEngine
import com.aerotech.aerocode.data.database.AeroDatabase
import com.aerotech.aerocode.data.firebase.FirebaseAuthService
import com.aerotech.aerocode.data.firebase.FirestoreSyncManager
import com.aerotech.aerocode.data.repository.ProjectRepository
import com.aerotech.aerocode.plugin.manager.PluginManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AeroCodeApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var fileSystem: LocalFileSystem
        private set
    lateinit var database: AeroDatabase
        private set
    lateinit var projectRepository: ProjectRepository
        private set
    lateinit var buildEngine: GradleBuildEngine
        private set
    lateinit var previewEngine: ComposePreviewEngine
        private set
    lateinit var repoSymbolIndexer: RepoSymbolIndexer
        private set
    lateinit var languageService: KotlinLanguageService
        private set
    lateinit var adbTools: AdbPlatformTools
        private set
    lateinit var gradleRepository: GradleDistributionRepository
        private set
    lateinit var gitEngine: GitEngine
        private set
    lateinit var terminalEngine: TerminalEngine
        private set
    lateinit var pluginManager: PluginManager
        private set
    lateinit var preferences: AeroPreferences
        private set
    lateinit var authService: FirebaseAuthService
        private set
    lateinit var firestoreSyncManager: FirestoreSyncManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        fileSystem = LocalFileSystem(this)
        database = AeroDatabase.getInstance(this)
        projectRepository = ProjectRepository(database, fileSystem)
        buildEngine = GradleBuildEngine(fileSystem, this)
        previewEngine = ComposePreviewEngine()
        repoSymbolIndexer = RepoSymbolIndexer()
        languageService = KotlinLanguageService(repoIndexer = repoSymbolIndexer)
        adbTools = AdbPlatformTools(this, fileSystem)
        gradleRepository = GradleDistributionRepository(fileSystem)
        gitEngine = GitEngine()
        terminalEngine = TerminalEngine(fileSystem, buildEngine, adbTools)
        pluginManager = PluginManager(database)
        preferences = AeroPreferences(this)
        authService = FirebaseAuthService(this, applicationScope)
        firestoreSyncManager = FirestoreSyncManager(this, authService)

        applicationScope.launch {
            pluginManager.initialize()
            // Seed initial demo project if empty
            val existing = projectRepository.allProjects.firstOrNull()
            if (existing.isNullOrEmpty()) {
                projectRepository.createProject(
                    name = "My Shop",
                    packageName = "com.aerotech.myshop",
                    templateId = "shop_ui"
                )
            }
        }
    }

    companion object {
        lateinit var instance: AeroCodeApplication
            private set
    }
}
