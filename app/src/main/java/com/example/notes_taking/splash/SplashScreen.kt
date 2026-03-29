package com.example.notes_taking.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.notes_taking.ui.theme.BrownColor
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    // أنيميشن للـ Scale
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = EaseOutBack
            )
        )
        delay(2000)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAE8C8)), // لون الخلفية البيج
        contentAlignment = Alignment.Center
    ) {

        // المحتوى الرئيسي في المنتصف
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {

            // الأيقونة (استبدلها بـ Image إذا عندك ملف SVG/PNG)
            NotepadIcon()

            Spacer(modifier = Modifier.height(24.dp))

            // اسم التطبيق
            Text(
                text = "NotePad",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = BrownColor,
                fontFamily = FontFamily.Cursive // أو استخدم خط مخصص
            )

            Spacer(modifier = Modifier.height(8.dp))

            // الوصف
            Text(
                text = "Take Quick Notes",
                fontSize = 16.sp,
                color = BrownColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            )
        }

        // النص السفلي
        Text(
            text = "designed by srkdesignwala",
            fontSize = 12.sp,
            color = BrownColor.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}


// ======= أيقونة الـ Notepad بـ Canvas =======
@Composable
fun NotepadIcon() {
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(120.dp)
    ) {
        val brown = BrownColor

        // الورقة الخلفية (مائلة قليلاً)
        rotate(-10f) {
            drawRoundRect(
                color = brown,
                topLeft = androidx.compose.ui.geometry.Offset(30f, 40f),
                size = androidx.compose.ui.geometry.Size(200f, 240f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
            )
        }

        // الورقة الأمامية
        drawRoundRect(
            color = brown,
            topLeft = androidx.compose.ui.geometry.Offset(50f, 60f),
            size = androidx.compose.ui.geometry.Size(200f, 240f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
        )

        // خطوط النص داخل الورقة
        drawLine(
            color = brown,
            start = androidx.compose.ui.geometry.Offset(90f, 150f),
            end = androidx.compose.ui.geometry.Offset(210f, 150f),
            strokeWidth = 6f
        )
        drawLine(
            color = brown,
            start = androidx.compose.ui.geometry.Offset(90f, 190f),
            end = androidx.compose.ui.geometry.Offset(170f, 190f),
            strokeWidth = 6f
        )

        // الدبوس في الأعلى
        drawCircle(
            color = brown,
            radius = 18f,
            center = androidx.compose.ui.geometry.Offset(150f, 65f)
        )
        drawCircle(
            color = Color(0xFFFAE8C8), // لون الخلفية
            radius = 10f,
            center = androidx.compose.ui.geometry.Offset(150f, 65f)
        )
    }
}