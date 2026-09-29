package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CosmicBlack,
    primaryContainer = CosmicSurface,
    onPrimaryContainer = NeonCyan,
    secondary = NeonGold,
    onSecondary = CosmicBlack,
    secondaryContainer = CosmicCard,
    onSecondaryContainer = NeonGold,
    tertiary = NeonPurple,
    onTertiary = CosmicBlack,
    background = CosmicBlack,
    onBackground = TextPrimary,
    surface = CosmicDark,
    onSurface = TextPrimary,
    surfaceVariant = CosmicSurface,
    onSurfaceVariant = TextSecondary,
    outline = CosmicCardBorder,
    error = NeonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CosmicColorScheme,
        typography = Typography,
        content = content
    )
}
