import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.API.Gemini.GeminiService
import com.example.notes_taking.API.Gemini.SummaryAiProvider
import com.example.notes_taking.API.Groq.GroqService
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.Screens.presentations.Summary.DailySummary
import com.example.notes_taking.Screens.presentations.Summary.SummaryRateLimiter
import com.example.notes_taking.Screens.presentations.Summary.SummaryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SummaryViewModel(
    private val repository: NoteRepository,
    private val rateLimiter: SummaryRateLimiter,          // ← جديد
    private val aiProvider: SummaryAiProvider = SummaryAiProvider.GROQ,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    fun summarizeToday() = summarizeDay(getTodayDate())

    fun getTodayDate(): String =
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

    private fun summarizeDay(date: String) {
        viewModelScope.launch {

            // ── تحقق من الحد قبل أي شيء ──────────────────────────────
            if (!rateLimiter.canAttempt()) {
                _uiState.update {
                    SummaryUiState.LimitReached(
                        attemptsUsed = rateLimiter.attemptsUsedToday(),
                        maxAttempts = SummaryRateLimiter.MAX_ATTEMPTS,
                    )
                }
                return@launch
            }

            _uiState.update { SummaryUiState.Loading }

            val todayNotes = runCatching { repository.getRecentNotes() }.getOrElse { e ->
                _uiState.update { SummaryUiState.Error(buildErrorMessage(e)) }
                return@launch
            }.filter { it.date == date }

            if (todayNotes.isEmpty()) {
                _uiState.update { SummaryUiState.EmptyToday(date) }
                return@launch
            }

            val notesText = todayNotes.filter { it.content.isNotBlank() || it.title.isNotBlank() }
                .map { "${it.title}: ${it.content}" }

            if (notesText.isEmpty()) {
                _uiState.update { SummaryUiState.EmptyToday(date) }
                return@launch
            }

            val summaryText = runCatching {
                when (aiProvider) {
                    SummaryAiProvider.GROQ -> GroqService.summarizeNotes(notesText, date)
                    SummaryAiProvider.GEMINI -> GeminiService.summarizeNotes(notesText, date)
                }
            }.getOrElse { e ->
                Log.e("SummaryVM", "AI error: ${e.message}", e)
                _uiState.update { SummaryUiState.Error("${e.message}") }
                return@launch
            }
            rateLimiter.recordAttempt()

            _uiState.update {
                SummaryUiState.Success(
                    DailySummary(
                        date = date,
                        notesCount = todayNotes.size,
                        summary = summaryText,
                        notes = todayNotes,
                    )
                )
            }
        }
    }

    private fun buildErrorMessage(e: Throwable): String = if (isArabic()) "حدث خطأ: ${e.message}"
    else "An error occurred: ${e.message}"

    private fun isArabic(): Boolean = Locale.getDefault().language == "ar"
}