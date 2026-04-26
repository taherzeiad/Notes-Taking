package com.example.notes_taking.Navmain

sealed class Route(val route: String) {
    object Splash : Route("splash_screen")
    object Onboarding : Route("onboarding_screen")
    object Home : Route("home_screen")
    object Settings : Route("settings_screen")
    object Tasks : Route("tasks_screen")
    object Notes : Route("notes_screen")

    object NoteEditor : Route("note_editor/{noteId}") {
        fun createRoute(noteId: Int) = "note_editor/$noteId"
    }
}