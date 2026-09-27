package com.aerotech.aerocode.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AccentTheme(val label: String, val primary: Color, val secondary: Color) {
    STUDIO_BLUE("Android Studio Blue", Color(0xFF3574F0), Color(0xFF56A8F5)),
    ANDROID_GREEN("Android Green", Color(0xFF499C54), Color(0xFF3574F0)),
    CYAN("Aero Cyan", Color(0xFF38BDF8), Color(0xFF818CF8)),
    PURPLE("Aero Purple", Color(0xFFA855F7), Color(0xFF6366F1)),
    AMBER("Aero Amber", Color(0xFFF59E0B), Color(0xFFEF4444)),
    MONO("Aero Mono", Color(0xFFE2E8F0), Color(0xFF94A3B8))
}

@Immutable
data class AeroColors(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val surfaceGlass: Color,
    val borderGlass: Color,
    val highlightGlass: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val isDark: Boolean
)

@Immutable
data class AeroDimensions(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val touchTarget: Dp = 48.dp,
    val bottomNavHeight: Dp = 68.dp,
    val glassBorderWidth: Dp = 1.dp
)

@Immutable
data class AeroShapes(
    val chip: RoundedCornerShape = RoundedCornerShape(8.dp),
    val small: RoundedCornerShape = RoundedCornerShape(10.dp),
    val card: RoundedCornerShape = RoundedCornerShape(18.dp),
    val dialog: RoundedCornerShape = RoundedCornerShape(24.dp),
    val button: RoundedCornerShape = RoundedCornerShape(12.dp),
    val bottomSheet: RoundedCornerShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(50)
)

@Immutable
data class AeroMotion(
    val fast: Int = 150,
    val normal: Int = 250,
    val slow: Int = 400
)

val LocalAeroColors = compositionLocalOf {
    AeroColors(
        primary = Color(0xFF3574F0),
        secondary = Color(0xFF56A8F5),
        accent = Color(0xFF3574F0),
        background = Color(0xFF1E1F22),
        surface = Color(0xFF2B2D30),
        surfaceGlass = Color(0xF22B2D30),
        borderGlass = Color(0xFF393B40),
        highlightGlass = Color(0x1AFFFFFF),
        cardBackground = Color(0xFF2B2D30),
        textPrimary = Color(0xFFDFE1E5),
        textSecondary = Color(0xFF868A91),
        isDark = true
    )
}

val LocalAeroDimensions = staticCompositionLocalOf { AeroDimensions() }
val LocalAeroShapes = staticCompositionLocalOf { AeroShapes() }
val LocalAeroMotion = staticCompositionLocalOf { AeroMotion() }

object AeroTheme {
    val colors: AeroColors
        @Composable get() = LocalAeroColors.current

    val dimensions: AeroDimensions
        @Composable get() = LocalAeroDimensions.current

    val shapes: AeroShapes
        @Composable get() = LocalAeroShapes.current

    val motion: AeroMotion
        @Composable get() = LocalAeroMotion.current
}

// Android Studio Native Syntax Colors
val SyntaxKeyword = Color(0xFFCF8E6D)
val SyntaxFunction = Color(0xFF56A8F5)
val SyntaxString = Color(0xFF6A8759)
val SyntaxNumber = Color(0xFF2AACB8)
val SyntaxType = Color(0xFF4EC9B0)
val SyntaxComment = Color(0xFF808080)
val SyntaxAnnotation = Color(0xFFBBB529)

// XML Syntax Colors
val SyntaxXmlTag = Color(0xFFE8BF6A)
val SyntaxXmlAttribute = Color(0xFFBABABA)
val SyntaxXmlValue = Color(0xFF6A8759)

val AeroCyan = Color(0xFF38BDF8)
val AeroIndigo = Color(0xFF818CF8)
val AeroEmerald = Color(0xFF499C54)
val AeroAmber = Color(0xFFFBBF24)
val AeroRose = Color(0xFFF87171)

val DarkBg = Color(0xFF1E1F22)
val DarkSurface = Color(0xFF2B2D30)
val DarkSurfaceVariant = Color(0xFF393B40)
val DarkBorder = Color(0xFF4E5157)

@Composable
fun AeroCodeTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accent: AccentTheme = AccentTheme.STUDIO_BLUE,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val primaryColor = accent.primary
    val secondaryColor = accent.secondary

    val aeroColors = if (isDark) {
        AeroColors(
            primary = primaryColor,
            secondary = secondaryColor,
            accent = primaryColor,
            background = Color(0xFF1E1F22),
            surface = Color(0xFF2B2D30),
            surfaceGlass = Color(0xF22B2D30),
            borderGlass = Color(0xFF393B40),
            highlightGlass = Color(0x14FFFFFF),
            cardBackground = Color(0xFF2B2D30),
            textPrimary = Color(0xFFDFE1E5),
            textSecondary = Color(0xFF868A91),
            isDark = true
        )
    } else {
        AeroColors(
            primary = primaryColor,
            secondary = secondaryColor,
            accent = primaryColor,
            background = Color(0xFFF7F8FA),
            surface = Color(0xFFFFFFFF),
            surfaceGlass = Color(0xF2FFFFFF),
            borderGlass = Color(0xFFD1D5DB),
            highlightGlass = Color(0x33FFFFFF),
            cardBackground = Color(0xFFF3F4F6),
            textPrimary = Color(0xFF1F2328),
            textSecondary = Color(0xFF656D76),
            isDark = false
        )
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color(0xFFDFE1E5),
            secondary = secondaryColor,
            background = aeroColors.background,
            onBackground = aeroColors.textPrimary,
            surface = aeroColors.surface,
            onSurface = aeroColors.textPrimary,
            surfaceVariant = Color(0xFF393B40),
            onSurfaceVariant = aeroColors.textSecondary,
            outline = aeroColors.borderGlass
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = Color(0xFF1F2328),
            secondary = secondaryColor,
            background = aeroColors.background,
            onBackground = aeroColors.textPrimary,
            surface = aeroColors.surface,
            onSurface = aeroColors.textPrimary,
            surfaceVariant = Color(0xFFF3F4F6),
            onSurfaceVariant = aeroColors.textSecondary,
            outline = Color(0xFFD1D5DB)
        )
    }

    CompositionLocalProvider(
        LocalAeroColors provides aeroColors,
        LocalAeroDimensions provides AeroDimensions(),
        LocalAeroShapes provides AeroShapes(),
        LocalAeroMotion provides AeroMotion()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
