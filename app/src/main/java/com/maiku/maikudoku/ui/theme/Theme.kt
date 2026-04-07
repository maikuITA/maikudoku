package com.maiku.maikudoku.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Sage80,
    onPrimary = Ink,
    primaryContainer = SageContainer80,
    onPrimaryContainer = Ink,
    secondary = Clay80,
    onSecondary = Ink,
    secondaryContainer = Color(0xFF4A3022),
    onSecondaryContainer = Color(0xFFF3DDCF),
    tertiary = Color(0xFF8FA3A8),
    background = SurfaceDark,
    onBackground = Color(0xFFE6E4DE),
    surface = SurfaceDark,
    onSurface = Color(0xFFE6E4DE),
    surfaceVariant = Color(0xFF3A423E),
    outline = Color(0xFF8B948E),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

private val LightColorScheme = lightColorScheme(
    primary = Sage40,
    onPrimary = Color.White,
    primaryContainer = SageContainer80,
    onPrimaryContainer = Ink,
    secondary = Clay40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1DDD0),
    onSecondaryContainer = Ink,
    tertiary = Color(0xFF6F7D84),
    background = Ivory,
    onBackground = Ink,
    surface = Ivory,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE7E1D8),
    outline = Color(0xFF7F8A83),
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun MaikudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color available on Android 12+ (API 31+).
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