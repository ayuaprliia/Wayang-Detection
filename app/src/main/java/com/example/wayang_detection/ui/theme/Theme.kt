package com.example.wayang_detection.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================
// WayangVision Theme — Always Dark, Gold Accent
// No light theme. No dynamic color. Pure premium dark.
// ============================================================

private val WayangDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = BgPrimary,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = Indigo,
    onSecondary = TextPrimary,
    secondaryContainer = BgElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = Emerald,
    onTertiary = BgPrimary,
    tertiaryContainer = BgElevated,
    onTertiaryContainer = Emerald,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgSecondary,
    onSurface = TextPrimary,
    surfaceVariant = BgElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = BgPrimary,
    surfaceContainerLow = BgSecondary,
    surfaceContainer = BgSecondary,
    surfaceContainerHigh = BgElevated,
    surfaceContainerHighest = BgOverlay,
    outline = TextMuted,
    outlineVariant = BgOverlay,
    error = Coral,
    onError = TextPrimary,
    errorContainer = Color(0xFF4A1C1C),
    onErrorContainer = Coral,
    inverseSurface = TextPrimary,
    inverseOnSurface = BgPrimary,
    inversePrimary = GoldDark,
    scrim = Color(0xFF000000)
)

@Composable
fun WayangDetectionTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = BgPrimary.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = BgPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = WayangDarkColorScheme,
        typography = Typography,
        content = content
    )
}