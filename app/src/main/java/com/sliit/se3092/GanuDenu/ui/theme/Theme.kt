package com.sliit.se3092.GanuDenu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CornYellowPrimary,
    onPrimary = CornYellowOnPrimary,
    primaryContainer = CornYellowContainer,
    onPrimaryContainer = CornYellowOnContainer,
    secondary = CornYellowPrimary,
    onSecondary = CornYellowOnPrimary,
    background = PureBlack,
    onBackground = PureWhite,
    surface = DarkSurface1,
    onSurface = PureWhite,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = LightGray
)

private val LightColorScheme = lightColorScheme(
    primary = CornYellowPrimary,
    onPrimary = CornYellowOnPrimary,
    primaryContainer = CornYellowContainer,
    onPrimaryContainer = CornYellowOnContainer,
    background = PureBlack, // GanuDenu enforces an OLED-focused dark theme by default
    surface = DarkSurface1
)

@Composable
fun GanuDenuTheme(
    darkTheme: Boolean = true, // Default to true for the Pure Black & Corn Yellow design
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PureBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}