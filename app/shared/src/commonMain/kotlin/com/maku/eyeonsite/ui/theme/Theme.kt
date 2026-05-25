package com.maku.eyeonsite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = White,
    primaryContainer = Sand,
    onPrimaryContainer = DeepForest,
    secondary = SavannaGold,
    onSecondary = DeepForest,
    secondaryContainer = Sand,
    onSecondaryContainer = Ink,
    tertiary = SignalGreen,
    onTertiary = DeepForest,
    background = Ivory,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Ash,
    outline = Stone,
    error = AlertCoral,
    onError = White,
)

private val DarkColorScheme = darkColorScheme(
    primary = SignalGreen,
    onPrimary = DeepForest,
    primaryContainer = Forest,
    onPrimaryContainer = White,
    secondary = SavannaGold,
    onSecondary = DeepForest,
    secondaryContainer = Forest,
    onSecondaryContainer = Sand,
    tertiary = SignalGreen,
    onTertiary = DeepForest,
    background = DeepForest,
    onBackground = Sand,
    surface = Color(0xFF0A3828),
    onSurface = Sand,
    surfaceVariant = Color(0xFF0D4A32),
    onSurfaceVariant = Stone,
    outline = Ash,
    error = AlertCoral,
    onError = White,
)

@Composable
fun EyeOnSiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = eyeOnSiteTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
    )
}
