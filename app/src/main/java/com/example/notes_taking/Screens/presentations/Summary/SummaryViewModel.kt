package com.example.notes_taking.Screens.presentations.Summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.API.GroqService
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailySummary(
    val date: String, val notesCount: Int, val summary: String, val notes: List<Note>
)

sealed class SummaryState {
    object Idle : SummaryState()
    object Loading : SummaryState()
    object EmptyToday : SummaryState()
    data class Success(val summary: DailySummary) : SummaryState()
    data class Error(val message: String) : SummaryState()
}

class SummaryViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Idle)
    val summaryState = _summaryState.asStateFlow()

    fun summarizeDay(date: String) {
        viewModelScope.launch {
            _summaryState.value = SummaryState.Loading
            try {
                val allNotes = repository.getRecentNotes()

                val todayNotes = allNotes.filter { it.date == date }

                if (todayNotes.isEmpty()) {
                    _summaryState.value = SummaryState.EmptyToday
                    return@launch
                }

                val notesText =
                    todayNotes.filter { it.content.isNotBlank() || it.title.isNotBlank() }
                        .map { "${it.title}: ${it.content}" }

                if (notesText.isEmpty()) {
                    _summaryState.value = SummaryState.EmptyToday
                    return@launch
                }

                val summary = try {
                    GroqService.summarizeNotes(notesText, date)
                } catch (e: Exception) {
                    if (Locale.getDefault().language == "ar") "فشل في تلخيص ملاحظات اليوم"
                    else "Failed to summarize today's notes"
                }

                _summaryState.value = SummaryState.Success(
                    DailySummary(
                        date = date,
                        notesCount = todayNotes.size,
                        summary = summary,
                        notes = todayNotes
                    )
                )

            } catch (e: Exception) {
                _summaryState.value = SummaryState.Error(
                    if (Locale.getDefault().language == "ar") "حدث خطأ: ${e.message}"
                    else "An error occurred: ${e.message}"
                )
            }
        }
    }

    fun getTodayDate(): String {
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    }
}