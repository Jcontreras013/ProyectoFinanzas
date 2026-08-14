package com.proyectofinanzas.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrimaryLight = Color(0xFF1E3A6B)
private val PrimaryDark = Color(0xFF9DC2FF)

private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    secondary = Color(0xFF4A6FA5),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    secondary = Color(0xFFA8C6F0),
    error = Color(0xFFFFB4AB),
)

@Composable
fun SistemaContableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
