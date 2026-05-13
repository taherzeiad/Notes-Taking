package com.example.notes_taking.Screens.presentations.PrivacyCenter

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.RoomDatabase.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ======= حالة الـ Export =======
sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val filePath: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

// ======= حالة الـ Delete =======
sealed class DeleteState {
    object Idle : DeleteState()
    object Loading : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}

class PrivacyViewModel(
    private val repository: NoteRepository,
    private val prefs: SharedPreferences,
    private val context: Context
) : ViewModel() {

    // ======= إعدادات الخصوصية من SharedPreferences =======
    private val _aiProcessingEnabled = MutableStateFlow(
        prefs.getBoolean("privacy_ai_processing", true)
    )
    val aiProcessingEnabled = _aiProcessingEnabled.asStateFlow()

    private val _voiceStorageEnabled = MutableStateFlow(
        prefs.getBoolean("privacy_voice_storage", true)
    )
    val voiceStorageEnabled = _voiceStorageEnabled.asStateFlow()

    private val _analyticsEnabled = MutableStateFlow(
        prefs.getBoolean("privacy_analytics", false)
    )
    val analyticsEnabled = _analyticsEnabled.asStateFlow()

    // ======= حالات العمليات =======
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState = _exportState.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState = _deleteState.asStateFlow()

    // ======= إعدادات Toggle =======
    fun setAiProcessing(enabled: Boolean) {
        _aiProcessingEnabled.value = enabled
        prefs.edit().putBoolean("privacy_ai_processing", enabled).apply()
    }

    fun setVoiceStorage(enabled: Boolean) {
        _voiceStorageEnabled.value = enabled
        prefs.edit().putBoolean("privacy_voice_storage", enabled).apply()
    }

    fun setAnalytics(enabled: Boolean) {
        _analyticsEnabled.value = enabled
        prefs.edit().putBoolean("privacy_analytics", enabled).apply()
    }

    // ======= تصدير البيانات =======
    fun exportData() {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            try {
                val notes = withContext(Dispatchers.IO) {
                    repository.getRecentNotes()
                }
                val tasks = withContext(Dispatchers.IO) {
                    repository.getAllTasks().let { flow ->
                        var result = emptyList<TaskEntity>()
                        val job = launch { flow.collect { result = it } }
                        kotlinx.coroutines.delay(500)
                        job.cancel()
                        result
                    }
                }

                val exportContent = buildExportContent(notes, tasks)

                val fileName = "notes_export_${System.currentTimeMillis()}.txt"
                val file = File(context.getExternalFilesDir(null), fileName)

                withContext(Dispatchers.IO) {
                    file.writeText(exportContent)
                }

                _exportState.value = ExportState.Success(file.absolutePath)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    private fun buildExportContent(notes: List<Note>, tasks: List<TaskEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("=".repeat(50))
        sb.appendLine("NOTES EXPORT")
        sb.appendLine("=".repeat(50))
        sb.appendLine()

        sb.appendLine("NOTES (${notes.size}):")
        sb.appendLine("-".repeat(30))
        notes.forEach { note ->
            sb.appendLine("Title: ${note.title}")
            sb.appendLine("Date: ${note.date}")
            sb.appendLine("Category: ${note.category}")
            sb.appendLine("Content: ${note.content}")
            sb.appendLine()
        }

        sb.appendLine("TASKS (${tasks.size}):")
        sb.appendLine("-".repeat(30))
        tasks.forEach { task ->
            sb.appendLine("Task: ${task.title}")
            sb.appendLine("Source: ${task.source}")
            sb.appendLine("Completed: ${task.isCompleted}")
            sb.appendLine()
        }

        return sb.toString()
    }

    // ======= حذف جميع البيانات =======
    fun deleteAllData() {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Loading
            try {
                withContext(Dispatchers.IO) {
                    // 1. جلب كل الملاحظات وحذفها
                    val notes = repository.getRecentNotes()
                    notes.forEach { note ->
                        repository.deleteNote(note)
                        // حذف الصور المرتبطة
                        note.imageUri?.let { path ->
                            val file = File(path)
                            if (file.exists()) file.delete()
                        }
                    }

                    // 2. حذف ملفات التسجيل الصوتي
                    context.filesDir.listFiles()?.forEach { file ->
                        if (file.name.endsWith(".mp4") || file.name.endsWith(".jpg")) {
                            file.delete()
                        }
                    }
                }
                _deleteState.value = DeleteState.Success
            } catch (e: Exception) {
                _deleteState.value = DeleteState.Error(e.message ?: "Delete failed")
            }
        }
    }

    fun resetExportState() { _exportState.value = ExportState.Idle }
    fun resetDeleteState() { _deleteState.value = DeleteState.Idle }

    // ======= Factory =======
    class Factory(
        private val repository: NoteRepository,
        private val prefs: SharedPreferences,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PrivacyViewModel(repository, prefs, context) as T
        }
    }
}