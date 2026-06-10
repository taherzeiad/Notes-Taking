@file:Suppress("DEPRECATION")

package com.example.myuniqueapp.ui.theme

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
// Light Mode Colors — لا تغيير
// ============================================
val PrimaryBrown = Color(0xFF5D4037)
val PrimaryLight = Color(0xFFF2F2F7)
val SugaryWhite = Color(0xFFF2F2F7)
val SurfaceWhite = Color(0xFFFFFFFF)
val SnowyWhite = Color(0xFFEFEFF4)
val TextPrimaryLight = Color(0xFF211A18)
val TextSecondaryLight = Color(0xFF756E6C)
val OutlineLight = Color(0xFFD3C2BC)
val AirForceBlue = Color(0xFF005A9C)
val ErrorRed = Color(0xFFBA1A1A)

// ============================================
// Dark Mode Colors — محترفة وعصرية
// ============================================

// ← خلفية عميقة نظيفة — رمادي داكن محايد (مش بني)
val DarkBackground = Color(0xFF121212)

// ← سطح الكاردات — رمادي داكن مرتفع قليلاً
val DarkSurface = Color(0xFF1E1E1E)

// ← للـ inputs والتابات والـ surfaceVariant
val DarkSurfaceVariant = Color(0xFF2C2C2C)

// ← Primary محترف — بني دافئ مضيء (يتناسق مع الـ Light)
val DarkPrimary = Color(0xFFD4A574)

// ← Primary Container — داكن دافئ
val DarkPrimaryContainer = Color(0xFF3D2B1F)

// ← Secondary
val DarkSecondary = Color(0xFFBFA58A)
val DarkSecondaryContainer = Color(0xFF2A2218)
val DarkOnSecondaryContainer = Color(0xFFE0CEBC)

// ← Tertiary — بيج دافئ
val DarkTertiary = Color(0xFFA89070)
val DarkTertiaryContainer = Color(0xFF2E2318)

// ← نصوص
val DarkTextPrimary = Color(0xFFEDEDED)
val DarkTextSecondary = Color(0xFF9E9E9E)

// ← حدود
val DarkOutline = Color(0xFF3A3A3A)
val DarkOutlineVariant = Color(0xFF2A2A2A)

// ← Error
val DarkError = Color(0xFFCF6679)
val DarkErrorContainer = Color(0xFF3B1219)

// ============================================
// Light Color Scheme — لا تغيير إطلاقاً
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
// Dark Color Scheme — محترف وعصري
// ============================================
private val DarkColorScheme = darkColorScheme(
    // ← Primary: بني ذهبي دافئ مضيء
    primary = DarkPrimary,
    onPrimary = Color(0xFF1A0F08),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFFFDCBE),

    // ← Secondary: بيج دافئ
    secondary = DarkSecondary,
    onSecondary = Color(0xFF1A1208),
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    // ← Tertiary: أدفأ قليلاً
    tertiary = DarkTertiary,
    onTertiary = Color(0xFF1A1208),
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = Color(0xFFD9C4AC),

    // ← Backgrounds
    background = DarkBackground,
    onBackground = DarkTextPrimary,

    // ← Surfaces
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    // ← Outlines
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    // ← Error
    error = DarkError,
    onError = Color(0xFF1A0008),
    errorContainer = DarkErrorContainer,
    onErrorContainer = Color(0xFFFFB3C1),

    // ← Inverse
    inverseSurface = Color(0xFFEDEDED),
    inverseOnSurface = Color(0xFF1E1E1E),
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