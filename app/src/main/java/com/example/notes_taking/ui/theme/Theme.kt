package com.example.notes_taking.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ألوان الوضع المظلم
// 1. تعريف ألوان الوضع المظلم
private val DarkColorScheme = darkColorScheme(
    primary = BrownCard,
    background = Color(0xFF121212), // خلفية داكنة بدلاً من PageBackground
    surface = Color(0xFF1E1E1E),    // لون الكروت في الوضع المظلم
    onBackground = Color.White,     // لون النصوص الأساسية
    onSurface = Color.White         // لون النصوص داخل الكروت
)

// 2. تعريف ألوان الوضع الفاتح
private val LightColorScheme = lightColorScheme(
    primary = BrownCard,
    background = PageBackground,
    surface = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun NotesTakingTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}