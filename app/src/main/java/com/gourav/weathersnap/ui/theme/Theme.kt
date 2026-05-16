package com.gourav.weathersnap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WeatherPrimary,
    onPrimary = WeatherPrimaryDark,
    secondary = WeatherTeal,
    tertiary = WeatherAmber,
    background = WeatherBackground,
    onBackground = WeatherText,
    surface = WeatherSurface,
    onSurface = WeatherText,
    surfaceVariant = WeatherSurfaceDark,
    onSurfaceVariant = WeatherMutedText,
)

@Composable
fun WeatherSnapTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
