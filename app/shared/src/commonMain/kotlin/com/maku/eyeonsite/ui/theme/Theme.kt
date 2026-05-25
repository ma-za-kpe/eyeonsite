package com.maku.eyeonsite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// ── Client — warm, premium, trust-signalling ─────────────────────────────────

private val ClientDarkColors =
    darkColorScheme(
        primary = Forest,
        onPrimary = White,
        primaryContainer = Color(0xFF0D4A32),
        onPrimaryContainer = Sand,
        secondary = SavannaGold,
        onSecondary = DeepForest,
        secondaryContainer = Color(0xFF3A2A00),
        onSecondaryContainer = Sand,
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

private val ClientLightColors =
    lightColorScheme(
        primary = Forest,
        onPrimary = White,
        primaryContainer = Sand,
        onPrimaryContainer = DeepForest,
        secondary = SavannaGold,
        onSecondary = DeepForest,
        secondaryContainer = Sand,
        onSecondaryContainer = Ink,
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

// ── Inspector — operational, high-contrast, field-ready ──────────────────────

private val InspectorDarkColors =
    darkColorScheme(
        primary = CautionAmber,
        onPrimary = Ink,
        primaryContainer = Color(0xFF3A2800),
        onPrimaryContainer = Sand,
        secondary = Forest,
        onSecondary = White,
        secondaryContainer = Color(0xFF0D4A32),
        onSecondaryContainer = Sand,
        background = Ink,
        onBackground = White,
        surface = Color(0xFF2A2A27),
        onSurface = White,
        surfaceVariant = Color(0xFF363633),
        onSurfaceVariant = Stone,
        outline = Ash,
        error = AlertCoral,
        onError = White,
    )

private val InspectorLightColors =
    lightColorScheme(
        primary = Forest,
        onPrimary = White,
        primaryContainer = Sand,
        onPrimaryContainer = DeepForest,
        secondary = CautionAmber,
        onSecondary = Ink,
        secondaryContainer = Color(0xFFFFF3CD),
        onSecondaryContainer = Ink,
        background = White,
        onBackground = Ink,
        surface = Ivory,
        onSurface = Ink,
        surfaceVariant = Sand,
        onSurfaceVariant = Ash,
        outline = Stone,
        error = AlertCoral,
        onError = White,
    )

@Composable
fun EyeOnSiteTheme(
    variant: AppVariant = AppVariant.CLIENT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when (variant) {
            AppVariant.CLIENT -> if (darkTheme) ClientDarkColors else ClientLightColors
            AppVariant.INSPECTOR -> if (darkTheme) InspectorDarkColors else InspectorLightColors
        }
    val typography =
        when (variant) {
            AppVariant.CLIENT -> eyeOnSiteTypography()
            AppVariant.INSPECTOR -> inspectorTypography()
        }

    CompositionLocalProvider(LocalAppVariant provides variant) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}
