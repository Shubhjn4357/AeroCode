package com.aerotech.aerocode.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aerotech.aerocode.ui.theme.AeroTheme
import kotlinx.coroutines.launch

enum class AeroDrawerState {
    HIDDEN,
    PEEK,
    EXPANDED,
    FULL_SCREEN
}

@Composable
fun AeroDraggableBottomDrawer(
    state: AeroDrawerState,
    onStateChange: (AeroDrawerState) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (state == AeroDrawerState.HIDDEN) return

    val coroutineScope = rememberCoroutineScope()
    val colors = AeroTheme.colors

    // Scrim overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = { onStateChange(AeroDrawerState.HIDDEN) })
    )

    // Drawer Surface
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val targetHeightFraction = when (state) {
            AeroDrawerState.HIDDEN -> 0.0f
            AeroDrawerState.PEEK -> 0.35f
            AeroDrawerState.EXPANDED -> 0.65f
            AeroDrawerState.FULL_SCREEN -> 0.92f
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(targetHeightFraction)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = colors.surfaceGlass,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.textSecondary.copy(alpha = 0.4f))
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    if (delta < -20) {
                                        // Dragged UP
                                        if (state == AeroDrawerState.PEEK) onStateChange(AeroDrawerState.EXPANDED)
                                        else if (state == AeroDrawerState.EXPANDED) onStateChange(AeroDrawerState.FULL_SCREEN)
                                    } else if (delta > 20) {
                                        // Dragged DOWN
                                        if (state == AeroDrawerState.FULL_SCREEN) onStateChange(AeroDrawerState.EXPANDED)
                                        else if (state == AeroDrawerState.EXPANDED) onStateChange(AeroDrawerState.PEEK)
                                        else onStateChange(AeroDrawerState.HIDDEN)
                                    }
                                }
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                content()
            }
        }
    }
}
