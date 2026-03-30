package com.example.notes_taking.Screens.presentations.Splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import kotlinx.coroutines.delay
import com.example.notes_taking.R
import com.example.notes_taking.ui.theme.BrownColor
import com.example.notes_taking.ui.theme.SplashBackground

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f, animationSpec = tween(durationMillis = 800)
        )
        delay(1000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {

        // المحتوى الرئيسي
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {

            // أيقونة الـ Notepad
            Image(
                painter = painterResource(id = R.drawable.sticky_notes),
                contentDescription = "NotePad Icon",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // اسم التطبيق بخط Mansalva
            Text(
                text = "NotePad",
                fontSize = 42.sp,
                fontFamily = MansalvaFontFamily,
                fontWeight = FontWeight.Normal,
                color = BrownColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // الوصف بخط Manrope
            Text(
                text = "Take Quick Notes",
                fontSize = 16.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Normal,
                color = BrownColor.copy(alpha = 0.7f)
            )
        }

        // النص السفلي بخط Manrope
        Text(
            text = "designed by srkdesignwala",
            fontSize = 12.sp,
            fontFamily = ManropeFontFamily,
            color = BrownColor.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}