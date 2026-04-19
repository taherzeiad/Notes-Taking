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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes_taking.R
import com.example.notes_taking.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    val scale = remember { Animatable(0f) }
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {
            // أيقونة التطبيق
            Image(
                painter = painterResource(id = R.drawable.sticky_notes),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // اسم التطبيق
            Text(
                text = stringResource(R.string.splash_app_name),
                fontSize = 42.sp,
                fontFamily = MansalvaFontFamily,
                fontWeight = FontWeight.Normal,
                color = BrownColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // وصف التطبيق
            Text(
                text = stringResource(R.string.splash_subtitle),
                fontSize = 16.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Normal,
                color = BrownColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // النص السفلي
        Text(
            text = stringResource(R.string.splash_designed_by),
            fontSize = 12.sp,
            fontFamily = ManropeFontFamily,
            color = BrownColor.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}