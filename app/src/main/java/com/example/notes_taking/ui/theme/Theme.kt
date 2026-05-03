package com.example.notes_taking.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- الألوان الأساسية (Core Colors) ---
val PrimaryBrown = Color(0xFF5D4037) // بني دافئ ورصين
val PrimaryLight = Color(0xFF8B6B61)
val AccentTeal = Color(0xFF4DB6AC)   // لون مميز (للعناصر التفاعلية)

// --- درجات الفاتح (Light Theme Palette) ---
val WhiteBackground = Color(0xFFFFFBFA) // أبيض بلمسة دافئة جداً
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF211A18)
val TextSecondaryLight = Color(0xFF756E6C)
val OutlineLight = Color(0xFFD3C2BC)

// --- درجات الغامق (Dark Theme Palette) ---
val BlackBackground = Color(0xFF1A1210) // أسود بني عميق (أريح للعين من الأسود الصرف)
val SurfaceDark = Color(0xFF251D1B)    // لون الكروت في الغامق
val TextPrimaryDark = Color(0xFFF0E0DB)
val TextSecondaryDark = Color(0xFFA08D89)
val OutlineDark = Color(0xFF53433F)

// --- ألوان خاصة بحالات معينة ---
val ErrorRed = Color(0xFFBA1A1A)
val SuccessGreen = Color(0xFF386B01)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF35221B),
    primaryContainer = PrimaryBrown,
    onPrimaryContainer = Color(0xFFFFDBD1),

    secondary = AccentTeal,
    onSecondary = Color(0xFF003733),

    background = BlackBackground,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,

    surfaceVariant = Color(0xFF53433F),
    onSurfaceVariant = TextSecondaryDark,

    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBrown,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBD1),
    onPrimaryContainer = Color(0xFF3B0900),

    secondary = AccentTeal,
    onSecondary = Color.White,

    background = WhiteBackground,
    onBackground = TextPrimaryLight,

    surface = SurfaceLight,
    onSurface = TextPrimaryLight,

    surfaceVariant = Color(0xFFF5EEEB),
    onSurfaceVariant = TextSecondaryLight,

    outline = OutlineLight
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