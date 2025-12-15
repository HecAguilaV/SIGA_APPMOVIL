package com.example.sigaapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,              // #03045E - Azul oscuro
    onPrimary = White,                  // Texto sobre primario
    primaryContainer = AccentCyan,      // Contenedor primario
    onPrimaryContainer = White,         // Texto sobre contenedor primario
    secondary = AccentCyan,             // #00B4D8 - Azul claro
    onSecondary = White,                // Texto sobre secundario
    secondaryContainer = AccentTurquoise, // #80FFDB - Turquesa
    onSecondaryContainer = PrimaryDark,  // Texto sobre contenedor secundario
    tertiary = AccentTurquoise,         // Turquesa como terciario
    onTertiary = PrimaryDark,           // Texto sobre terciario
    background = Background,            // Fondo de la app
    onBackground = TextPrimary,         // Texto sobre fondo
    surface = SurfaceLight,             // Superficie de cards
    onSurface = TextPrimary,            // Texto sobre superficie
    surfaceVariant = Color(0xFFE8F4F8), // Variante de superficie
    onSurfaceVariant = TextSecondary,  // Texto sobre variante
    error = AlertRed,                   // Color de error
    onError = White,                    // Texto sobre error
    errorContainer = Color(0xFFFFEBEE), // Contenedor de error
    onErrorContainer = AlertRed          // Texto sobre contenedor de error
)

@Composable
fun SIGAAPPTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // Status bar con texto claro sobre fondo oscuro
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
