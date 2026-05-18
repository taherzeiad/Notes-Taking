package com.example.notes_taking.Screens.presentations.Summary

import com.example.notes_taking.RoomDatabase.Note

data class DailySummary(
    val date: String,
    val notesCount: Int,
    val summary: String,
    val notes: List<Note>,
)

sealed interface SummaryUiState {

    data object Idle : SummaryUiState

    data object Loading : SummaryUiState

    data class EmptyToday(val date: String) : SummaryUiState

    data class Success(val summary: DailySummary) : SummaryUiState

    data class Error(val message: String) : SummaryUiState
}