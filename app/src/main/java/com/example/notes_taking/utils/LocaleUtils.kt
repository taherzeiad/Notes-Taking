@file:Suppress("DEPRECATION")

package com.example.notes_taking.utils

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
}