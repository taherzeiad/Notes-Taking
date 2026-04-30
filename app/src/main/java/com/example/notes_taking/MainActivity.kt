@file:Suppress("DEPRECATION")

package com.example.notes_taking
// أضف هذه الاستيرادات في الأعلى
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.edit
import androidx.navigation.compose.rememberNavController
import com.example.notes_taking.Navmain.NavGraph
import com.example.notes_taking.Screens.presentations.Settings.SettingsViewModel
import com.example.notes_taking.ui.theme.NotesTakingTheme
import com.example.notes_taking.utils.LocaleUtils
import java.util.Locale

class MainActivity : ComponentActivity() {

    // 1. تعريف الـ ViewModel على مستوى النشاط (Activity) لضمان مشاركة الحالة
    private val settingsViewModel: SettingsViewModel by viewModels()

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
        enableEdgeToEdge()
        setTheme(R.style.Theme_NotesTaking)
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"

        setContent {
            // 2. مراقبة حالة الوضع المظلم من الـ ViewModel
            val isDarkMode = settingsViewModel.isDarkModeEnabled

            val navController = rememberNavController()

            // 3. تغليف التطبيق بالثيم وتمرير حالة الوضع المظلم له
            NotesTakingTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    // 4. تمرير الـ ViewModel للـ NavGraph لضمان أن شاشة الإعدادات تستخدم نفس النسخة
                    NavGraph(
                        navController = navController,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}