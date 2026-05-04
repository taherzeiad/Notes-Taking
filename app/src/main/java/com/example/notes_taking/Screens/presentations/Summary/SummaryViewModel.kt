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
    val date: String,
    val notesCount: Int,
    val summary: String,
    val notes: List<Note>
)

sealed class SummaryState {
    object Idle : SummaryState()
    object Loading : SummaryState()
    data class Success(val summaries: List<DailySummary>) : SummaryState()
    data class Error(val message: String) : SummaryState()
}

class SummaryViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Idle)
    val summaryState = _summaryState.asStateFlow()

    private val _selectedDate = MutableStateFlow(getTodayDate())
    val selectedDate = _selectedDate.asStateFlow()

    // ← جلب وتلخيص ملاحظات يوم معين
    fun summarizeDay(date: String) {
        viewModelScope.launch {
            _summaryState.value = SummaryState.Loading
            try {
                val allNotes = repository.getRecentNotes()

                // تجميع الملاحظات حسب التاريخ
                val groupedByDate = allNotes
                    .groupBy { it.date }
                    .filter { it.value.isNotEmpty() }

                if (groupedByDate.isEmpty()) {
                    _summaryState.value = SummaryState.Error("لا توجد ملاحظات لتلخيصها")
                    return@launch
                }

                val summaries = mutableListOf<DailySummary>()

                // تلخيص كل يوم
                for ((noteDate, notes) in groupedByDate) {
                    val notesText = notes
                        .filter { it.content.isNotBlank() || it.title.isNotBlank() }
                        .map { "${it.title}: ${it.content}" }

                    if (notesText.isEmpty()) continue

                    val summary = try {
                        GroqService.summarizeNotes(notesText, noteDate)
                    } catch (e: Exception) {
                        "فشل في تلخيص ملاحظات هذا اليوم"
                    }

                    summaries.add(
                        DailySummary(
                            date = noteDate,
                            notesCount = notes.size,
                            summary = summary,
                            notes = notes
                        )
                    )
                }

                _summaryState.value = SummaryState.Success(
                    summaries.sortedByDescending { it.date }
                )

            } catch (e: Exception) {
                _summaryState.value = SummaryState.Error("حدث خطأ: ${e.message}")
            }
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun getTodayDate(): String {
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    }
}