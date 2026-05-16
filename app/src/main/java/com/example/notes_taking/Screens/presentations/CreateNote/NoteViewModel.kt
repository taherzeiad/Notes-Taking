package com.example.notes_taking.Screens.presentations.Editor

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.API.GroqService
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.RoomDatabase.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class NoteViewModel(
    private val repository: NoteRepository, private val context: Context
) : ViewModel() {

    private val privacyPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    // ======= isAiLoading - المتغير الرئيسي للتحميل =======
    private val _isAiLoading = mutableStateOf(false)
    val isAiLoading: androidx.compose.runtime.State<Boolean> = _isAiLoading

    // ======= دوال AI للـ Editor =======
    fun rephraseText(
        text: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    GroqService.rephraseText(text)
                }
                onResult(result)
            } catch (e: Exception) {
                Log.e("NoteViewModel", "rephraseText failed: ${e.message}")
                onError(e.message ?: "خطأ غير معروف")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun diacritizeText(
        text: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    GroqService.diacritizeText(text)
                }
                onResult(result)
            } catch (e: Exception) {
                Log.e("NoteViewModel", "diacritizeText failed: ${e.message}")
                onError(e.message ?: "خطأ غير معروف")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // ======= دوال مساعدة لقراءة إعدادات الخصوصية =======
    fun isAiProcessingEnabled(): Boolean {
        return privacyPrefs.getBoolean("privacy_ai_processing", true)
    }

    fun isVoiceStorageEnabled(): Boolean {
        return privacyPrefs.getBoolean("privacy_voice_storage", true)
    }

    fun isAnalyticsEnabled(): Boolean {
        return privacyPrefs.getBoolean("privacy_analytics", false)
    }

    suspend fun getNoteById(id: Int): Note? {
        return if (id > 0) {
            withContext(Dispatchers.IO) {
                try {
                    repository.getNoteById(id)
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Error getting note: ${e.message}")
                    null
                }
            }
        } else null
    }

    fun saveNoteWithAI(
        id: Int,
        title: String,
        content: String,
        imageUri: String?,
        audioPaths: String?,
        date: String,
        manualTasks: List<String> = emptyList(),
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val finalTitle = title.trim().ifBlank {
                    content.take(30).trim().ifBlank { "ملاحظة جديدة" }
                }
                val finalContent = content.trim()

                var autoCategory = "General"
                if (isAiProcessingEnabled() && finalContent.isNotBlank()) {
                    try {
                        val classification = GroqService.classifyNoteContent(finalContent)
                        autoCategory = when {
                            classification.contains("Philo", ignoreCase = true) -> "Philosophy"
                            classification.contains("Liter", ignoreCase = true) -> "Literature"
                            classification.contains("Dev", ignoreCase = true) -> "Self-Development"
                            classification.contains("Task", ignoreCase = true) -> "Task"
                            classification.contains("Work", ignoreCase = true) -> "Work"
                            else -> "General"
                        }
                        if (isAnalyticsEnabled()) {
                            logAnalytics(
                                "note_classified", mapOf(
                                    "category" to autoCategory,
                                    "content_length" to finalContent.length.toString()
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("NoteViewModel", "AI classification failed: ${e.message}")
                    }
                }

                val finalAudioPaths = if (isVoiceStorageEnabled()) audioPaths else null

                val noteToSave = Note(
                    id = if (id > 0) id else 0,
                    title = finalTitle,
                    content = finalContent,
                    audioPaths = finalAudioPaths,
                    category = autoCategory,
                    imageUri = imageUri,
                    date = date
                )

                if (id > 0) repository.updateNote(noteToSave)
                else repository.insertNote(noteToSave)

                val savedNoteId = if (id > 0) id else repository.getLastNote()?.id ?: 0

                if (savedNoteId > 0) repository.deleteTasksByNoteId(savedNoteId)

                val allTaskTitles = mutableListOf<String>()
                allTaskTitles.addAll(manualTasks.filter { it.isNotBlank() })

                if (isAiProcessingEnabled() && finalContent.isNotBlank()) {
                    val textOnlyContent = finalContent.lines()
                        .filter { !it.startsWith("•") }
                        .joinToString("\n").trim()

                    if (textOnlyContent.isNotBlank()) {
                        try {
                            val aiTasks = GroqService.extractTasksFromNote(finalTitle, textOnlyContent)
                            aiTasks.forEach { aiTask ->
                                val isDuplicate = allTaskTitles.any { existing ->
                                    existing.contains(aiTask, ignoreCase = true) ||
                                            aiTask.contains(existing, ignoreCase = true)
                                }
                                if (!isDuplicate && aiTask.isNotBlank()) allTaskTitles.add(aiTask)
                            }
                            if (isAnalyticsEnabled()) {
                                logAnalytics(
                                    "tasks_extracted", mapOf(
                                        "ai_tasks_count" to aiTasks.size.toString(),
                                        "manual_tasks_count" to manualTasks.size.toString()
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("NoteViewModel", "AI task extraction failed: ${e.message}")
                        }
                    }
                }

                if (allTaskTitles.isNotEmpty() && savedNoteId > 0) {
                    val taskEntities = allTaskTitles.mapIndexed { index, taskTitle ->
                        TaskEntity(
                            title = taskTitle,
                            source = finalTitle,
                            noteId = savedNoteId,
                            date = date,
                            isUrgent = index < manualTasks.size
                        )
                    }
                    repository.insertTasks(taskEntities)
                }

                withContext(Dispatchers.Main) { onComplete() }

            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error during save: ${e.message}")
                try {
                    val fallbackNote = Note(
                        id = if (id > 0) id else 0,
                        title = title.trim().ifBlank { "ملاحظة جديدة" },
                        content = content.trim(),
                        category = "General",
                        imageUri = imageUri,
                        audioPaths = if (isVoiceStorageEnabled()) audioPaths else null,
                        date = date
                    )
                    if (id > 0) repository.updateNote(fallbackNote)
                    else repository.insertNote(fallbackNote)

                    if (manualTasks.isNotEmpty()) {
                        val savedNoteId = if (id > 0) id else repository.getLastNote()?.id ?: 0
                        if (savedNoteId > 0) {
                            repository.insertTasks(manualTasks.filter { it.isNotBlank() }
                                .map { taskTitle ->
                                    TaskEntity(
                                        title = taskTitle,
                                        source = title.ifBlank { "ملاحظة" },
                                        noteId = savedNoteId,
                                        date = date,
                                        isUrgent = true
                                    )
                                })
                        }
                    }
                    withContext(Dispatchers.Main) { onComplete() }
                } catch (fallbackError: Exception) {
                    withContext(Dispatchers.Main) {
                        onError("فشل في حفظ الملاحظة: ${fallbackError.message}")
                    }
                }
            }
        }
    }

    private fun logAnalytics(event: String, params: Map<String, String>) {
        Log.d("NoteAnalytics", "Event: $event, Params: $params")
        val analyticsLog = privacyPrefs.getString("analytics_log", "") ?: ""
        val newLog = "$analyticsLog\n${System.currentTimeMillis()}: $event - $params"
        privacyPrefs.edit().putString("analytics_log", newLog).apply()
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val fileName = "note_image_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file.absolutePath
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "Error saving image: ${e.message}")
            null
        }
    }

    fun deleteNote(note: Note, onDeleteSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteNote(note)
                withContext(Dispatchers.Main) { onDeleteSuccess() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("فشل في حذف الملاحظة: ${e.message}") }
            }
        }
    }
}