package com.example.notes_taking.Screens.presentations.Onboarding

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
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
    viewModel: OnboardingViewModel, onFinish: () -> Unit, isRtl: Boolean
) {
    val page = onboardingPages[viewModel.currentPage]
    val isLastPage = viewModel.currentPage == onboardingPages.size - 1
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // خلفية متكيفة
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (viewModel.isLoading) 0.3f else 1f)
                    .padding(bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ترويسة التطبيق
                Text(
                    text = stringResource(R.string.intellectual_sanctuary),
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.primary
                    ),
                    modifier = Modifier.padding(top = 48.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OnboardingImageSection(imageRes = page.imageRes)

                Spacer(modifier = Modifier.height(40.dp))

                OnboardingPagerIndicator(
                    pageSize = onboardingPages.size, currentPage = viewModel.currentPage
                )

                Spacer(modifier = Modifier.height(25.dp))

                OnboardingTextSection(
                    title = stringResource(page.titleRes),
                    description = stringResource(page.descRes)
                )

                Spacer(modifier = Modifier.height(18.dp))

                OnboardingActions(
                    isLastPage = isLastPage,
                    isLoading = viewModel.isLoading,
                    onNext = { viewModel.nextPage(isLastPage, onFinish) },
                    onSkip = { viewModel.skip(onFinish) })
            }

            if (viewModel.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
fun OnboardingImageSection(imageRes: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(240.dp)
        )
    }
}

@Composable
fun OnboardingPagerIndicator(pageSize: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageSize) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 8.dp, label = "dot_width"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
fun OnboardingTextSection(title: String, description: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            fontSize = 15.sp,
            fontFamily = ManropeFontFamily,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@Composable
fun OnboardingActions(
    isLastPage: Boolean, isLoading: Boolean, onNext: () -> Unit, onSkip: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onNext,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isLastPage) stringResource(R.string.get_started) else stringResource(
                        R.string.next
                    ), fontSize = 18.sp, fontFamily = MansalvaFontFamily
                )
                if (!isLastPage) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (!isLastPage) {
            TextButton(onClick = onSkip, enabled = !isLoading) {
                Text(
                    stringResource(R.string.skip),
                    fontSize = 16.sp,
                    fontFamily = ManropeFontFamily,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.loading),
                    fontFamily = ManropeFontFamily,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}