package com.example.notes_taking.Screens.presentations.Onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.example.notes_taking.R
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import com.example.notes_taking.ui.theme.OnboardingBackground
import com.example.notes_taking.ui.theme.OnboardingBrown
import com.example.notes_taking.ui.theme.OnboardingDot


// ======= Data =======
data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "سجّل أفكارك بذكاء",
        description = "دع الذكاء الاصطناعي ينظم خواطرك ويحولها إلى\nملاحظات منظمة بلمسة إبداعية.",
        imageRes = R.drawable.onboarding1
    ),
    OnboardingPage(
        title = "نظّم ملاحظاتك",
        description = "رتّب أفكارك وملاحظاتك بسهولة\nوابحث عنها في أي وقت.",
        imageRes = R.drawable.onboarding1
    ),
    OnboardingPage(
        title = "استخراج المهام تلقائياً",
        description = "ببساطة، اكتب أفكارك. وسيقوم ذكاؤنا الاصطناعي بتحويل\n" +
                "ملاحظاتك إلى قائمة مهام منظمة وقابلة للتنفيذ.",
        imageRes = R.drawable.onboarding1
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val page = onboardingPages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ======= العنوان العلوي =======
            Text(
                text = "Intellectual Sanctuary",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = OnboardingBrown,
                modifier = Modifier.padding(top = 48.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ======= الصورة =======
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFF5E6D8)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ======= النقاط =======
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                onboardingPages.forEachIndexed { index, _ ->
                    val isSelected = index == currentPage
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 32.dp else 8.dp,
                        label = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) OnboardingBrown
                                else OnboardingDot
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ======= النص الرئيسي =======
            Text(
                text = page.title,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MansalvaFontFamily,
                color = OnboardingBrown,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ======= النص الفرعي =======
            Text(
                text = page.description,
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                color = OnboardingBrown.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // ======= زر التالي =======
            Button(
                onClick = {
                    if (currentPage < onboardingPages.size - 1) {
                        currentPage++
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnboardingBrown
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (currentPage < onboardingPages.size - 1) "التالي" else "ابدأ الآن",
                        fontSize = 18.sp,
                        fontFamily = MansalvaFontFamily,
                        color = Color.White
                    )
                    Text(
                        text = "←",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ======= زر تخطي =======
            if (currentPage < onboardingPages.size - 1) {
                TextButton(onClick = onFinish) {
                    Text(
                        text = "تخطي",
                        fontSize = 15.sp,
                        fontFamily = ManropeFontFamily,
                        color = OnboardingBrown.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}