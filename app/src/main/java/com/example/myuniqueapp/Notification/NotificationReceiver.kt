package com.example.myuniqueapp.Notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // ← إعادة جدولة الإشعارات بعد إعادة التشغيل
                val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("notifications_enabled", true)
                if (enabled) {
                    NotificationScheduler.scheduleDailyReminder(context)
                }
            }
            "DAILY_REMINDER" -> {
                NotificationHelper.showDailyReminderNotification(context)
            }
        }
    }
}