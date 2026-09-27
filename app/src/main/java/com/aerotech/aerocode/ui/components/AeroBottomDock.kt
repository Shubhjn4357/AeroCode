package com.aerotech.aerocode.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerotech.aerocode.ui.theme.AeroTheme

@Composable
fun AeroFloatingGlassDock(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    isFabExpanded: Boolean,
    onFabToggle: () -> Unit,
    onCreateProjectClick: () -> Unit,
    onImportTemplateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AeroTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expandable Quick Action Frosted Glass Menu
        AnimatedVisibility(
            visible = isFabExpanded,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                    shrinkVertically(spring(stiffness = Spring.StiffnessMedium))
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 14.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, colors.borderGlass, RoundedCornerShape(26.dp))
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = colors.primary.copy(alpha = 0.25f)
                    ),
                color = colors.surfaceGlass,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(230.dp)
                ) {
                    DockActionItem(
                        icon = Icons.Default.Add,
                        label = "Create Project",
                        onClick = {
                            onFabToggle()
                            onCreateProjectClick()
                        }
                    )
                    DockActionItem(
                        icon = Icons.Default.RocketLaunch,
                        label = "Starter Templates",
                        onClick = {
                            onFabToggle()
                            onImportTemplateClick()
                        }
                    )
                    DockActionItem(
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

        // Pill-Shaped Frosted Animated Glass Dock Bar
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    spotColor = colors.primary.copy(alpha = 0.22f),
                    ambientColor = colors.primary.copy(alpha = 0.10f)
                )
                .clip(CircleShape)
                .border(1.dp, colors.borderGlass, CircleShape),
            color = colors.surfaceGlass,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Home Pill
                DockAnimatedPillButton(
                    selected = currentDestination == NavDestination.HOME,
                    onClick = { onNavigate(NavDestination.HOME) },
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    label = "Home",
                    testTag = "nav_home"
                )

                // Projects Pill
                DockAnimatedPillButton(
                    selected = currentDestination == NavDestination.PROJECTS,
                    onClick = { onNavigate(NavDestination.PROJECTS) },
                    selectedIcon = Icons.Filled.Folder,
                    unselectedIcon = Icons.Outlined.Folder,
                    label = "Projects",
                    testTag = "nav_projects"
                )

                // Center Action Pill Button (+)
                val rotation by animateFloatAsState(
                    targetValue = if (isFabExpanded) 45f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "dockFabRotation"
                )

                val fabScale by animateFloatAsState(
                    targetValue = if (isFabExpanded) 1.08f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "dockFabScale"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(48.dp)
                        .scale(fabScale)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = onFabToggle
                        )
                        .testTag("dock_fab_plus"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Actions",
                        tint = if (colors.isDark) Color(0xFF042F2E) else Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation)
                    )
                }

                // Plugins Pill
                DockAnimatedPillButton(
                    selected = currentDestination == NavDestination.PLUGINS,
                    onClick = { onNavigate(NavDestination.PLUGINS) },
                    selectedIcon = Icons.Filled.Extension,
                    unselectedIcon = Icons.Outlined.Extension,
                    label = "Plugins",
                    testTag = "nav_plugins"
                )

                // Settings Pill
                DockAnimatedPillButton(
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
private fun DockAnimatedPillButton(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    testTag: String
) {
    val colors = AeroTheme.colors

    val pillBackground by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pillBg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.textSecondary.copy(alpha = 0.75f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconTint"
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconScale"
    )

    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(pillBackground)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) colors.primary.copy(alpha = 0.35f) else Color.Transparent,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = colors.primary),
                onClick = onClick
            )
            .padding(horizontal = if (selected) 12.dp else 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier
                .size(20.dp)
                .scale(scale)
        )

        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                    shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = colors.primary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DockActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = AeroTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary
        )
    }
}
