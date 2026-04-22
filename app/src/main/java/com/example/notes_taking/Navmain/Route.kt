package com.example.notes_taking.Navmain

sealed class Route(val route: String) {
    object Splash : Route("splash_screen")
    object Home : Route("home_screen")
    object CreateNote : Route("create_note/0")
    object EditNote : Route("create_note/{noteId}") {
        fun createRoute(noteId: Int) = "create_note/$noteId"
    }

    object Onboarding : Route("onboarding_screen")
    object Settings : Route("settings_screen")
    object Tasks : Route("tasks_screen")

    object Notes : Route("notes_screen")

}