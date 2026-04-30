package com.example.notes_taking.Screens.presentations.Settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    // إدارة حالة الوضع الليلي
    var isDarkModeEnabled by mutableStateOf(false)
        private set

    // بيانات المستخدم - يفضل جلبها من الـ Repository لاحقاً
    var userName by mutableStateOf("UserName")
        private set

    var userEmail by mutableStateOf("qwerazxc@gmail.com")
        private set

    fun toggleDarkMode(enabled: Boolean) {
        isDarkModeEnabled = enabled
        // TODO: Save to DataStore so the theme persists
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            onSuccess()
        }
    }
}