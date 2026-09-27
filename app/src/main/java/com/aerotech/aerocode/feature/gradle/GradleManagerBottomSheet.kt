package com.aerotech.aerocode.feature.gradle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.AeroCodeApplication
import com.aerotech.aerocode.core.gradle.GradleVersionInfo
import com.aerotech.aerocode.core.permission.PermissionManager
import com.aerotech.aerocode.data.database.ProjectEntity
import com.aerotech.aerocode.ui.components.AeroGlassCard
import com.aerotech.aerocode.ui.components.StoragePermissionDialog
import com.aerotech.aerocode.ui.theme.AeroTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradleManagerBottomSheet(
    project: ProjectEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val app = AeroCodeApplication.instance
    val colors = AeroTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val repo = app.gradleRepository

    var isLoading by remember { mutableStateOf(true) }
    var currentStable by remember { mutableStateOf<GradleVersionInfo?>(null) }
    var projectVersion by remember { mutableStateOf("8.11.1") }
    var allVersions by remember { mutableStateOf<List<GradleVersionInfo>>(emptyList()) }
    var updateSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Downloading state
    var isDownloading by remember { mutableStateOf(false) }
    var downloadingVersion by remember { mutableStateOf("") }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadStatusText by remember { mutableStateOf("") }
    var showPermissionDialog by remember { mutableStateOf(false) }

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            currentStable = repo.fetchCurrentStableVersion()
            if (project != null) {
                projectVersion = repo.getProjectGradleVersion(project.path)
            }
            allVersions = repo.fetchAllVersions()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    if (showPermissionDialog) {
        StoragePermissionDialog(
            featureName = "Gradle Distributions",
            onDismiss = { showPermissionDialog = false },
            onPermissionGranted = {
                showPermissionDialog = false
                loadData()
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceGlass,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.borderGlass)
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Gradle Distributions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Official services.gradle.org on-demand repository",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Download Progress Bar if active
            if (isDownloading) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Downloading Gradle $downloadingVersion from official server...",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Text(
                                text = "${(downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = colors.primary,
                            trackColor = colors.borderGlass
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = downloadStatusText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            } else {
                // Active Gradle Distribution Card
                AeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Current Project Wrapper",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "Gradle $projectVersion",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                            }

                            currentStable?.let { stable ->
                                if (projectVersion == stable.version) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "UP TO DATE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (!PermissionManager.hasStoragePermission(context)) {
                                                showPermissionDialog = true
                                                return@Button
                                            }
                                            if (project != null) {
                                                coroutineScope.launch {
                                                    val ok = repo.updateProjectGradleWrapper(project.path, stable.version)
                                                    if (ok) {
                                                        projectVersion = stable.version
                                                        updateSuccessMessage = "Updated project wrapper to Gradle ${stable.version}!"
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Upgrade to ${stable.version}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        updateSuccessMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(msg, style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Available Gradle Versions List from official server
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Official Stable Releases (On-Demand Install)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textSecondary
                    )
                    IconButton(onClick = { loadData() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, colors.borderGlass, RoundedCornerShape(14.dp)),
                    color = colors.cardBackground
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(allVersions.filter { !it.isSnapshot && !it.isNightly }) { ver ->
                            val isSelected = ver.version == projectVersion
                            val isLocallyInstalled = repo.isVersionInstalled(ver.version)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Gradle ${ver.version}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) colors.primary else colors.textPrimary
                                    )
                                    if (ver.isCurrent) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = colors.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "LATEST",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = colors.primary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    if (isLocallyInstalled) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "INSTALLED",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isLocallyInstalled && !isDownloading) {
                                        IconButton(
                                            onClick = {
                                                if (!PermissionManager.hasStoragePermission(context)) {
                                                    showPermissionDialog = true
                                                    return@IconButton
                                                }
                                                coroutineScope.launch {
                                                    isDownloading = true
                                                    downloadingVersion = ver.version
                                                    repo.downloadAndInstallGradle(ver.version, ver.downloadUrl) { prog, msg ->
                                                        downloadProgress = prog
                                                        downloadStatusText = msg
                                                    }
                                                    isDownloading = false
                                                    loadData()
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = "Download & Install", tint = colors.primary)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.borderGlass,
                                        modifier = Modifier.clickable {
                                            if (project != null) {
                                                coroutineScope.launch {
                                                    repo.updateProjectGradleWrapper(project.path, ver.version)
                                                    projectVersion = ver.version
                                                    updateSuccessMessage = "Wrapper set to Gradle ${ver.version}."
                                                }
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = if (isSelected) "Active" else "Use",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) colors.primary else colors.textSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

