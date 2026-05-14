package com.example.notes_taking.Screens.presentations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes_taking.ui.theme.ManropeFontFamily

/**
 * TopBar موحد لجميع الشاشات
 *
 * @param title العنوان الظاهر في المنتصف
 * @param onSearchClick إذا كان null لن يظهر زر البحث (يُعوَّض بمسافة فارغة)
 */
@Composable
fun AppTopBar(
    title: String,
    onSearchClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // الجانب الأيسر: زر البحث أو مسافة بنفس الحجم
        if (onSearchClick != null) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // مسافة فارغة بنفس حجم الأيقونة لإبقاء العنوان في المنتصف
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(40.dp))
        }

        // العنوان في المنتصف دائماً
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )

        // الجانب الأيمن: أيقونة الكتاب دائماً
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(40.dp)
                .padding(8.dp)
        )
    }
}