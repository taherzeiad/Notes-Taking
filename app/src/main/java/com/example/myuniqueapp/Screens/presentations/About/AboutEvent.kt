package com.example.myuniqueapp.Screens.presentations.About

// ======= One-shot Events =======
sealed class AboutEvent {
    data class OpenUrl(val url: String) : AboutEvent()
    data class SendEmail(val email: String) : AboutEvent()
    data class ShowSnackbar(val message: String) : AboutEvent()
}