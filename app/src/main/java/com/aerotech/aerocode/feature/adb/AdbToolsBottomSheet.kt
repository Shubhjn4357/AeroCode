package com.aerotech.aerocode.feature.adb

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.aerotech.aerocode.core.permission.PermissionManager
import com.aerotech.aerocode.data.database.ProjectEntity
import com.aerotech.aerocode.ui.components.AeroGlassCard
import com.aerotech.aerocode.ui.components.StoragePermissionDialog
import com.aerotech.aerocode.ui.theme.AeroTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbToolsBottomSheet(
    project: ProjectEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val app = AeroCodeApplication.instance
    val colors = AeroTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val adbTools = app.adbTools

    val devices by adbTools.connectedDevices.collectAsState()
    val logs by adbTools.logcatLogs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var installStatus by remember { mutableStateOf<String?>(null) }
    var shellCommandInput by remember { mutableStateOf("") }
    var shellOutput by remember { mutableStateOf<String?>(null) }

    // Platform-tools download state
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadStatusText by remember { mutableStateOf("") }
    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        adbTools.refreshDevices()
        adbTools.fetchLogcat()
    }

    if (showPermissionDialog) {
        StoragePermissionDialog(
            featureName = "Android SDK Platform Tools",
            onDismiss = { showPermissionDialog = false },
            onPermissionGranted = {
                showPermissionDialog = false
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
                .padding(horizontal = 20.dp)
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
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ADB Platform Tools",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Bridge, Device Runner & Logcat Inspector",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = colors.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = colors.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Devices", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        coroutineScope.launch { adbTools.fetchLogcat() }
                    },
                    text = { Text("Logcat", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("ADB Shell", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Connected Devices & APK Install
                    Column {
                        // Official Google Platform-Tools Installation Card
                        val isToolsInstalled = adbTools.isPlatformToolsInstalled()
                        AeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Official Platform-Tools (ADB)",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = if (isToolsInstalled) "Installed at .tools/platform-tools/adb" else "Google Android SDK Repository (dl.google.com)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isToolsInstalled) Color(0xFF10B981).copy(alpha = 0.15f) else colors.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (isToolsInstalled) "INSTALLED" else "AVAILABLE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = if (isToolsInstalled) Color(0xFF10B981) else colors.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (!isToolsInstalled && !isDownloading) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            if (!PermissionManager.hasStoragePermission(context)) {
                                                showPermissionDialog = true
                                                return@Button
                                            }
                                            coroutineScope.launch {
                                                isDownloading = true
                                                adbTools.downloadAndInstallPlatformTools { prog, msg ->
                                                    downloadProgress = prog
                                                    downloadStatusText = msg
                                                }
                                                isDownloading = false
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Download & Install ADB (Google Official)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                if (isDownloading) {
                                    Spacer(modifier = Modifier.height(10.dp))
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
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Attached Target Devices (${devices.size})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textSecondary
                            )
                            IconButton(onClick = { adbTools.refreshDevices() }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        devices.forEach { device ->
                            AeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhoneAndroid,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = device.model,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "${device.serial} • Status: ${device.state}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "ONLINE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // One-Click APK Deploy
                        Button(
                            onClick = {
                                if (project != null) {
                                    coroutineScope.launch {
                                        installStatus = "Deploying build to connected device..."
                                        val apkPath = "${project.path}/build/outputs/apk/debug/${project.name.replace(" ", "_").lowercase()}-debug.apk"
                                        val (_, msg) = adbTools.installApk(apkPath)
                                        installStatus = msg
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = if (colors.isDark) Color(0xFF042F2E) else Color.White
                            )
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Deploy & Install to Device", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    installStatus = "Configuring Android Platform-Tools & ADB daemon..."
                                    val (_, msg) = adbTools.installPlatformTools()
                                    installStatus = msg
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Install / Verify Platform-Tools", color = colors.textPrimary)
                        }

                        installStatus?.let { status ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = colors.primary
                            )
                        }
                    }
                }

                1 -> {
                    // Live Logcat
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "System Logcat (Recent)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textSecondary
                            )
                            IconButton(onClick = { coroutineScope.launch { adbTools.fetchLogcat() } }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 220.dp, max = 320.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, colors.borderGlass, RoundedCornerShape(14.dp)),
                            color = colors.cardBackground
                        ) {
                            LazyColumn(modifier = Modifier.padding(10.dp)) {
                                items(logs) { entry ->
                                    val levelColor = when (entry.level) {
                                        "E" -> Color(0xFFEF4444)
                                        "W" -> Color(0xFFF59E0B)
                                        "D" -> colors.primary
                                        else -> colors.textPrimary
                                    }
                                    Text(
                                        text = "${entry.timestamp} ${entry.level}/${entry.tag}: ${entry.message}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        ),
                                        color = levelColor,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Interactive ADB Shell
                    Column {
                        Text(
                            text = "Execute Command on Device Shell",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = shellCommandInput,
                                onValueChange = { shellCommandInput = it },
                                placeholder = { Text("e.g. getprop ro.build.version.release") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.borderGlass
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (shellCommandInput.isNotBlank()) {
                                        coroutineScope.launch {
                                            shellOutput = adbTools.runShellCommand(shellCommandInput.trim())
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 220.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, colors.borderGlass, RoundedCornerShape(14.dp)),
                            color = colors.cardBackground
                        ) {
                            Text(
                                text = shellOutput ?: "Enter a command and tap execute to inspect output.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = colors.textPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
