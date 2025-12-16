package com.fontys.frontend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Flagger Light Theme - Warm vintage map aesthetic, clean execution
private val FlaggerLightColorScheme = lightColorScheme(
    primary = Color(0xFFE98D58),           // Warm orange - map accent
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBE3CD),  // Light cream - parchment
    onPrimaryContainer = Color(0xFF523735), // Dark brown text
    secondary = Color(0xFFE98D58),         // Warm orange accent
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4C5B0), // Darker cream
    onSecondaryContainer = Color(0xFF523735),
    tertiary = Color(0xFF8B6F47),          // Muted brown
    onTertiary = Color.White,
    background = Color(0xFFE8DCC4),        // Warm cream - aged parchment
    onBackground = Color(0xFF523735),      // Dark brown text
    surface = Color(0xFF4A362A),           // Dark brown - headers
    onSurface = Color(0xFFEBE3CD),         // Light cream on dark
    surfaceVariant = Color(0xFFD4C5B0),    // Darker cream variant
    onSurfaceVariant = Color(0xFF523735),  // Dark brown text
    outline = Color(0xFFC4B5A0),           // Subtle brown border
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

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

// Flagger Dark Theme - Warm brown dark mode with explorer aesthetic
private val FlaggerDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE98D58),           // Keep warm orange
    onPrimary = Color(0xFF1C1B1F),         // Dark text on orange
    primaryContainer = Color(0xFF5C4738),  // Darker brown container
    onPrimaryContainer = Color(0xFFE8DCC4), // Light cream text
    secondary = Color(0xFFE98D58),         // Warm orange accent
    onSecondary = Color(0xFF1C1B1F),
    secondaryContainer = Color(0xFF3D342E), // Medium brown
    onSecondaryContainer = Color(0xFFD4C5B0),
    tertiary = Color(0xFF8B6F47),          // Muted brown
    onTertiary = Color.White,
    background = Color(0xFF2D2420),        // Dark brown background
    onBackground = Color(0xFFE8DCC4),      // Light cream text
    surface = Color(0xFF3D342E),           // Lighter brown surface
    onSurface = Color(0xFFE8DCC4),         // Light cream text
    surfaceVariant = Color(0xFF4A362A),    // Medium dark brown
    onSurfaceVariant = Color(0xFFD4C5B0),  // Light text
    outline = Color(0xFF8B6F47),           // Muted brown borders
    error = Color(0xFFCF6679),             // Softer red for dark mode
    onError = Color(0xFF1C1B1F),
    errorContainer = Color(0xFF5C3030),    // Dark red container
    onErrorContainer = Color(0xFFF9DEDC),
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
    // Use Flagger theme with dark mode support
    val colorScheme = when {
        darkTheme -> FlaggerDarkColorScheme
        else -> FlaggerLightColorScheme
    }

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

@Composable
fun FlaggerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> FlaggerDarkColorScheme
        else -> FlaggerLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
