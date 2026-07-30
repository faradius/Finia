package com.devmastercrack.finia.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FiniaLightColorScheme = lightColorScheme(
    primary = FiniaColors.Accent,
    onPrimary = FiniaColors.White,
    primaryContainer = FiniaColors.AccentSoft,
    onPrimaryContainer = FiniaColors.Accent,
    error = FiniaColors.Danger,
    onError = FiniaColors.White,
    errorContainer = FiniaColors.DangerSoft,
    onErrorContainer = FiniaColors.Danger,
    background = FiniaColors.ScreenBg,
    onBackground = FiniaColors.TextPrimary,
    surface = FiniaColors.White,
    onSurface = FiniaColors.TextPrimary,
    surfaceVariant = FiniaColors.SurfaceNeutral,
    onSurfaceVariant = FiniaColors.TextSecondary,
    outline = FiniaColors.BorderSubtle,
    outlineVariant = FiniaColors.BorderSubtle2,
)

// The design spec is hifi light-only; keep dark mode non-broken without inventing new tokens.
private val FiniaDarkColorScheme = darkColorScheme(
    primary = FiniaColors.Accent,
    onPrimary = Color.White,
    error = FiniaColors.Danger,
    onError = Color.White,
)

@Composable
fun FiniaTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) FiniaDarkColorScheme else FiniaLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
