package com.example.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF26C6DA), // Luminous Cyan-Teal for vibrant contrast on dark canvas
    onPrimary = Color(0xFF00363A),
    primaryContainer = PrimaryBrandDark,
    onPrimaryContainer = Color(0xFFB2EBF2),
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF00363A),
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceSubtle,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = StatusError,
    onError = Color.White
)

val LightColorScheme = lightColorScheme(
    primary = PrimaryBrand, // Deep Ocean Teal (0xFF007A87) with high contrast on white/slate
    onPrimary = Color.White,
    primaryContainer = PrimaryBrandContainer,
    onPrimaryContainer = PrimaryBrandDark,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceSubtle,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = StatusError,
    onError = Color.White
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Keep status/navigation bar icons readable when the in-app theme differs from the system one.
    val context = LocalContext.current
    DisposableEffect(darkTheme) {
        val barStyle = if (darkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        }
        (context as? ComponentActivity)?.enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        onDispose { }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
