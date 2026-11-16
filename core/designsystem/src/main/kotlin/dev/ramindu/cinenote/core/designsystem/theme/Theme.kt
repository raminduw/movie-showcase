package dev.ramindu.cinenote.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** "Cinema" palette: deep charcoal surfaces, a coral accent and a gold rating star. */
object CineColors {
    val Coral = Color(0xFFE8505B)
    val CoralDark = Color(0xFFFF6B74)
    val Gold = Color(0xFFF5B841)
    val Charcoal = Color(0xFF0F1115)
    val Graphite = Color(0xFF181B22)
    val Slate = Color(0xFF232733)
    val Cream = Color(0xFFFAF7F5)
}

private val DarkColors = darkColorScheme(
    primary = CineColors.CoralDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C1D23),
    onPrimaryContainer = Color(0xFFFFDADB),
    secondary = CineColors.Gold,
    onSecondary = Color(0xFF2A1F00),
    // Selected chips / nav indicator
    secondaryContainer = Color(0xFF5C1D23),
    onSecondaryContainer = Color(0xFFFFDADB),
    background = CineColors.Charcoal,
    onBackground = Color(0xFFE8E6EB),
    surface = CineColors.Charcoal,
    onSurface = Color(0xFFE8E6EB),
    surfaceVariant = CineColors.Slate,
    onSurfaceVariant = Color(0xFFB8B6C0),
    surfaceContainer = CineColors.Graphite,
    surfaceContainerHigh = CineColors.Slate,
    surfaceContainerLow = Color(0xFF14161C),
    outline = Color(0xFF4A4E5C),
)

private val LightColors = lightColorScheme(
    primary = CineColors.Coral,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDADB),
    onPrimaryContainer = Color(0xFF410009),
    secondary = Color(0xFFB7811A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDADB),
    onSecondaryContainer = Color(0xFF410009),
    background = CineColors.Cream,
    onBackground = Color(0xFF1C1B1F),
    surface = CineColors.Cream,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEFE8E6),
    onSurfaceVariant = Color(0xFF5A5558),
    surfaceContainer = Color(0xFFF3EEEC),
    surfaceContainerHigh = Color(0xFFEDE7E4),
    surfaceContainerLow = Color(0xFFF7F2F0),
    outline = Color(0xFFCAC2C0),
)

private val CineTypography = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp),
    )
}

@Composable
fun CineNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CineTypography,
        content = content,
    )
}
