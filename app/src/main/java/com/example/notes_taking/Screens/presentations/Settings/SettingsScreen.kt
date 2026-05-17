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
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.notes_taking.Screens.presentations.AppTopBar
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel, navController: NavHostController
) {
    // ← Permission Launcher للإشعارات
    val context = LocalContext.current
    var showTimePickerDialog by remember { mutableStateOf(false) }
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
        bottomBar = { BottomNavBar(navController = navController, selectedTab = 0) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { SettingsTopBar() }
            item { SettingsHeader() }

            item {
                CustomizationSection(
                    isDarkMode = viewModel.isDarkModeEnabled,
                    onDarkModeChange = { viewModel.toggleDarkMode(it) })
            }

            // ← قسم الإشعارات
            item {
                NotificationsSection(
                    isEnabled = viewModel.isNotificationsEnabled,
                    reminderHour = viewModel.reminderHour,
                    reminderMinute = viewModel.reminderMinute,
                    onToggle = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleNotifications(enabled)
                        }
                    },
                    onTimeClick = { showTimePickerDialog = true })
            }

            item { PrivacySection(navController = navController) }
        }
    }

    // ======= Time Picker Dialog =======
    if (showTimePickerDialog) {
        TimePickerDialog(
            initialHour = viewModel.reminderHour,
            initialMinute = viewModel.reminderMinute,
            onConfirm = { hour, minute ->
                viewModel.updateReminderTime(hour, minute)
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false })
    }
}

// ======= Notifications Section =======
@Composable
fun NotificationsSection(
    isEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
) {
    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl
    val timeText = "%02d:%02d".format(reminderHour, reminderMinute)

    SettingsSection(title = stringResource(R.string.item_notifications)) {

        // ← Toggle الإشعارات
        SettingsItemWithToggle(
            label = stringResource(R.string.notif_daily_reminder),
            subLabel = stringResource(R.string.notif_daily_reminder_desc),
            icon = Icons.Outlined.Notifications,
            checked = isEnabled,
            onCheckedChange = onToggle
        )

        // ← وقت التذكير (يظهر فقط عند التفعيل)
        AnimatedVisibility(
            visible = isEnabled, enter = expandVertically(), exit = shrinkVertically()
        ) {
            Column {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // ← اختيار الوقت
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTimeClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    SettingsIconBox(Icons.Outlined.Schedule)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (isArabic) "وقت التذكير" else "Reminder Time",
                            fontSize = 15.sp,
                            fontFamily = ManropeFontFamily,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = timeText,
                            fontSize = 13.sp,
                            fontFamily = ManropeFontFamily,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }


            }
        }
    }
}

// ======= Time Picker Dialog =======
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int, initialMinute: Int, onConfirm: (Int, Int) -> Unit, onDismiss: () -> Unit
) {
    val isArabic = LocalLayoutDirection.current == LayoutDirection.Rtl
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour, initialMinute = initialMinute, is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss, title = {
            Text(
                text = if (isArabic) "اختر وقت التذكير" else "Choose Reminder Time",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }, text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState)
            }
        }, confirmButton = {
            Button(
                onClick = { onConfirm(timePickerState.hour, timePickerState.minute) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.add), fontFamily = ManropeFontFamily)
            }
        }, dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), fontFamily = ManropeFontFamily)
            }
        }, shape = RoundedCornerShape(20.dp)
    )
}

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
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsItem(
            label = stringResource(R.string.item_notifications), icon = Icons.Outlined.Notifications
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
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subLabel,
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange, colors = switchColors()
        )
    }
}

@Composable
fun SettingsTopBar() {
    AppTopBar(title = stringResource(R.string.app_name_styled))
}

@Composable
fun SettingsHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_subtitle),
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PrivacySection(navController: NavHostController) {
    SettingsSection(title = stringResource(R.string.section_privacy)) {
        SettingsItem(
            label = stringResource(R.string.item_privacy_center),
            icon = Icons.Outlined.Shield,
            onClick = { navController.navigate(Route.PrivacyCenter.route) })
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsItem(
            label = stringResource(R.string.item_about), icon = Icons.Outlined.Info, onClick = {
                navController.navigate(Route.AboutApp.route)
            }

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
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(label: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Row(modifier = Modifier
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
fun SettingsIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape
            ), contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
)