package com.sl.passwordgenerator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE1E5F2),
    onSurfaceVariant = Color(0xFF1B2838)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9FA8DA),
    onPrimary = Color(0xFF000000),
    background = Color(0xFF101218),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF141821),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1E2430),
    onSurfaceVariant = Color(0xFFE1E5F2)
)

@Composable
fun PasswordGeneratorTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}