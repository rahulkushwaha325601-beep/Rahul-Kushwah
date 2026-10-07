package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LibraryGold,
    onPrimary = LibraryNavyDark,
    primaryContainer = LibraryNavyLight,
    onPrimaryContainer = Color.White,
    secondary = LibraryAmberLight,
    onSecondary = Slate900,
    background = Color(0xFF0B1424),
    surface = Color(0xFF132038),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1B2A4A),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334B73),
    error = Color(0xFFF87171)
)

private val LightColorScheme = lightColorScheme(
    primary = LibraryNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = LibraryNavyDark,
    secondary = LibraryAmber,
    onSecondary = Color.White,
    secondaryContainer = LibraryAmberContainer,
    onSecondaryContainer = Color(0xFF78350F),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFEEF2F6),
    onSurfaceVariant = Slate700,
    outline = Color(0xFFCBD5E1),
    error = ErrorRed
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
        content = content
    )
}
