package com.example.notes_taking.Screens.presentations.Settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.notes_taking.Navmain.Route
import com.example.notes_taking.R
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var showTimePicker by remember { mutableStateOf(false) }

    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
        if (granted) viewModel.toggleNotifications(true)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController, selectedTab = 0) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "topbar") { SettingsTopBar() }
            item(key = "header") { SettingsHeader() }

            item(key = "customization") {
                CustomizationSection(
                    isDarkMode = viewModel.isDarkModeEnabled,
                    onDarkModeChange = viewModel::toggleDarkMode
                )
            }

            item(key = "notifications") {
                NotificationsSection(
                    isEnabled = viewModel.isNotificationsEnabled,
                    reminderHour = viewModel.reminderHour,
                    reminderMinute = viewModel.reminderMinute,
                    isRtl = isRtl,
                    onToggle = { enabled ->
                        if (enabled &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !hasNotifPermission
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleNotifications(enabled)
                        }
                    },
                    onTimeClick = { showTimePicker = true },
                    onTestClick = viewModel::sendTestNotification
                )
            }

            item(key = "privacy") {
                PrivacySection(navController = navController)
            }

            item(key = "spacer") { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = viewModel.reminderHour,
            initialMinute = viewModel.reminderMinute,
            isRtl = isRtl,
            onConfirm = { hour, minute ->
                viewModel.updateReminderTime(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

// ======= Top Bar =======
@Composable
fun SettingsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person, null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = stringResource(R.string.app_name_styled),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Icon(
            Icons.AutoMirrored.Outlined.MenuBook, null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(26.dp)
        )
    }
}

// ======= Header =======
@Composable
fun SettingsHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_subtitle),
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

// ======= Customization Section =======
@Composable
fun CustomizationSection(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    SettingsSection(title = stringResource(R.string.section_customization)) {
        SettingsToggleItem(
            label = stringResource(R.string.item_dark_mode),
            subLabel = stringResource(R.string.sub_dark_mode),
            icon = Icons.Outlined.DarkMode,
            checked = isDarkMode,
            onCheckedChange = onDarkModeChange
        )
    }
}

// ======= Notifications Section =======
@Composable
fun NotificationsSection(
    isEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    isRtl: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    onTestClick: () -> Unit
) {
    val timeText = "%02d:%02d".format(reminderHour, reminderMinute)

    SettingsSection(title = stringResource(R.string.item_notifications)) {

        SettingsToggleItem(
            label = stringResource(R.string.notif_daily_reminder),
            subLabel = stringResource(R.string.notif_daily_reminder_desc),
            icon = Icons.Outlined.Notifications,
            checked = isEnabled,
            onCheckedChange = onToggle
        )

        AnimatedVisibility(
            visible = isEnabled,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // ← وقت التذكير
                SettingsArrowItem(
                    label = if (isRtl) "وقت التذكير" else "Reminder Time",
                    subLabel = timeText,
                    icon = Icons.Outlined.Schedule,
                    onClick = onTimeClick
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // ← إشعار تجريبي
                SettingsArrowItem(
                    label = if (isRtl) "إرسال إشعار تجريبي" else "Send Test Notification",
                    subLabel = if (isRtl) "اختبر الإشعار الآن" else "Test your notification now",
                    icon = Icons.Outlined.Notifications,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onTestClick
                )
            }
        }
    }
}

// ======= Privacy Section =======
@Composable
fun PrivacySection(navController: NavHostController) {
    SettingsSection(title = stringResource(R.string.section_privacy)) {
        SettingsArrowItem(
            label = stringResource(R.string.item_privacy_center),
            icon = Icons.Outlined.Shield,
            onClick = { navController.navigate(Route.PrivacyCenter.route) }
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsArrowItem(
            label = stringResource(R.string.item_about),
            icon = Icons.Outlined.Info,
            onClick = { navController.navigate(Route.AboutApp.route) }
        )
    }
}

// ======= Time Picker Dialog =======
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    isRtl: Boolean,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isRtl) "اختر وقت التذكير" else "Choose Reminder Time",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(state.hour, state.minute) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.add), fontFamily = ManropeFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), fontFamily = ManropeFontFamily)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ============================================
// Reusable Components
// ============================================

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsToggleItem(
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
        SettingsIconBox(icon = icon)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subLabel,
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsArrowItem(
    label: String,
    icon: ImageVector,
    subLabel: String? = null,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon = icon, tint = iconTint)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    fontSize = 13.sp,
                    fontFamily = ManropeFontFamily,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsIconBox(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}