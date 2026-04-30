package com.example.notes_taking.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ألوان الوضع المظلم
private val DarkColorScheme = darkColorScheme(
    primary = BrownCard,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    background = Color(0xFF1C1C1C),
    secondary = Color(0xFFB8A898)
)

private val LightColorScheme = lightColorScheme(
    primary = BrownCard,
    surface = Color.White,
    onSurface = Color.Black,
    background = Color.White,
    secondary = Color(0xFFF5F0EB)
)

@Composable
fun NotesTakingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}