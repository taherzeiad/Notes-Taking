package com.example.notes_taking.Screens.presentations.Onboarding

import OnboardingViewModel
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes_taking.R
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import com.example.notes_taking.ui.theme.OnboardingBackground
import com.example.notes_taking.ui.theme.OnboardingBrown
import com.example.notes_taking.ui.theme.OnboardingDot

// ======= Data =======
data class OnboardingPage(
    val titleRes: Int, val descRes: Int, val imageRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(R.string.ob_title_1, R.string.ob_desc_1, R.drawable.onboarding1),
    OnboardingPage(R.string.ob_title_2, R.string.ob_desc_2, R.drawable.onboarding1),
    OnboardingPage(R.string.ob_title_3, R.string.ob_desc_3, R.drawable.onboarding2)
)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit,
    isRtl: Boolean
) {
    val page = onboardingPages[viewModel.currentPage]
    val isLastPage = viewModel.currentPage == onboardingPages.size - 1

    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OnboardingBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (viewModel.isLoading) 0.3f else 1f),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                // ======= العنوان العلوي =======
                Text(
                    text = stringResource(R.string.intellectual_sanctuary),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ManropeFontFamily,
                    color = OnboardingBrown,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 48.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ======= الصورة =======
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
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

                // ======= Points =======
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    onboardingPages.forEachIndexed { index, _ ->
                        val isSelected = index == viewModel.currentPage
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 32.dp else 8.dp, label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) OnboardingBrown else OnboardingDot
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                // ======= النص الرئيسي =======
                Text(
                    text = stringResource(page.titleRes),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MansalvaFontFamily,
                    color = OnboardingBrown,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ======= النص الفرعي =======
                Text(
                    text = stringResource(page.descRes),
                    fontSize = 15.sp,
                    fontFamily = ManropeFontFamily,
                    color = OnboardingBrown.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 29.3.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ======= زر التالي =======
                Button(
                    onClick = { viewModel.nextPage(isLastPage, onFinish) },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OnboardingBrown)
                ) {
                    if (viewModel.isLoading && isLastPage) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (!isLastPage) stringResource(R.string.next)
                                else stringResource(R.string.get_started),
                                fontSize = 18.sp,
                                fontFamily = MansalvaFontFamily,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ======= زر تخطي =======
                if (!isLastPage) {
                    TextButton(
                        onClick = { viewModel.skip(onFinish) },
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color = OnboardingBrown,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.skip),
                                fontSize = 18.sp,
                                fontFamily = ManropeFontFamily,
                                color = OnboardingBrown.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
            // ======= Loading Overlay =======
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = OnboardingBrown,
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = stringResource(R.string.loading),
                                fontSize = 14.sp,
                                fontFamily = ManropeFontFamily,
                                color = OnboardingBrown.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}