package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VyroColorScheme = darkColorScheme(
    primary = VyroVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VyroVioletDark,
    onPrimaryContainer = Color.White,
    secondary = VyroCyanSecondary,
    onSecondary = Color.Black,
    secondaryContainer = VyroCyanDark,
    onSecondaryContainer = Color.White,
    tertiary = VyroGoldTertiary,
    onTertiary = Color.Black,
    background = VyroBackground,
    onBackground = VyroTextPrimary,
    surface = VyroSurface,
    onSurface = VyroTextPrimary,
    surfaceVariant = VyroSurfaceElevated,
    onSurfaceVariant = VyroTextSecondary,
    outline = VyroBorder,
    error = VyroRose,
    onError = Color.White
)

@Composable
fun VyroTheme(
    darkTheme: Boolean = true, // VYRO defaults to immersive futuristic dark mode
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VyroColorScheme,
        typography = Typography,
        content = content
    )
}
