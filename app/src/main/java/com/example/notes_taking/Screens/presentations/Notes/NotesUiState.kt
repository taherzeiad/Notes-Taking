package com.example.notes_taking.Screens.presentations.Notes

import com.example.notes_taking.RoomDatabase.Note

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true
)