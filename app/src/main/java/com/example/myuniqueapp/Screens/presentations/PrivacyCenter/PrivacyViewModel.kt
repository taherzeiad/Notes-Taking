package com.example.myuniqueapp.Screens.presentations.PrivacyCenter

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myuniqueapp.Repository.NoteRepository
import com.example.myuniqueapp.RoomDatabase.Note
import com.example.myuniqueapp.RoomDatabase.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PrivacyViewModel(
    private val repository: NoteRepository,
    private val prefs: SharedPreferences,
    private val context: Context
) : ViewModel() {

    // ======= Single UI State =======
    private val _uiState = MutableStateFlow(
        PrivacyUiState(
            aiProcessingEnabled = prefs.getBoolean("privacy_ai_processing", true),
            voiceStorageEnabled  = prefs.getBoolean("privacy_voice_storage", true),
            analyticsEnabled     = prefs.getBoolean("privacy_analytics", false),
        )
    )
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    // ======= One-shot Events Channel =======
    private val _events = Channel<PrivacyEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ======= Permissions =======
    fun refreshPermissions(isMicGranted: Boolean, isStorageGranted: Boolean) {
        _uiState.update {
            it.copy(
                isMicGranted     = isMicGranted,
                isStorageGranted = isStorageGranted
            )
        }
    }

    // ======= Toggle Settings =======
    fun setAiProcessing(enabled: Boolean) {
        prefs.edit().putBoolean("privacy_ai_processing", enabled).apply()
        _uiState.update { it.copy(aiProcessingEnabled = enabled) }
    }

    fun setVoiceStorage(enabled: Boolean) {
        prefs.edit().putBoolean("privacy_voice_storage", enabled).apply()
        _uiState.update { it.copy(voiceStorageEnabled = enabled) }
    }

    fun setAnalytics(enabled: Boolean) {
        prefs.edit().putBoolean("privacy_analytics", enabled).apply()
        _uiState.update { it.copy(analyticsEnabled = enabled) }
    }

    // ======= Dialog Controls =======
    fun showDeleteDialog()  { _uiState.update { it.copy(showDeleteDialog  = true)  } }
    fun hideDeleteDialog()  { _uiState.update { it.copy(showDeleteDialog  = false) } }
    fun showExportDialog()  { _uiState.update { it.copy(showExportDialog  = true)  } }
    fun hideExportDialog()  { _uiState.update { it.copy(showExportDialog  = false) } }

    // ======= Export Data =======
    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(exportState = ExportState.Loading) }
            try {
                val (notes, tasks) = withContext(Dispatchers.IO) {
                    val notes = repository.getRecentNotes()
                    // استخدام first() بدل الـ delay hack
                    val tasks = repository.getAllTasks().first()
                    Pair(notes, tasks)
                }

                val content  = buildExportContent(notes, tasks)
                val fileName = "notes_export_${System.currentTimeMillis()}.txt"
                val file     = File(context.getExternalFilesDir(null), fileName)

                withContext(Dispatchers.IO) { file.writeText(content) }

                _uiState.update { it.copy(exportState = ExportState.Success(file.absolutePath)) }
                _events.send(PrivacyEvent.OpenFile(file.absolutePath))

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(exportState = ExportState.Error(e.message ?: "Export failed"))
                }
                _events.send(
                    PrivacyEvent.ShowSnackbar("Export failed: ${e.message}")
                )
            }
        }
    }

    // ======= Delete All Data =======
    fun deleteAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(deleteState = DeleteState.Loading) }
            try {
                withContext(Dispatchers.IO) {
                    repository.getRecentNotes().forEach { note ->
                        repository.deleteNote(note)
                        note.imageUri?.let { path ->
                            File(path).takeIf { it.exists() }?.delete()
                        }
                    }
                    context.filesDir.listFiles()
                        ?.filter { it.name.endsWith(".mp4") || it.name.endsWith(".jpg") }
                        ?.forEach { it.delete() }
                }
                _uiState.update { it.copy(deleteState = DeleteState.Success) }
                _events.send(PrivacyEvent.ShowSnackbar("All data deleted successfully"))

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(deleteState = DeleteState.Error(e.message ?: "Delete failed"))
                }
                _events.send(
                    PrivacyEvent.ShowSnackbar("Delete failed: ${e.message}")
                )
            }
        }
    }

    // ======= Reset States =======
    fun resetExportState() { _uiState.update { it.copy(exportState = ExportState.Idle) } }
    fun resetDeleteState() { _uiState.update { it.copy(deleteState = DeleteState.Idle) } }

    // ======= Open Settings =======
    fun onManagePermission() {
        viewModelScope.launch { _events.send(PrivacyEvent.OpenAppSettings) }
    }

    // ======= Private Helpers =======
    private fun buildExportContent(notes: List<Note>, tasks: List<TaskEntity>): String =
        buildString {
            appendLine("=".repeat(50))
            appendLine("NOTES EXPORT")
            appendLine("=".repeat(50))
            appendLine()
            appendLine("NOTES (${notes.size}):")
            appendLine("-".repeat(30))
            notes.forEach { note ->
                appendLine("Title: ${note.title}")
                appendLine("Date: ${note.date}")
                appendLine("Category: ${note.category}")
                appendLine("Content: ${note.content}")
                appendLine()
            }
            appendLine("TASKS (${tasks.size}):")
            appendLine("-".repeat(30))
            tasks.forEach { task ->
                appendLine("Task: ${task.title}")
                appendLine("Source: ${task.source}")
                appendLine("Completed: ${task.isCompleted}")
                appendLine()
            }
        }

    // ======= Factory =======
    class Factory(
        private val repository: NoteRepository,
        private val prefs: SharedPreferences,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PrivacyViewModel(repository, prefs, context) as T
    }
}