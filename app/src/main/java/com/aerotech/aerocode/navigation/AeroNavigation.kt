package com.aerotech.aerocode.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aerotech.aerocode.AeroCodeApplication
import com.aerotech.aerocode.feature.createproject.CreateProjectBottomSheet
import com.aerotech.aerocode.feature.createproject.CreateProjectStep
import com.aerotech.aerocode.feature.editor.WorkspaceScreen
import com.aerotech.aerocode.feature.home.HomeScreen
import com.aerotech.aerocode.feature.plugins.PluginsScreen
import com.aerotech.aerocode.feature.projects.ProjectsScreen
import com.aerotech.aerocode.feature.settings.SettingsScreen
import com.aerotech.aerocode.ui.components.AeroFloatingGlassDock
import com.aerotech.aerocode.ui.components.NavDestination
import com.aerotech.aerocode.ui.theme.AeroTheme
import kotlinx.coroutines.launch

sealed interface Screen {
    data class Main(val destination: NavDestination) : Screen
    data class Workspace(val projectId: String) : Screen
}

@Composable
fun AeroNavigationApp(modifier: Modifier = Modifier) {
    val app = AeroCodeApplication.instance
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main(NavDestination.HOME)) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var initialStepForDialog by remember { mutableStateOf(CreateProjectStep.FORM) }
    var initialTemplateIdForDialog by remember { mutableStateOf<String?>(null) }

    val projects by app.projectRepository.allProjects.collectAsState(initial = emptyList())
    val plugins by app.pluginManager.plugins.collectAsState()

    fun openProject(id: String) {
        currentScreen = Screen.Workspace(id)
    }

    when (val screen = currentScreen) {
        is Screen.Workspace -> {
            WorkspaceScreen(
                projectId = screen.projectId,
                onNavigateBack = {
                    currentScreen = Screen.Main(NavDestination.HOME)
                },
                modifier = modifier
            )
        }

        is Screen.Main -> {
            BackHandler(enabled = screen.destination != NavDestination.HOME) {
                currentScreen = Screen.Main(NavDestination.HOME)
            }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(AeroTheme.colors.background)
            ) {
                // Main Screen Content
                when (screen.destination) {
                    NavDestination.HOME -> {
                        HomeScreen(
                            projects = projects,
                            onOpenProject = { openProject(it) },
                            onCreateProjectClick = {
                                initialStepForDialog = CreateProjectStep.FORM
                                initialTemplateIdForDialog = null
                                showCreateProjectDialog = true
                            },
                            onImportTemplateClick = {
                                initialStepForDialog = CreateProjectStep.TEMPLATE_GRID
                                initialTemplateIdForDialog = null
                                showCreateProjectDialog = true
                            },
                            onDuplicateProject = { id, name ->
                                coroutineScope.launch {
                                    app.projectRepository.duplicateProject(id, name)
                                }
                            },
                            onDeleteProject = { id ->
                                coroutineScope.launch {
                                    app.projectRepository.deleteProject(id)
                                }
                            }
                        )
                    }

                    NavDestination.PROJECTS -> {
                        ProjectsScreen(
                            projects = projects,
                            onOpenProject = { openProject(it) },
                            onCreateProjectClick = {
                                initialStepForDialog = CreateProjectStep.FORM
                                initialTemplateIdForDialog = null
                                showCreateProjectDialog = true
                            },
                            onDuplicateProject = { id, name ->
                                coroutineScope.launch {
                                    app.projectRepository.duplicateProject(id, name)
                                }
                            },
                            onDeleteProject = { id ->
                                coroutineScope.launch {
                                    app.projectRepository.deleteProject(id)
                                }
                            }
                        )
                    }

                    NavDestination.PLUGINS -> {
                        PluginsScreen(
                            plugins = plugins,
                            onInstallPlugin = { id, perms ->
                                coroutineScope.launch {
                                    app.pluginManager.installPlugin(id, perms)
                                }
                            },
                            onTogglePlugin = { id, enabled ->
                                coroutineScope.launch {
                                    app.pluginManager.togglePlugin(id, enabled)
                                }
                            },
                            onUninstallPlugin = { id ->
                                coroutineScope.launch {
                                    app.pluginManager.uninstallPlugin(id)
                                }
                            }
                        )
                    }

                    NavDestination.SETTINGS -> {
                        SettingsScreen(
                            preferences = app.preferences,
                            coroutineScope = coroutineScope
                        )
                    }
                }

                // Floating Glass Dock Bottom Navigation
                AeroFloatingGlassDock(
                    currentDestination = screen.destination,
                    onNavigate = { dest ->
                        isFabExpanded = false
                        currentScreen = Screen.Main(dest)
                    },
                    isFabExpanded = isFabExpanded,
                    onFabToggle = { isFabExpanded = !isFabExpanded },
                    onCreateProjectClick = {
                        initialStepForDialog = CreateProjectStep.FORM
                        initialTemplateIdForDialog = null
                        showCreateProjectDialog = true
                    },
                    onImportTemplateClick = {
                        initialStepForDialog = CreateProjectStep.TEMPLATE_GRID
                        initialTemplateIdForDialog = null
                        showCreateProjectDialog = true
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Create Project Bottom Sheet
    if (showCreateProjectDialog) {
        CreateProjectBottomSheet(
            initialStep = initialStepForDialog,
            initialTemplateId = initialTemplateIdForDialog,
            onDismiss = { showCreateProjectDialog = false },
            onCreateProject = { name, pkg, templateId, javaVer, gradleVer, minSdk ->
                showCreateProjectDialog = false
                coroutineScope.launch {
                    val created = app.projectRepository.createProject(
                        name = name,
                        packageName = pkg,
                        templateId = templateId,
                        javaVersion = javaVer,
                        gradleVersion = gradleVer,
                        minSdk = minSdk
                    )
                    openProject(created.id)
                }
            }
        )
    }
}
