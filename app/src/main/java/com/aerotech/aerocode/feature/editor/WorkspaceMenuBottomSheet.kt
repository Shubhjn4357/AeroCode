package com.aerotech.aerocode.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.ui.theme.AeroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceMenuBottomSheet(
    onDismiss: () -> Unit,
    onSaveFile: () -> Unit,
    onOpenGit: () -> Unit,
    onOpenGradle: () -> Unit,
    onOpenDependencies: () -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenAdb: () -> Unit,
    onSyncCloud: () -> Unit,
    onTriggerBuild: () -> Unit
) {
    val colors = AeroTheme.colors

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
                Column {
                    Text(
                        text = "Project Actions & Tools",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Android Studio Native Workflows",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Items
            MenuActionItem(
                icon = Icons.Default.Commit,
                iconTint = Color(0xFFF97316),
                title = "Git Version Control",
                subtitle = "Pull, Push, Merge, Rebase, Branch & History",
                onClick = {
                    onDismiss()
                    onOpenGit()
                }
            )

            MenuActionItem(
                icon = Icons.Default.Build,
                iconTint = Color(0xFF10B981),
                title = "Run & Assemble APK",
                subtitle = "Compile Kotlin & XML, generate signed debug APK",
                onClick = {
                    onDismiss()
                    onTriggerBuild()
                }
            )

            MenuActionItem(
                icon = Icons.Default.Extension,
                iconTint = Color(0xFF818CF8),
                title = "Install Studio Libraries & Extensions",
                subtitle = "Search & install Compose, Room, Retrofit dependencies",
                onClick = {
                    onDismiss()
                    onOpenDependencies()
                }
            )

            MenuActionItem(
                icon = Icons.Default.SettingsSuggest,
                iconTint = Color(0xFF38BDF8),
                title = "Gradle Distribution Manager",
                subtitle = "Inspect Gradle releases, sync gradle-wrapper.properties",
                onClick = {
                    onDismiss()
                    onOpenGradle()
                }
            )

            MenuActionItem(
                icon = Icons.Default.Devices,
                iconTint = Color(0xFF06B6D4),
                title = "ADB Platform Tools",
                subtitle = "Device discovery, Logcat streaming & APK install",
                onClick = {
                    onDismiss()
                    onOpenAdb()
                }
            )

            MenuActionItem(
                icon = Icons.Default.Terminal,
                iconTint = colors.primary,
                title = "Terminal & Shell Console",
                subtitle = "Integrated bash shell & gradlew execution",
                onClick = {
                    onDismiss()
                    onOpenTerminal()
                }
            )

            HorizontalDivider(
                color = colors.borderGlass,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onDismiss()
                            onSaveFile()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Buffer", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onDismiss()
                            onSyncCloud()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cloud Backup", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MenuActionItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = AeroTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = colors.textSecondary
            )
        }
    }
}
