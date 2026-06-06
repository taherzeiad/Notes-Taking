package com.example.myuniqueapp.Screens.presentations.Settings

data class SettingsUiState(
    val isDarkModeEnabled: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val notificationMessage: String = "",
    val showTimePickerDialog: Boolean = false,
) {
    val reminderTimeFormatted: String
        get() = "%02d:%02d".format(reminderHour, reminderMinute)
}