package com.example.myuniqueapp.Screens.presentations.Home

import com.example.myuniqueapp.RoomDatabase.Note


data class HomeUiState(
    val lastEditedNote: Note? = null,
    val isEmpty: Boolean = true,
    val isLoading: Boolean = true
)
