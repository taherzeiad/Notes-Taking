package com.example.notes_taking.navmain

sealed class Route(val route: String) {
    object Splash : Route("splash_screen")
    object Home : Route("home_screen")
    object CreateNote : Route("create_note_screen/{noteId}") {
        fun passId(id: Int) = "create_note_screen/$id"
    }}