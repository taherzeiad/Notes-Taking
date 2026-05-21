package com.example.notes_taking.Screens.presentations.Summary

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SummaryRateLimiter(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "summary_rate_limiter"
        private const val KEY_DATE = "last_date"
        private const val KEY_COUNT = "attempts_count"
        const val MAX_ATTEMPTS = 4
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /** كم محاولة استُخدمت اليوم */
    fun attemptsUsedToday(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_COUNT, 0)
    }

    /** كم محاولة تبقّت */
    fun remainingAttempts(): Int = MAX_ATTEMPTS - attemptsUsedToday()

    /** هل يمكن تنفيذ محاولة جديدة؟ */
    fun canAttempt(): Boolean = attemptsUsedToday() < MAX_ATTEMPTS

    /** سجّل محاولة واحدة — ارجع false إذا تجاوز الحد */
    fun recordAttempt(): Boolean {
        if (!canAttempt()) return false
        prefs.edit().putString(KEY_DATE, todayKey()).putInt(KEY_COUNT, attemptsUsedToday() + 1)
            .apply()
        return true
    }

    /** إعادة الضبط تلقائياً إذا تغيّر اليوم */
    private fun resetIfNewDay() {
        val saved = prefs.getString(KEY_DATE, null)
        if (saved != todayKey()) {
            prefs.edit().putString(KEY_DATE, todayKey()).putInt(KEY_COUNT, 0).apply()
        }
    }
}