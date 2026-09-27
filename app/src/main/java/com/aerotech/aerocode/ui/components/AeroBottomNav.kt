package com.aerotech.aerocode.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.ui.theme.AeroCyan
import com.aerotech.aerocode.ui.theme.AeroIndigo
import com.aerotech.aerocode.ui.theme.DarkBorder
import com.aerotech.aerocode.ui.theme.DarkSurface

enum class NavDestination(val label: String) {
    HOME("Home"),
    PROJECTS("Projects"),
    PLUGINS("Plugins"),
    SETTINGS("Settings")
}

@Composable
fun AeroFloatingBottomBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    isFabExpanded: Boolean,
    onFabToggle: () -> Unit,
    onCreateProjectClick: () -> Unit,
    onImportTemplateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expandable Quick Action Menu
        AnimatedVisibility(
            visible = isFabExpanded,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                    shrinkVertically(spring(stiffness = Spring.StiffnessMedium))
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                color = DarkSurface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(220.dp)
                ) {
                    FabActionItem(
                        icon = Icons.Default.Add,
                        label = "Create Project",
                        onClick = {
                            onFabToggle()
                            onCreateProjectClick()
                        }
                    )
                    FabActionItem(
                        icon = Icons.Default.RocketLaunch,
                        label = "Starter Templates",
                        onClick = {
                            onFabToggle()
                            onImportTemplateClick()
                        }
                    )
                    FabActionItem(
                        icon = Icons.Default.Folder,
                        label = "Explore Projects",
                        onClick = {
                            onFabToggle()
                            onNavigate(NavDestination.PROJECTS)
                        }
                    )
                }
            }
        }

        // Floating Navigation Pill
        Surface(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, DarkBorder.copy(alpha = 0.6f), RoundedCornerShape(32.dp)),
            color = DarkSurface.copy(alpha = 0.95f),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Home
                NavButton(
                    selected = currentDestination == NavDestination.HOME,
                    onClick = { onNavigate(NavDestination.HOME) },
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    label = "Home",
                    testTag = "nav_home"
                )

                // Projects
                NavButton(
                    selected = currentDestination == NavDestination.PROJECTS,
                    onClick = { onNavigate(NavDestination.PROJECTS) },
                    selectedIcon = Icons.Filled.Folder,
                    unselectedIcon = Icons.Outlined.Folder,
                    label = "Projects",
                    testTag = "nav_projects"
                )

                // Center Expandable FAB (+)
                val rotation by animateFloatAsState(
                    targetValue = if (isFabExpanded) 45f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "fabRotation"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AeroCyan)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = onFabToggle
                        )
                        .testTag("nav_fab_plus"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Actions",
                        tint = Color(0xFF042F2E),
                        modifier = Modifier
                            .size(26.dp)
                            .rotate(rotation)
                    )
                }

                // Plugins
                NavButton(
                    selected = currentDestination == NavDestination.PLUGINS,
                    onClick = { onNavigate(NavDestination.PLUGINS) },
                    selectedIcon = Icons.Filled.Extension,
                    unselectedIcon = Icons.Outlined.Extension,
                    label = "Plugins",
                    testTag = "nav_plugins"
                )

                // Settings
                NavButton(
                    selected = currentDestination == NavDestination.SETTINGS,
                    onClick = { onNavigate(NavDestination.SETTINGS) },
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    label = "Settings",
                    testTag = "nav_settings"
                )
            }
        }
    }
}

@Composable
private fun NavButton(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tint = if (selected) AeroCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = AeroCyan),
                onClick = onClick
            )
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = tint
        )
    }
}

@Composable
private fun FabActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AeroCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
