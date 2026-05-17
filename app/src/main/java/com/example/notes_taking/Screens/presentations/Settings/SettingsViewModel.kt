package com.example.notes_taking.Screens.presentations.Settings

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notes_taking.Notification.NotificationHelper
import com.example.notes_taking.Notification.NotificationScheduler

class SettingsViewModel(
    private val prefs: SharedPreferences, application: Application
) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>().applicationContext

    // ======= Dark Mode =======
    var isDarkModeEnabled by mutableStateOf(
        prefs.getBoolean("dark_mode", false)
    )
        private set

    fun toggleDarkMode(enabled: Boolean) {
        isDarkModeEnabled = enabled
        prefs.edit { putBoolean("dark_mode", enabled) }
    }

    // ======= Notifications =======
    var isNotificationsEnabled by mutableStateOf(
        prefs.getBoolean("notifications_enabled", true)
    )
        private set

    var reminderHour by mutableIntStateOf(
        prefs.getInt("reminder_hour", 20)
    )
        private set

    var reminderMinute by mutableIntStateOf(
        prefs.getInt("reminder_minute", 0)
    )
        private set

    fun toggleNotifications(enabled: Boolean) {
        isNotificationsEnabled = enabled
        prefs.edit { putBoolean("notifications_enabled", enabled) }

        if (enabled) {
            NotificationHelper.createNotificationChannel(context)
            NotificationScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
        } else {
            NotificationScheduler.cancelDailyReminder(context)
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        reminderHour = hour
        reminderMinute = minute
        prefs.edit {
            putInt("reminder_hour", hour)
            putInt("reminder_minute", minute)
        }
        if (isNotificationsEnabled) {
            NotificationScheduler.scheduleDailyReminder(context, hour, minute)
        }
    }

    fun sendTestNotification() {
        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.showDailyReminderNotification(context)
    }

    // ======= Factory =======
    class Factory(
        private val prefs: SharedPreferences, private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(prefs, application) as T
    }
}