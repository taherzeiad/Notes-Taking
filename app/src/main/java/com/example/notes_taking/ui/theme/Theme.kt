@file:Suppress("DEPRECATION")

package com.example.notes_taking.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


// --- الألوان الأساسية المشتركة ---
val PrimaryBrown = Color(0xFF5D4037)
val PrimaryLight = Color(0xFF8B6B61)
val AccentTeal = Color(0xFF4DB6AC)

// --- لوحة الوضع الفاتح (Light Palette) ---
val WhiteBackground = Color(0xFFFFFBFA)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF211A18)
val TextSecondaryLight = Color(0xFF756E6C)
val OutlineLight = Color(0xFFD3C2BC)
val SurfaceVariantLight = Color(0xFFF5EEEB)

// --- لوحة الوضع الغامق (Dark Palette) ---
val BlackBackground = Color(0xFF1A1210)
val SurfaceDark = Color(0xFF251D1B)
val TextPrimaryDark = Color(0xFFF0E0DB)
val TextSecondaryDark = Color(0xFFA08D89)
val OutlineDark = Color(0xFF53433F)
val SurfaceVariantDark = Color(0xFF3B2F2C)

val ErrorRed = Color(0xFFBA1A1A)
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
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = ErrorRed
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
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = ErrorRed
)
@Composable
fun NotesTakingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}