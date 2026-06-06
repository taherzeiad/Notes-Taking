package com.example.myuniqueapp.Screens.presentations.Notes

import com.example.myuniqueapp.RoomDatabase.Note

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true
)