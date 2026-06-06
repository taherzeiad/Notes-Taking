package com.example.myuniqueapp.Worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myuniqueapp.API.Groq.GroqService
import com.example.myuniqueapp.RoomDatabase.NoteDatabase
import com.example.myuniqueapp.RoomDatabase.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteAiWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getInt("note_id", -1)
        val noteDate = inputData.getString("note_date") ?: return Result.failure()

        if (noteId <= 0) return Result.failure()

        return withContext(Dispatchers.IO) {
            try {
                val db = NoteDatabase.getDatabase(applicationContext)
                val noteDao = db.noteDao()
                val taskDao = db.taskDao()

                val note = noteDao.getNoteById(noteId) ?: return@withContext Result.failure()

                // ← تخطَّ إذا كانت الفئة محددة بالفعل
                val needsClassification = note.category == "General" || note.category.isBlank()
                val content = note.content.trim()

                if (content.isBlank()) return@withContext Result.success()

                // 1. تصنيف الملاحظة
                if (needsClassification) {
                    val category = try {
                        val c = GroqService.classifyNoteContent(content)
                        when {
                            c.contains("Philo", ignoreCase = true) -> "Philosophy"
                            c.contains("Liter", ignoreCase = true) -> "Literature"
                            c.contains("Dev", ignoreCase = true) -> "Self-Development"
                            c.contains("Task", ignoreCase = true) -> "Task"
                            c.contains("Work", ignoreCase = true) -> "Work"
                            else -> "General"
                        }
                    } catch (e: Exception) {
                        Log.e("NoteAiWorker", "classify failed: ${e.message}")
                        "General"
                    }
                    noteDao.updateNote(note.copy(category = category))
                }

                // 2. استخراج المهام
                val existingTasks = taskDao.getTasksByNoteId(noteId)
                val textOnly = content.lines()
                    .filter { !it.startsWith("•") }
                    .joinToString("\n")
                    .trim()

                if (textOnly.isNotBlank()) {
                    try {
                        val aiTasks = GroqService.extractTasksFromNote(note.title, textOnly)
                        val newTasks = aiTasks.filter { aiTask ->
                            aiTask.isNotBlank() && existingTasks.none {
                                it.title.contains(aiTask, true) || aiTask.contains(it.title, true)
                            }
                        }.map { taskTitle ->
                            TaskEntity(
                                title = taskTitle,
                                source = note.title,
                                noteId = noteId,
                                date = noteDate,
                                isUrgent = false
                            )
                        }
                        if (newTasks.isNotEmpty()) {
                            taskDao.insertTasks(newTasks)
                        }
                    } catch (e: Exception) {
                        Log.e("NoteAiWorker", "extractTasks failed: ${e.message}")
                    }
                }

                Result.success()
            } catch (e: Exception) {
                Log.e("NoteAiWorker", "doWork failed: ${e.message}")
                // ← أعد المحاولة لاحقاً
                Result.retry()
            }
        }
    }
}