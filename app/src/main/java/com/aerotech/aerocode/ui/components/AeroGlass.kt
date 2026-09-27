package com.aerotech.aerocode.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aerotech.aerocode.ui.theme.AeroTheme

@Composable
fun AeroAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = AeroTheme.colors
    val primary = colors.primary
    val secondary = colors.secondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .drawBehind {
                // Subtle ambient top-right glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primary.copy(alpha = if (colors.isDark) 0.12f else 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.15f),
                        radius = size.width * 0.7f
                    )
                )
                // Subtle ambient bottom-left glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondary.copy(alpha = if (colors.isDark) 0.08f else 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.85f),
                        radius = size.width * 0.65f
                    )
                )
            },
        content = content
    )
}

@Composable
fun AeroGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = AeroTheme.shapes.card,
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colors = AeroTheme.colors
    val surfaceColor = backgroundColor ?: colors.surfaceGlass
    val actualBorderColor = borderColor ?: colors.borderGlass

    Surface(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, actualBorderColor, shape),
        color = surfaceColor,
        shape = shape,
        content = content
    )
}

@Composable
fun AeroGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = AeroTheme.shapes.card,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = AeroTheme.colors
    val baseModifier = modifier
        .clip(shape)
        .border(borderWidth, colors.borderGlass, shape)

    val finalModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Surface(
        modifier = finalModifier,
        color = colors.cardBackground.copy(alpha = if (colors.isDark) 0.85f else 0.95f),
        shape = shape,
        content = content
    )
}
