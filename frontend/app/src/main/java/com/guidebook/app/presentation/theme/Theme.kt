package com.guidebook.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    // Primary — Rich Indigo
    primary              = Indigo600,
    onPrimary            = White,
    primaryContainer     = Indigo100,
    onPrimaryContainer   = Indigo900,

    // Secondary — Coral Warm
    secondary            = Coral500,
    onSecondary          = White,
    secondaryContainer   = Coral100,
    onSecondaryContainer = Coral900,

    // Tertiary — Sky
    tertiary             = Sky500,
    onTertiary           = White,
    tertiaryContainer    = Color(0xFFE0F2FE),
    onTertiaryContainer  = Sky900,

    // Backgrounds & Surfaces
    background           = Slate50,
    onBackground         = NearBlack,
    surface              = White,
    onSurface            = NearBlack,
    surfaceVariant       = Slate100,
    onSurfaceVariant     = Slate600,

    // Borders
    outline              = Slate200,
    outlineVariant       = Color(0xFFEEEDF8),

    // Error
    error                = ErrorRed,
    onError              = White,
    errorContainer       = ErrorRedLight,
    onErrorContainer     = Color(0xFF7A0D0D),

    // Inverse
    inverseSurface       = Slate800,
    inverseOnSurface     = Slate100,
    inversePrimary       = Indigo300,

    scrim                = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    // Primary
    primary              = Indigo300,
    onPrimary            = Indigo900,
    primaryContainer     = Indigo700,
    onPrimaryContainer   = Indigo100,

    // Secondary — Coral
    secondary            = Coral400,
    onSecondary          = Coral900,
    secondaryContainer   = Coral700,
    onSecondaryContainer = Coral100,

    // Tertiary — Sky
    tertiary             = Sky400,
    onTertiary           = Sky900,
    tertiaryContainer    = Color(0xFF0C4A6E),
    onTertiaryContainer  = Color(0xFFBAE6FD),

    // Backgrounds & Surfaces
    background           = Slate950,
    onBackground         = Color(0xFFF0EFF8),
    surface              = Slate900,
    onSurface            = Color(0xFFE8E6F8),
    surfaceVariant       = Slate850,
    onSurfaceVariant     = Slate300,

    // Borders
    outline              = Color(0xFF2E2B50),
    outlineVariant       = Slate800,

    // Error
    error                = ErrorRedDark,
    onError              = Color(0xFF7A0D0D),
    errorContainer       = Color(0xFF7A0D0D),
    onErrorContainer     = Color(0xFFFFCCCC),

    // Inverse
    inverseSurface       = Slate200,
    inverseOnSurface     = Slate800,
    inversePrimary       = Indigo600,

    scrim                = Color(0xFF000000)
)

@Composable
fun GuidebookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = AppShapes,
        content     = content
    )
}
