package com.example.ui.theme

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
    primary = PurpleAccent,
    onPrimary = Charcoal950,
    primaryContainer = PurpleDark,
    onPrimaryContainer = PurpleGlow,
    secondary = IndigoAccent,
    onSecondary = TextPrimary,
    secondaryContainer = Charcoal700,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldSuccess,
    onTertiary = Charcoal950,
    background = Charcoal950,
    onBackground = TextPrimary,
    surface = Charcoal900,
    onSurface = TextPrimary,
    surfaceVariant = Charcoal800,
    onSurfaceVariant = TextSecondary,
    outline = Charcoal600,
    outlineVariant = DividerColor,
    error = RoseStrict,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = PurpleDark,
    onPrimary = TextPrimary,
    primaryContainer = PurpleGlow,
    onPrimaryContainer = Charcoal950,
    secondary = IndigoAccent,
    onSecondary = TextPrimary,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    tertiary = EmeraldSuccess,
    onTertiary = TextPrimary,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = TextMuted,
    outlineVariant = DividerColor,
    error = RoseStrict,
    onError = TextPrimary
)

@Composable
fun FocusLockTheme(
    darkTheme: Boolean = true, // Default to premium dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
