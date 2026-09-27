package com.aerotech.aerocode.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.AeroCodeApplication
import com.aerotech.aerocode.core.storage.AeroPreferences
import com.aerotech.aerocode.core.storage.BuildSettings
import com.aerotech.aerocode.core.storage.EditorSettings
import com.aerotech.aerocode.core.storage.PreviewSettings
import com.aerotech.aerocode.feature.auth.AccountSyncDialog
import com.aerotech.aerocode.ui.components.AeroAmbientBackground
import com.aerotech.aerocode.ui.components.AeroGlassCard
import com.aerotech.aerocode.ui.theme.AccentTheme
import com.aerotech.aerocode.ui.theme.AeroCyan
import com.aerotech.aerocode.ui.theme.AeroTheme
import com.aerotech.aerocode.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferences: AeroPreferences,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val app = AeroCodeApplication.instance
    val colors = AeroTheme.colors
    val currentThemeMode by preferences.themeMode.collectAsState(initial = ThemeMode.DARK)
    val currentAccent by preferences.accentTheme.collectAsState(initial = AccentTheme.CYAN)
    val editorSettings by preferences.editorSettings.collectAsState(initial = EditorSettings())
    val buildSettings by preferences.buildSettings.collectAsState(initial = BuildSettings())
    val currentUser by app.authService.currentUser.collectAsState()
    val syncStatus by app.firestoreSyncManager.syncStatus.collectAsState()
    var showAccountDialog by remember { mutableStateOf(false) }

    AeroAmbientBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        )
                        Text(
                            text = "Global theme, editor, build, and storage preferences",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Firebase Account & Firestore Cloud Sync
            item {
                SettingsSectionHeader(icon = Icons.Default.Cloud, title = "Account & Cloud Sync (Firebase)")
                AeroGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (currentUser != null) Icons.Default.Person else Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = colors.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = currentUser?.displayName ?: "Guest / Local Mode",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = currentUser?.email ?: "Sign in with Google to backup projects in Firestore",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAccountDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text(if (currentUser != null) "Manage Cloud Sync" else "Sign In with Google", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            if (currentUser != null) {
                                OutlinedButton(
                                    onClick = { app.authService.signOut() },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Sign Out", color = colors.textSecondary)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Appearance & Global Theme Engine
            item {
                SettingsSectionHeader(icon = Icons.Default.Palette, title = "Appearance & Themes")
                AeroGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Theme Mode", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                val isSelected = currentThemeMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { coroutineScope.launch { preferences.setThemeMode(mode) } },
                                    label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary,
                                        selectedLabelColor = if (colors.isDark) Color(0xFF042F2E) else Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Accent Palette", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AccentTheme.entries.forEach { accent ->
                                val isSelected = currentAccent == accent
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accent.primary)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else colors.borderGlass,
                                            shape = CircleShape
                                        )
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        color = if (isSelected) accent.primary else accent.primary.copy(alpha = 0.8f),
                                        onClick = {
                                            coroutineScope.launch { preferences.setAccentTheme(accent) }
                                        }
                                    ) {}
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Editor Preferences
            item {
                SettingsSectionHeader(icon = Icons.Default.Code, title = "Editor")
                AeroGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Font Size: ${editorSettings.fontSizeSp} sp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                        Slider(
                            value = editorSettings.fontSizeSp.toFloat(),
                            onValueChange = { coroutineScope.launch { preferences.setEditorFontSize(it.toInt()) } },
                            valueRange = 11f..22f,
                            steps = 10
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tab Size",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 4).forEach { size ->
                                FilterChip(
                                    selected = editorSettings.tabSize == size,
                                    onClick = { coroutineScope.launch { preferences.setTabSize(size) } },
                                    label = { Text("$size spaces") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingToggleRow(
                            title = "Line Numbers",
                            subtitle = "Show gutter line indicators",
                            checked = editorSettings.showLineNumbers,
                            onCheckedChange = { coroutineScope.launch { preferences.setShowLineNumbers(it) } }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingToggleRow(
                            title = "Auto Save",
                            subtitle = "Save changes automatically after 800ms pause",
                            checked = editorSettings.autoSave,
                            onCheckedChange = { coroutineScope.launch { preferences.setAutoSave(it) } }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Build Preferences
            item {
                SettingsSectionHeader(icon = Icons.Default.Build, title = "Build Engine")
                AeroGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Build Variant", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("debug", "release").forEach { variant ->
                                FilterChip(
                                    selected = buildSettings.buildVariant == variant,
                                    onClick = { coroutineScope.launch { preferences.setBuildVariant(variant) } },
                                    label = { Text(variant.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingToggleRow(
                            title = "Parallel Build",
                            subtitle = "Concurrent Gradle task execution",
                            checked = buildSettings.parallelBuild,
                            onCheckedChange = {}
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Storage & Cache
            item {
                SettingsSectionHeader(icon = Icons.Default.Storage, title = "Storage")
                AeroGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Workspace Cache", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                                Text("Compiler artifacts and intermediate files", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            Button(
                                onClick = { coroutineScope.launch { preferences.clearCrashRecovery() } },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceGlass)
                            ) {
                                Text("Clear", color = colors.primary, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // About
            item {
                SettingsSectionHeader(icon = Icons.Default.Info, title = "About")
                AeroGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AeroCode 2.0", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                        Text("Redesigned Mobile Jetpack Compose IDE for Android", style = MaterialTheme.typography.bodySmall, color = colors.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Package: com.aerotech.aerocode\nArchitecture: Glassmorphism + Stable Editor + Compose AST + Verified Build Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showAccountDialog) {
        AccountSyncDialog(onDismiss = { showAccountDialog = false })
    }
}

@Composable
fun SettingsSectionHeader(icon: ImageVector, title: String) {
    val colors = AeroTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary
        )
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = AeroTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
