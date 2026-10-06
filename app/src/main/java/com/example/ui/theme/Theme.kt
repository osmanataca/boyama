package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = CoralDarkPrimary,
    onPrimary = Color(0xFF4D0C00),
    primaryContainer = Color(0xFF752113),
    onPrimaryContainer = CoralContainer,
    secondary = TealDarkSecondary,
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF00504B),
    onSecondaryContainer = TealContainer,
    tertiary = AmberDarkTertiary,
    onTertiary = Color(0xFF462A00),
    tertiaryContainer = Color(0xFF653E00),
    onTertiaryContainer = AmberContainer,
    background = DarkStudioBg,
    onBackground = DarkStudioInk,
    surface = DarkStudioSurface,
    onSurface = DarkStudioInk,
    surfaceVariant = DarkStudioCard,
    onSurfaceVariant = DarkStudioMuted,
    outline = Color(0xFF474261)
)

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralContainer,
    onPrimaryContainer = OnCoralContainer,
    secondary = TealSecondary,
    onSecondary = Color.White,
    secondaryContainer = TealContainer,
    onSecondaryContainer = OnTealContainer,
    tertiary = AmberTertiary,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,
    background = StudioCream,
    onBackground = StudioInk,
    surface = StudioSurface,
    onSurface = StudioInk,
    surfaceVariant = StudioPaper,
    onSurfaceVariant = StudioMutedInk,
    outline = Color(0xFFD6CFC2)
)

val StudioShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = StudioShapes,
        content = content
    )
}
