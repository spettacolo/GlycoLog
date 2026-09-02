package com.uni.glycolog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// tema scuro
private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = DarkBackground,
    secondary = GreenDark,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = LightSurface,
    surface = DarkSurface,
    onSurface = LightSurface,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = GrayText,
    outline = DarkBorder,
    error = RedAlert
)

private val LightColorScheme = lightColorScheme(
    primary = GreenDark,
    onPrimary = LightSurface,
    secondary = GreenPrimary,
    onSecondary = DarkBackground,
    background = LightBackground,
    onBackground = DarkBackground,
    surface = LightSurface,
    onSurface = DarkBackground,
    surfaceVariant = LightBorder,
    onSurfaceVariant = GrayTextLight,
    outline = LightBorder,
    error = RedAlert
)

@Composable
fun GlycoLogTheme(
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
