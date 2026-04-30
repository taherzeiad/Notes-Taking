package com.example.notes_taking.Screens.presentations.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.notes_taking.R
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.BrownCard
import com.example.notes_taking.ui.theme.CardBorder
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import com.example.notes_taking.ui.theme.TextPrimary
import com.example.notes_taking.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navController: NavHostController
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(navController = navController, selectedTab = 0)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1. الترويسة (Top Bar)
            item { SettingsTopBar() }

            // 2. عنوان الصفحة
            item { SettingsHeader() }


            item {
                CustomizationSection(
                    isDarkMode = viewModel.isDarkModeEnabled,
                    onDarkModeChange = { viewModel.toggleDarkMode(it) }
                )
            }

            item {
                PrivacySection()
            }

        }
    }
}

// ======= مكونات فرعية (UI Components) =======
@Composable
fun CustomizationSection(isDarkMode: Boolean, onDarkModeChange: (Boolean) -> Unit) {
    SettingsSection(title = stringResource(R.string.section_customization)) {
        SettingsItemWithToggle(
            label = stringResource(R.string.item_dark_mode),
            subLabel = stringResource(R.string.sub_dark_mode),
            icon = Icons.Outlined.DarkMode,
            checked = isDarkMode,
            onCheckedChange = onDarkModeChange
        )
        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 16.dp))
        SettingsItem(
            label = stringResource(R.string.item_notifications),
            icon = Icons.Outlined.Notifications
        )
    }
}


// ======= Helper UI Components (Clean & Automated) =======
@Composable
fun ProfileAvatar(
    size: androidx.compose.ui.unit.Dp = 40.dp, iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(BrownCard),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ), elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(label: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        SettingsIconBox(icon)
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            fontSize = 15.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsItemWithToggle(
    label: String,
    subLabel: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subLabel,
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange, colors = switchColors()
        )
    }
}

@Composable
fun SettingsIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ), contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = BrownCard,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = Color(0xFFD0C8C0)
)

@Composable
fun SettingsTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar()
        Text(
            text = stringResource(R.string.app_name_styled),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ManropeFontFamily,
            color = TextPrimary
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun SettingsHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_subtitle),
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PrivacySection() {
    SettingsSection(title = stringResource(R.string.section_privacy)) {
        SettingsItem(
            label = stringResource(R.string.item_privacy_center),
            icon = Icons.Outlined.Shield
        )
        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 16.dp))
        SettingsItem(
            label = stringResource(R.string.item_about),
            icon = Icons.Outlined.Info
        )
    }
}