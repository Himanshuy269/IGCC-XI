package com.example.igccxi.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CricketGreenPrimary,
    onPrimary = CricketGreenOnPrimary,
    primaryContainer = CricketGreenContainer,
    onPrimaryContainer = CricketGreenOnContainer,
    secondary = CricketGold,
    onSecondary = CricketOnGold,
    secondaryContainer = CricketGoldContainer,
    onSecondaryContainer = CricketOnGoldContainer,
    tertiary = CricketNavy,
    onTertiary = CricketOnNavy,
    tertiaryContainer = CricketNavyContainer,
    onTertiaryContainer = CricketOnNavyContainer,
    background = CricketBackgroundLight,
    onBackground = CricketOnSurfaceLight,
    surface = CricketSurfaceLight,
    onSurface = CricketOnSurfaceLight,
    surfaceVariant = CricketSurfaceVariantLight,
    onSurfaceVariant = CricketOnSurfaceVariantLight,
    outline = CricketOutlineLight,
    outlineVariant = CricketOutlineVariantLight,
    error = CricketWicketRed,
    errorContainer = CricketWicketContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4ADE80),
    onPrimary = Color(0xFF052E16),
    primaryContainer = Color(0xFF14532D),
    onPrimaryContainer = Color(0xFFBBF7D0),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = Color(0xFF94A3B8),
    onTertiary = Color(0xFF0F172A),
    background = CricketBackgroundDark,
    onBackground = CricketOnSurfaceDark,
    surface = CricketSurfaceDark,
    onSurface = CricketOnSurfaceDark,
    surfaceVariant = CricketSurfaceVariantDark,
    onSurfaceVariant = CricketOnSurfaceVariantDark,
    outline = CricketOutlineDark,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

@Composable
fun IGCCXITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
