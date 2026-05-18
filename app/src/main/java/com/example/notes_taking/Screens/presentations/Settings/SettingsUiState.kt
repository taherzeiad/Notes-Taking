package com.example.notes_taking.Screens.presentations.Settings

data class SettingsUiState(
    // ── Customization ─────────────────────────────────────────────────────────
    val isDarkModeEnabled: Boolean = false,

    // ── Notifications ─────────────────────────────────────────────────────────
    val isNotificationsEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,

    // ── Dialogs ───────────────────────────────────────────────────────────────
    val showTimePickerDialog: Boolean = false,
) {
    val reminderTimeFormatted: String
        get() = "%02d:%02d".format(reminderHour, reminderMinute)
}