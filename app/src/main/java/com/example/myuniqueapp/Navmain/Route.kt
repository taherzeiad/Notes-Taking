package com.example.myuniqueapp.Navmain

sealed class Route(val route: String) {
    enum class PrivacyPolicy(route: Any) {

    }

    object Splash : Route("splash_screen")
    object Onboarding : Route("onboarding_screen")
    object Home : Route("home_screen")
    object Settings : Route("settings_screen")
    object Tasks : Route("tasks_screen")
    object Notes : Route("notes_screen")

    object NoteEditor : Route("note_editor/{noteId}/{openAudio}/{openImage}") {
        fun createRoute(noteId: Int, openAudio: Boolean = false, openImage: Boolean = false) =
            "note_editor/$noteId/$openAudio/$openImage"
    }


    object AboutApp : Route("about_app")
    object Summary : Route("summary_screen")
    object Privacy : Route("privacy_screen")
    object PrivacyCenter : Route("privacy_center_screen")
}