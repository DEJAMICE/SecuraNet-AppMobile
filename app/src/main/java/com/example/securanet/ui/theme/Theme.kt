package com.example.securanet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color


// ── Light colour scheme (the app uses light-only for the MVP) ────────────────
private val SecuraNetLightColors = lightColorScheme(
    // Primary – navy
    primary             = NavyPrimary,
    onPrimary           = OnNavy,
    primaryContainer    = NavyPrimaryContainer,
    onPrimaryContainer  = OnNavy,

    // Secondary – softer navy
    secondary           = NavyPrimaryLight,
    onSecondary         = OnNavy,
    secondaryContainer  = Color(0xFFDDE5F4),
    onSecondaryContainer = NavyPrimary,

    // Error / alert – SOS red
    error               = SosRed,
    onError             = OnNavy,
    errorContainer      = SosRedLight,
    onErrorContainer    = SosRed,

    // Surfaces
    background          = SoftBackground,
    onBackground        = NavyPrimary,
    surface             = SurfaceWhite,
    onSurface           = NavyPrimary,
    surfaceVariant      = Color(0xFFECF0F4),
    onSurfaceVariant    = OnSurfaceVariantLight,
    outline             = Color(0xFFBCC4CF)
)

// ── Dark colour scheme (mirrors the brand; not heavily used in this MVP) ─────
private val SecuraNetDarkColors = darkColorScheme(
    primary             = NavyPrimaryLight,
    onPrimary           = OnNavy,
    primaryContainer    = NavyPrimary,
    onPrimaryContainer  = OnNavy,
    error               = SosRedLight,
    onError             = SosRed,
    errorContainer      = SosRed,
    onErrorContainer    = OnNavy,
    background          = Color(0xFF111827),
    onBackground        = OnNavy,
    surface             = Color(0xFF1C2A40),
    onSurface           = OnNavy
)

@Composable
fun SecuraNetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled – we always use our branded palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SecuraNetDarkColors else SecuraNetLightColors

    // Keep status-bar icons readable against the surface colour
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}