package com.example.myuniqueapp.Screens.presentations.Summary

import com.example.myuniqueapp.RoomDatabase.Note

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
    data class LimitReached(val attemptsUsed: Int, val maxAttempts: Int) : SummaryUiState

}