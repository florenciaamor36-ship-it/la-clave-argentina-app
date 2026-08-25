package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF001F29),
    primaryContainer = CyberCyanDim,
    onPrimaryContainer = Color(0xFFE0F7FA),
    
    secondary = CyberEmerald,
    onSecondary = Color(0xFF00220F),
    secondaryContainer = CyberEmeraldDim,
    onSecondaryContainer = Color(0xFFE8F5E9),
    
    tertiary = CyberPurple,
    onTertiary = Color.White,
    tertiaryContainer = CyberPurpleDim,
    onTertiaryContainer = Color(0xFFEDE7F6),
    
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextGray,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E293B),
    error = CyberRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
