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
    primary        = Green900,
    onPrimary      = OnPrimary,
    secondary      = Green700,
    onSecondary    = OnPrimary,
    tertiary       = Teal200,
    background     = Background,
    surface        = Surface,
    error          = Error
)

private val DarkColorScheme = darkColorScheme(
    primary        = Green200,
    onPrimary      = Green900,
    secondary      = Green400,
    onSecondary    = Green900,
    tertiary       = Teal400,
    background     = Color(0xFF121212),
    surface        = Color(0xFF1E1E1E),
    error          = Color(0xFFCF6679)
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
        content     = content
    )
}
