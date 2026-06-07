package com.example.myuniqueapp.Screens.presentations.About

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AboutViewModel : ViewModel() {

    private val _events = Channel<AboutEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onWebsiteClick() {
        viewModelScope.launch {
            _events.send(AboutEvent.OpenUrl("https://www.linkedin.com/in/taherqudeih"))
        }
    }

    fun onSupportClick() {
        viewModelScope.launch {
            _events.send(AboutEvent.SendEmail("taherqudeih@gmail.com"))
        }
    }
}