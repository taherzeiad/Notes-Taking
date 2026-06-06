package com.example.myuniqueapp.Screens.presentations.CreateNote

import androidx.compose.animation.*
import com.notestalking.myuniqueapp.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myuniqueapp.Screens.presentations.Editor.RateLimitState
import com.example.myuniqueapp.ui.theme.ManropeFontFamily

/**
 * يظهر فوق الـ Toolbar — يعرض عدد المحاولات المتبقية أو العداد التنازلي
 */
@Composable
fun AiRateLimitBadge(state: RateLimitState) {

    // لا تُظهر شيئاً إذا لم يُستخدم AI بعد
    if (state.usedCount == 0 && !state.isLimited) return

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (state.isLimited) colorScheme.errorContainer.copy(alpha = 0.9f)
                    else colorScheme.secondaryContainer.copy(alpha = 0.8f)
                )
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Outlined.Timer,
                contentDescription = null,
                tint = if (state.isLimited) colorScheme.error else colorScheme.secondary,
                modifier = Modifier.size(14.dp),
            )

            Text(
                text = if (state.isLimited) stringResource(
                    R.string.ai_rate_limit_wait,
                    state.secondsRemaining
                )
                else stringResource(R.string.ai_rate_limit_remaining, state.remainingCalls),
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (state.isLimited) colorScheme.error else colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )

            // أدوات صغيرة تعرض عدد الاستخدام
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(RateLimitState.MAX_CALLS) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (index < state.usedCount) {
                                    if (state.isLimited) colorScheme.error
                                    else colorScheme.primary
                                } else colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}