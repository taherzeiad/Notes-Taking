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

// ============================================
// Light Mode Colors
// ============================================
val PrimaryBrown = Color(0xFF5D4037)
val PrimaryLight = Color(0xFFF2F2F7)

// ============================================
// Light Mode Colors
// ============================================
val SugaryWhite = Color(0xFFF2F2F7)    // ← خلفية الشاشات — أبيض بدرجة رمادية خفيفة
val SurfaceWhite = Color(0xFFFFFFFF)   // ← الكاردات — أبيض نقي
val SnowyWhite = Color(0xFFEFEFF4)     // ← surfaceVariant والتابات

val TextPrimaryLight = Color(0xFF211A18)
val TextSecondaryLight = Color(0xFF756E6C)
val OutlineLight = Color(0xFFD3C2BC)
val AirForceBlue = Color(0xFF005A9C)
val ErrorRed = Color(0xFFBA1A1A)

// ============================================
// Dark Mode Colors — متناسقة مع الـ Light
// ============================================
val DarkBackground = Color(0xFF1C1612)    // بني غامق جداً — يتناسب مع البني الفاتح
val DarkSurface = Color(0xFF26201D)       // سطح الكاردات
val DarkSurfaceVariant = Color(0xFF32281F) // للـ inputs والتابات
val DarkPrimary = Color(0xFFBB9085)       // بني فاتح مضيء للـ Dark
val DarkPrimaryContainer = Color(0xFF4A342D) // container غامق بني
val DarkSecondaryContainer = Color(0xFF3A2E28)
val DarkOnSecondaryContainer = Color(0xFFCCB8B1)
val DarkTextPrimary = Color(0xFFF2E4DF)
val DarkTextSecondary = Color(0xFFA89490)
val DarkOutline = Color(0xFF5C4840)

// ============================================
// Light Color Scheme
// ============================================
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBrown,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF3B0D05),

    secondary = PrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = SnowyWhite,
    onSecondaryContainer = TextSecondaryLight,

    tertiary = Color(0xFF6D5E4F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5EDE8),
    onTertiaryContainer = Color(0xFF251810),

    background = SugaryWhite,
    onBackground = TextPrimaryLight,

    surface = SurfaceWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = SnowyWhite,
    onSurfaceVariant = TextSecondaryLight,

    outline = OutlineLight,
    outlineVariant = Color(0xFFEDE0DB),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    inverseSurface = Color(0xFF362F2C),
    inverseOnSurface = Color(0xFFFEEDE8),
    inversePrimary = DarkPrimary,

    scrim = Color(0xFF000000)
)

// ============================================
// Dark Color Scheme — متناسق مع Light
// ============================================
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF3B1A12),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFFFDBD1),

    secondary = Color(0xFFA8897E),
    onSecondary = Color(0xFF2E1A14),
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    tertiary = Color(0xFF9C8678),
    onTertiary = Color(0xFF2A1C14),
    tertiaryContainer = Color(0xFF3D2D24),
    onTertiaryContainer = Color(0xFFD9C2B8),

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkOutline,
    outlineVariant = Color(0xFF3D2E28),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    inverseSurface = Color(0xFFF2E4DF),
    inverseOnSurface = Color(0xFF26201D),
    inversePrimary = PrimaryBrown,

    scrim = Color(0xFF000000)
)

// ============================================
// Theme
// ============================================
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
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}