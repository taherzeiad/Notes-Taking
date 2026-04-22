@file:Suppress("DEPRECATION")

package com.example.notes_taking

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.edit
import androidx.navigation.compose.rememberNavController
import com.example.notes_taking.Navmain.NavGraph
import com.example.notes_taking.utils.LocaleUtils
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val deviceLang = Locale.getDefault().language
        val lang = if (deviceLang == "ar") "ar" else "en"

        newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit {
                putString("language", lang)
            }

        val context = LocaleUtils.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. تفعيل العرض بكامل الشاشة لإخفاء الفجوات
        enableEdgeToEdge()

        // 2. إعادة الثيم الأصلي للتطبيق (بدل ثيم الـ Splash الشفاف)
        setTheme(R.style.Theme_NotesTaking)

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