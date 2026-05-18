package com.example.notes_taking.Screens.presentations.Settings

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notes_taking.Notification.NotificationHelper
import com.example.notes_taking.Notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    private val prefs: SharedPreferences,
    application: Application,
) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>().applicationContext

    // ── Single source of truth ────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isDarkModeEnabled      = prefs.getBoolean("dark_mode", false),
            isNotificationsEnabled = prefs.getBoolean("notifications_enabled", true),
            reminderHour           = prefs.getInt("reminder_hour", 20),
            reminderMinute         = prefs.getInt("reminder_minute", 0),
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // ── User intents ──────────────────────────────────────────────────────────

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkModeEnabled = enabled) }
        prefs.edit { putBoolean("dark_mode", enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationsEnabled = enabled) }
        prefs.edit { putBoolean("notifications_enabled", enabled) }

        if (enabled) {
            val state = _uiState.value
            NotificationHelper.createNotificationChannel(context)
            NotificationScheduler.scheduleDailyReminder(context, state.reminderHour, state.reminderMinute)
        } else {
            NotificationScheduler.cancelDailyReminder(context)
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(reminderHour = hour, reminderMinute = minute) }
        prefs.edit {
            putInt("reminder_hour", hour)
            putInt("reminder_minute", minute)
        }
        if (_uiState.value.isNotificationsEnabled) {
            NotificationScheduler.scheduleDailyReminder(context, hour, minute)
        }
    }

    fun openTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = true) }
    }

    fun dismissTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = false) }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(
        private val prefs: SharedPreferences,
        private val application: Application,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(prefs, application) as T
    }
}