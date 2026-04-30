package com.example.notes_taking.Screens.presentations.Settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    // إدارة حالة الوضع الليلي
    var isDarkModeEnabled by mutableStateOf(false)
        private set

    fun toggleDarkMode(enabled: Boolean) {
        isDarkModeEnabled = enabled
        // TODO: Save to DataStore so the theme persists
    }
}