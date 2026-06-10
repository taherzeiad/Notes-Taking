package com.example.myuniqueapp.Screens.presentations.Settings

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.myuniqueapp.Navmain.Route
import com.notestalking.myuniqueapp.R
import com.example.myuniqueapp.Screens.presentations.AppTopBar
import com.example.myuniqueapp.Screens.presentations.Home.BottomNavBar
import com.example.myuniqueapp.ui.theme.ManropeFontFamily
import com.example.myuniqueapp.ui.theme.MansalvaFontFamily

sealed interface SettingsIntent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsIntent
    data class ToggleNotifications(val enabled: Boolean) : SettingsIntent
    data class ConfirmReminderTime(val hour: Int, val minute: Int) : SettingsIntent
    data class UpdateNotificationMessage(val message: String) : SettingsIntent
    data object OpenTimePicker : SettingsIntent
    data object DismissTimePicker : SettingsIntent
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navController: NavHostController,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ── Notification permission (Android 13+) — UI concern only ──────────────
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

    SettingsContent(
        state = state,
        navController = navController,
        onIntent = { intent ->
            handleIntent(
                intent = intent,
                viewModel = viewModel,
                hasNotifPermission = hasNotifPermission,
                requestPermission = {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
            )
        },
    )
}

private fun handleIntent(
    intent: SettingsIntent,
    viewModel: SettingsViewModel,
    hasNotifPermission: Boolean,
    requestPermission: () -> Unit,
) {
    when (intent) {
        is SettingsIntent.ToggleDarkMode -> viewModel.toggleDarkMode(intent.enabled)
        is SettingsIntent.ToggleNotifications -> {
            val needsPermission =
                intent.enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPermission
            if (needsPermission) requestPermission()
            else viewModel.toggleNotifications(intent.enabled)
        }

        is SettingsIntent.ConfirmReminderTime -> {
            viewModel.updateReminderTime(intent.hour, intent.minute)
            viewModel.dismissTimePicker()
        }

        is SettingsIntent.UpdateNotificationMessage -> viewModel.updateNotificationMessage(intent.message)

        is SettingsIntent.OpenTimePicker -> viewModel.openTimePicker()
        is SettingsIntent.DismissTimePicker -> viewModel.dismissTimePicker()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stateless Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    navController: NavHostController,
    onIntent: (SettingsIntent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController, selectedTab = 0) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item { AppTopBar(title = stringResource(R.string.app_name_styled)) }

            item { SettingsHeader() }

            item {
                CustomizationSection(
                    isDarkMode = state.isDarkModeEnabled,
                    onDarkModeChange = { onIntent(SettingsIntent.ToggleDarkMode(it)) },
                )
            }

            item {
                NotificationsSection(
                    isEnabled = state.isNotificationsEnabled,
                    reminderTime = state.reminderTimeFormatted,
                    notificationMessage = state.notificationMessage,
                    onToggle = { onIntent(SettingsIntent.ToggleNotifications(it)) },
                    onTimeClick = { onIntent(SettingsIntent.OpenTimePicker) },
                    onMessageChange = { onIntent(SettingsIntent.UpdateNotificationMessage(it)) })
            }

            item { PrivacySection(navController = navController) }
        }
    }

    if (state.showTimePickerDialog) {
        ReminderTimePickerDialog(
            initialHour = state.reminderHour,
            initialMinute = state.reminderMinute,
            onConfirm = { h, m -> onIntent(SettingsIntent.ConfirmReminderTime(h, m)) },
            onDismiss = { onIntent(SettingsIntent.DismissTimePicker) },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sections
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_subtitle),
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun CustomizationSection(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.section_customization)) {
        SettingsItemWithToggle(
            label = stringResource(R.string.item_dark_mode),
            subLabel = stringResource(R.string.sub_dark_mode),
            icon = Icons.Outlined.DarkMode,
            checked = isDarkMode,
            onCheckedChange = onDarkModeChange,
        )
    }
}

@Composable
private fun NotificationsSection(
    isEnabled: Boolean,
    reminderTime: String,
    notificationMessage: String,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    onMessageChange: (String) -> Unit
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    SettingsSection(title = stringResource(R.string.item_notifications)) {

        SettingsItemWithToggle(
            label = stringResource(R.string.notif_daily_reminder),
            subLabel = stringResource(R.string.notif_daily_reminder_desc),
            icon = Icons.Outlined.Notifications,
            checked = isEnabled,
            onCheckedChange = onToggle,
        )

        AnimatedVisibility(
            visible = isEnabled,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                ReminderTimeRow(
                    isRtl = isRtl, timeDisplay = reminderTime, onClick = onTimeClick
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                NotificationMessageField(
                    message = notificationMessage, isRtl = isRtl, onMessageChange = onMessageChange
                )
            }
        }
    }
}

// ======= Notification Message Field =======
@Composable
private fun NotificationMessageField(
    message: String, isRtl: Boolean, onMessageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsIconBox(Icons.Outlined.EditNote)
            Text(
                text = if (isRtl) "نص الإشعار" else "Notification Message",
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            placeholder = {
                Text(
                    text = if (isRtl) "اكتب نص الإشعار... (اتركه فارغاً للنص الافتراضي)"
                    else "Write notification text... (leave empty for default)",
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = ManropeFontFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            supportingText = {
                Text(
                    text = "${message.length}/100",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = if (message.length > 100) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = if (isRtl) TextAlign.Start else TextAlign.End
                )
            })
    }
}

@Composable
private fun ReminderTimeRow(
    isRtl: Boolean,
    timeDisplay: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBox(Icons.Outlined.Schedule)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = if (isRtl) "وقت التذكير" else "Reminder Time",
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = timeDisplay,
                fontSize = 13.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PrivacySection(navController: NavHostController) {
    SettingsSection(title = stringResource(R.string.section_privacy)) {
        SettingsItem(
            label = stringResource(R.string.item_privacy_center),
            icon = Icons.Outlined.Shield,
            onClick = { navController.navigate(Route.PrivacyCenter.route) },
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        SettingsItem(
            label = stringResource(R.string.item_about),
            icon = Icons.Outlined.Info,
            onClick = { navController.navigate(Route.AboutApp.route) },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isRtl) "اختر وقت التذكير" else "Choose Reminder Time",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(timePickerState.hour, timePickerState.minute) },
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.add), fontFamily = ManropeFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), fontFamily = ManropeFontFamily)
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun SettingsItemWithToggle(
    label: String,
    subLabel: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconBox(icon)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subLabel,
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

@Composable
fun SettingsIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(18.dp),
        )
    }
}