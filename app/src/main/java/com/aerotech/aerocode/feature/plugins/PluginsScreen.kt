package com.aerotech.aerocode.feature.plugins

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.plugin.model.AeroPlugin
import com.aerotech.aerocode.plugin.model.PluginPermission
import com.aerotech.aerocode.ui.components.AeroAmbientBackground
import com.aerotech.aerocode.ui.components.AeroGlassCard
import com.aerotech.aerocode.ui.theme.AeroTheme

@Composable
fun PluginsScreen(
    plugins: List<AeroPlugin>,
    onInstallPlugin: (pluginId: String, permissions: Set<PluginPermission>) -> Unit,
    onTogglePlugin: (pluginId: String, enabled: Boolean) -> Unit,
    onUninstallPlugin: (pluginId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AeroTheme.colors
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Installed, 1: Available
    var searchQuery by remember { mutableStateOf("") }
    var selectedPluginForDetails by remember { mutableStateOf<AeroPlugin?>(null) }

    val installed = remember(plugins, searchQuery) {
        plugins.filter { it.isInstalled && (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)) }
    }
    val available = remember(plugins, searchQuery) {
        plugins.filter { !it.isInstalled && (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)) }
    }

    AeroAmbientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header with Dynamic Theme Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surfaceGlass,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderGlass),
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Extension,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Plugin Architecture",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                "Minimal core + expandable capabilities",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                            text = {
                                Text(
                                    "Installed (${installed.size})",
                                    color = if (selectedTab == 0) colors.primary else colors.textSecondary,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "Available (${available.size})",
                                    color = if (selectedTab == 1) colors.primary else colors.textSecondary,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // Search Bar & List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search plugins...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.primary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = colors.surfaceGlass,
                            focusedContainerColor = colors.surfaceGlass,
                            unfocusedBorderColor = colors.borderGlass,
                            focusedBorderColor = colors.primary,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = colors.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                val currentList = if (selectedTab == 0) installed else available

                if (currentList.isEmpty()) {
                    item {
                        AeroGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Extension,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "No plugins found",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                } else {
                    items(currentList, key = { it.id }) { plugin ->
                        PluginItemCard(
                            plugin = plugin,
                            onClick = { selectedPluginForDetails = plugin },
                            onToggle = { enabled -> onTogglePlugin(plugin.id, enabled) },
                            onInstallClick = { selectedPluginForDetails = plugin }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    // Plugin Details & Permission Consent Dialog
    if (selectedPluginForDetails != null) {
        val plugin = selectedPluginForDetails!!
        PluginDetailsDialog(
            plugin = plugin,
            onDismiss = { selectedPluginForDetails = null },
            onInstallWithPermissions = { perms ->
                onInstallPlugin(plugin.id, perms)
                selectedPluginForDetails = null
            },
            onUninstall = {
                onUninstallPlugin(plugin.id)
                selectedPluginForDetails = null
            }
        )
    }
}

@Composable
fun PluginItemCard(
    plugin: AeroPlugin,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onInstallClick: () -> Unit
) {
    val colors = AeroTheme.colors
    AeroGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Extension,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            plugin.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            "v${plugin.version} • ${plugin.author}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (plugin.isInstalled) {
                    Switch(
                        checked = plugin.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primary
                        )
                    )
                } else {
                    Button(
                        onClick = onInstallClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = if (colors.isDark) Color(0xFF042F2E) else Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                plugin.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Capabilities Tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                plugin.capabilities.forEach { cap ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.primary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = cap.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginDetailsDialog(
    plugin: AeroPlugin,
    onDismiss: () -> Unit,
    onInstallWithPermissions: (Set<PluginPermission>) -> Unit,
    onUninstall: () -> Unit
) {
    val colors = AeroTheme.colors
    var grantedPermissions by remember {
        mutableStateOf(plugin.requestedPermissions.toSet())
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        plugin.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        "Version ${plugin.version} by ${plugin.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                plugin.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Permissions Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Permissions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (plugin.requestedPermissions.isEmpty()) {
                Text(
                    "No special permissions requested.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            } else {
                plugin.requestedPermissions.forEach { perm ->
                    val isChecked = grantedPermissions.contains(perm)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                grantedPermissions = if (checked) {
                                    grantedPermissions + perm
                                } else {
                                    grantedPermissions - perm
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                perm.label,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                perm.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", color = colors.textPrimary)
                }

                if (!plugin.isInstalled) {
                    Button(
                        onClick = { onInstallWithPermissions(grantedPermissions) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = if (colors.isDark) Color(0xFF042F2E) else Color.White
                        )
                    ) {
                        Text("Grant & Install", fontWeight = FontWeight.Bold)
                    }
                } else if (!plugin.isBuiltIn) {
                    Button(
                        onClick = onUninstall,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Uninstall", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
