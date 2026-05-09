package com.example.notes_taking.Screens.presentations.Editor

import android.content.Context
import android.net.Uri
import android.util.Log
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

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

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

                // 1. تصنيف الملاحظة بالـ AI
                var autoCategory = "General"
                if (finalContent.isNotBlank()) {
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
                    } catch (e: Exception) {
                        Log.e("NoteViewModel", "AI classification failed: ${e.message}")
                    }
                }

                // 2. حفظ الملاحظة
                val noteToSave = Note(
                    id = if (id > 0) id else 0,
                    title = finalTitle,
                    content = finalContent,
                    category = autoCategory,
                    imageUri = imageUri,
                    date = date
                )

                if (id > 0) {
                    repository.updateNote(noteToSave)
                } else {
                    repository.insertNote(noteToSave)
                }

                // 3. جلب ID الملاحظة المحفوظة
                val savedNoteId = if (id > 0) id else {
                    repository.getLastNote()?.id ?: 0
                }

                // 4. حذف المهام القديمة لهذه الملاحظة
                if (savedNoteId > 0) {
                    repository.deleteTasksByNoteId(savedNoteId)
                }

                // 5. بناء قائمة المهام
                val allTaskTitles = mutableListOf<String>()

                // أولاً: المهام اليدوية من BulletBlocks
                allTaskTitles.addAll(manualTasks.filter { it.isNotBlank() })

                // ثانياً: إذا كانت الملاحظة من نوع Task أو تحتوي على مهام في النص
                if (finalContent.isNotBlank()) {
                    val textOnlyContent =
                        finalContent.lines().filter { !it.startsWith("•") }.joinToString("\n")
                            .trim()

                    if (textOnlyContent.isNotBlank()) {
                        try {
                            val aiTasks =
                                GroqService.extractTasksFromNote(finalTitle, textOnlyContent)
                            aiTasks.forEach { aiTask ->
                                val isDuplicate = allTaskTitles.any { existing ->
                                    existing.contains(aiTask, ignoreCase = true) || aiTask.contains(
                                        existing,
                                        ignoreCase = true
                                    )
                                }
                                if (!isDuplicate && aiTask.isNotBlank()) {
                                    allTaskTitles.add(aiTask)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("NoteViewModel", "AI task extraction failed: ${e.message}")
                        }
                    }
                }

                // 6. حفظ المهام في Room
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
                // Fallback
                try {
                    val fallbackNote = Note(
                        id = if (id > 0) id else 0,
                        title = title.trim().ifBlank { "ملاحظة جديدة" },
                        content = content.trim(),
                        category = "General",
                        imageUri = imageUri,
                        date = date
                    )
                    if (id > 0) repository.updateNote(fallbackNote)
                    else repository.insertNote(fallbackNote)

                    // حفظ المهام اليدوية على الأقل
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