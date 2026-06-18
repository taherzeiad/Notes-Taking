@file:Suppress("DEPRECATION")

package com.example.myuniqueapp.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Locale

object LocaleUtils {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getCurrentLanguage(context: Context): String {
        return context.resources.configuration.locales[0].language
    }
    fun getTimeAgo(timestamp: Long): String {
        val isArabic = java.util.Locale.getDefault().language == "ar"
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            diff < 60_000L -> {
                if (isArabic) "الآن" else "Just now"
            }
            minutes < 60 -> {
                if (isArabic) "منذ $minutes دقيقة"
                else "$minutes min ago"
            }
            hours < 24 -> {
                if (isArabic) "منذ $hours ساعة"
                else "$hours hr ago"
            }
            days == 1L -> {
                if (isArabic) "أمس" else "Yesterday"
            }
            days < 7 -> {
                if (isArabic) "منذ $days أيام"
                else "$days days ago"
            }
            else -> {
                // ← أعرض التاريخ كاملاً
                val sdf = java.text.SimpleDateFormat(
                    "dd MMM", java.util.Locale.getDefault()
                )
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
}