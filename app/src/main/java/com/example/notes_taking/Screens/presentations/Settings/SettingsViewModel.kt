package com.example.notes_taking.Screens.presentations.Settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    // حالة الوضع الليلي
    var isDarkModeEnabled by mutableStateOf(false)
        private set

    // بيانات المستخدم (يمكن جلبها مستقبلاً من Repository أو DataStore)
    var userName by mutableStateOf("طاهر قديح")
        private set

    var userEmail by mutableStateOf("taher@sanctuary.io")
        private set

    fun toggleDarkMode(enabled: Boolean) {
        isDarkModeEnabled = enabled
        // هنا يمكنك حفظ القيمة في SharedPreferences أو DataStore
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // منطق تسجيل الخروج (مسح التوكن، حذف البيانات المؤقتة)
            onSuccess()
        }
    }

    fun updateProfile() {
        // منطق تحديث الملف الشخصي
    }
}