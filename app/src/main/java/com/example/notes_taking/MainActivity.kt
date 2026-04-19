@file:Suppress("DEPRECATION")

package com.example.notes_taking

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.example.notes_taking.navmain.NavGraph
import com.example.notes_taking.utils.LocaleUtils
import java.util.Locale

object LocaleUtils {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    // ← دالة جديدة لجلب لغة الجهاز
    fun getDeviceLanguage(): String {
        val deviceLang = Locale.getDefault().language
        return if (deviceLang == "ar") "ar" else "en"
    }
}
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // ← جلب لغة الجهاز تلقائياً
        val deviceLang = Locale.getDefault().language

        // ← إذا لغة الجهاز عربية استخدمها، غير ذلك إنجليزية
        val lang = if (deviceLang == "ar") "ar" else "en"

        // ← احفظها في SharedPreferences
        newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("language", lang)
            .apply()

        val context = LocaleUtils.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"

        setContent {
            val navController = rememberNavController()

            CompositionLocalProvider(
                LocalLayoutDirection provides if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                NavGraph(navController = navController)
            }
        }
    }
}