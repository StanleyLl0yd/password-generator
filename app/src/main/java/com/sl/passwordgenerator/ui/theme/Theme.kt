package com.sl.passwordgenerator.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F5CD1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDFE0FF),
    onPrimaryContainer = Color(0xFF0A0F37),
    secondary = Color(0xFF5B5F72),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1E2F9),
    onSecondaryContainer = Color(0xFF171A2C),
    tertiary = Color(0xFF7A5267),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E7),
    onTertiaryContainer = Color(0xFF301022),
    background = Color(0xFFF7F7FF),
    onBackground = Color(0xFF1B1B23),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B23),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF454654)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBDC2FF),
    onPrimary = Color(0xFF1D2878),
    primaryContainer = Color(0xFF353F90),
    onPrimaryContainer = Color(0xFFE0E2FF),
    secondary = Color(0xFFC4C6DD),
    onSecondary = Color(0xFF2E3042),
    secondaryContainer = Color(0xFF45495A),
    onSecondaryContainer = Color(0xFFE1E3FA),
    tertiary = Color(0xFFE9B8CF),
    onTertiary = Color(0xFF43263A),
    tertiaryContainer = Color(0xFF5B3C51),
    onTertiaryContainer = Color(0xFFFFD9EC),
    background = Color(0xFF11131A),
    onBackground = Color(0xFFE4E1EB),
    surface = Color(0xFF171A21),
    onSurface = Color(0xFFE4E1EB),
    surfaceVariant = Color(0xFF424653),
    onSurfaceVariant = Color(0xFFC5C6D9)
)

@Composable
fun PasswordGeneratorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
