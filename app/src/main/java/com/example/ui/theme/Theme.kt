package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppGreenLight,
    onPrimary = Slate900,
    primaryContainer = WhatsAppGreenDark,
    onPrimaryContainer = Color.White,
    secondary = AiViolet,
    onSecondary = Color.White,
    secondaryContainer = AiVioletDark,
    onSecondaryContainer = AiVioletLight,
    background = Color(0xFF0B141A),
    surface = Color(0xFF111B21),
    surfaceVariant = Color(0xFF202C33),
    onBackground = Color(0xFFE9EDEF),
    onSurface = Color(0xFFE9EDEF),
    onSurfaceVariant = Slate400,
    outline = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1F4E0),
    onPrimaryContainer = Color(0xFF00382B),
    secondary = AiViolet,
    onSecondary = Color.White,
    secondaryContainer = AiVioletLight,
    onSecondaryContainer = AiVioletDark,
    background = Color(0xFFF0F2F5),
    surface = Color.White,
    surfaceVariant = Slate100,
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate600,
    outline = Slate200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
