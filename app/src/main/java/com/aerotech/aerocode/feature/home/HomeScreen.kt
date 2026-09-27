package com.aerotech.aerocode.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.AeroCodeApplication
import com.aerotech.aerocode.data.database.ProjectEntity
import com.aerotech.aerocode.feature.auth.AccountSyncDialog
import com.aerotech.aerocode.feature.auth.CloudSyncHomeCard
import com.aerotech.aerocode.feature.auth.UserAccountBadge
import com.aerotech.aerocode.ui.components.AeroAmbientBackground
import com.aerotech.aerocode.ui.components.AeroGlassCard
import com.aerotech.aerocode.ui.theme.AeroCyan
import com.aerotech.aerocode.ui.theme.AeroTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    projects: List<ProjectEntity>,
    onOpenProject: (String) -> Unit,
    onCreateProjectClick: () -> Unit,
    onImportTemplateClick: () -> Unit,
    onDuplicateProject: (String, String) -> Unit,
    onDeleteProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = AeroCodeApplication.instance
    val colors = AeroTheme.colors
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showAccountDialog by remember { mutableStateOf(false) }

    val currentUser by app.authService.currentUser.collectAsState()

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val filteredProjects = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) projects
        else projects.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    AeroAmbientBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp)
        ) {
            // App Brand Header & Greeting
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Aero",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            )
                            Text(
                                text = "Code",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            )
                        }
                        Text(
                            text = "Build something.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }

                    // Account & Status Badges
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAccountBadge(
                            user = currentUser,
                            onClick = { showAccountDialog = true }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Quick Hero Action Glass Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // New Project Glass Card
                    AeroGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_card_new_project"),
                        onClick = onCreateProjectClick
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "New Project",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Scaffold Compose app",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Templates Glass Card
                    AeroGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_card_templates"),
                        onClick = onImportTemplateClick
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.secondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    tint = colors.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Templates",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Store, Counter, M3",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Cloud Sync Persistence Card
            item {
                CloudSyncHomeCard(
                    user = currentUser,
                    projects = projects,
                    onOpenAccountDialog = { showAccountDialog = true }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search projects...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_projects"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = colors.surfaceGlass,
                        focusedContainerColor = colors.surfaceGlass,
                        unfocusedBorderColor = colors.borderGlass,
                        focusedBorderColor = colors.primary
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section Title: Recent
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${filteredProjects.size} projects",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Projects List
            if (filteredProjects.isEmpty()) {
                item {
                    AeroGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = colors.textSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No projects found",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ New Project' to create your first Compose application.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredProjects, key = { it.id }) { project ->
                    var menuExpanded by remember { mutableStateOf(false) }
                    val formattedTime = remember(project.lastModified) {
                        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        sdf.format(Date(project.lastModified))
                    }

                    AeroGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_item_${project.name.replace(" ", "_")}"),
                        onClick = { onOpenProject(project.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = project.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = colors.secondary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = project.template,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = colors.secondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• $formattedTime",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = colors.textSecondary
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier.background(colors.cardBackground)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Open in Editor") },
                                        onClick = {
                                            menuExpanded = false
                                            onOpenProject(project.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Duplicate") },
                                        onClick = {
                                            menuExpanded = false
                                            onDuplicateProject(project.id, "${project.name} Copy")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = Color(0xFFF87171)) },
                                        onClick = {
                                            menuExpanded = false
                                            onDeleteProject(project.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showAccountDialog) {
        AccountSyncDialog(
            onDismiss = { showAccountDialog = false },
            projects = projects,
            onRestoreProject = { cloudProj ->
                showAccountDialog = false
                coroutineScope.launch {
                    val restored = app.projectRepository.createProject(
                        name = cloudProj.name,
                        packageName = cloudProj.packageName,
                        templateId = cloudProj.templateId
                    )
                    cloudProj.files["MainActivity.kt"]?.let { code ->
                        val mainFilePath = app.projectRepository.getMainFilePath(restored.id)
                        if (mainFilePath != null) {
                            app.fileSystem.write(mainFilePath, code)
                        }
                    }
                    onOpenProject(restored.id)
                }
            }
        )
    }
}
