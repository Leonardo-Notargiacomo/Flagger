package com.fontys.frontend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)

// Badge Screen Theme - Warm brown and orange color scheme
private val BadgeLightColorScheme = lightColorScheme(
    primary = Color(0xFFD4956C),          // Warm orange/coral
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DCC4),  // Light cream/beige
    onPrimaryContainer = Color(0xFF5C4738), // Dark brown text
    secondary = Color(0xFFD4956C),         // Orange accent
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DCC4),
    onSecondaryContainer = Color(0xFF5C4738),
    background = Color(0xFFE8DCC4),        // Cream background
    onBackground = Color(0xFF5C4738),      // Dark brown text
    surface = Color(0xFF4A362A),           // Dark brown for header
    onSurface = Color(0xFFE8DCC4),         // Light text on dark surface
    surfaceVariant = Color(0xFFD4C5B0),    // Slightly darker cream
    onSurfaceVariant = Color(0xFF5C4738),
    outline = Color(0xFF5C4738),           // Dark brown borders
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun BadgeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BadgeLightColorScheme,
        content = content
    )
}
