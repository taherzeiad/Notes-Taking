package com.example.notes_taking.Screens.presentations.Editor

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.API.GroqService
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    /**
     * جلب ملاحظة بواسطة المعرف (ID)
     */
    suspend fun getNoteById(id: Int): Note? {
        return if (id > 0) {
            withContext(Dispatchers.IO) {
                try {
                    repository.getNoteById(id)
                } catch (e: Exception) {
                    Log.e("NoteViewModel", "Error getting note by id: ${e.message}")
                    null
                }
            }
        } else {
            null
        }
    }

    /**
     * حفظ ملاحظة مع الذكاء الاصطناعي
     */
    fun saveNoteWithAI(
        id: Int,
        title: String,
        content: String,
        imageUri: String?,
        date: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // التحقق من صحة المدخلات الأساسية
                val finalTitle = if (title.isBlank()) {
                    // إنشاء عنوان تلقائي من أول 30 حرفاً من المحتوى
                    val autoTitle = content.take(30).trim()
                    if (autoTitle.isBlank()) "ملاحظة جديدة" else autoTitle
                } else {
                    title.trim()
                }

                val finalContent = content.trim()

                var autoCategory = "General"

                // استدعاء الذكاء الاصطناعي فقط إذا كان المحتوى غير فارغ
                if (finalContent.isNotBlank()) {
                    try {
                        val classification = GroqService.classifyNoteContent(finalContent)
                        autoCategory = when {
                            classification.contains("Philo", ignoreCase = true) ||
                                    classification.contains("فلسفة", ignoreCase = true) -> "Philosophy"
                            classification.contains("Liter", ignoreCase = true) ||
                                    classification.contains("أدب", ignoreCase = true) -> "Literature"
                            classification.contains("Dev", ignoreCase = true) ||
                                    classification.contains("تطوير", ignoreCase = true) -> "Self-Development"
                            else -> "General"
                        }
                    } catch (e: Exception) {
                        Log.e("AI_SAVE", "AI classification failed: ${e.message}")
                        autoCategory = "General"
                    }
                }

                val noteToSave = Note(
                    id = if (id > 0) id else 0,
                    title = finalTitle,
                    content = finalContent,
                    category = autoCategory,
                    imageUri = imageUri,
                    date = date
                )

                // حفظ الملاحظة
                if (id > 0) {
                    repository.updateNote(noteToSave)
                } else {
                    repository.insertNote(noteToSave)
                }

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("AI_SAVE", "Error during save: ${e.message}")

                // حفظ الملاحظة بدون AI في حالة الفشل التام
                try {
                    val fallbackTitle = if (title.isBlank()) "ملاحظة جديدة" else title.trim()
                    val fallbackNote = Note(
                        id = if (id > 0) id else 0,
                        title = fallbackTitle,
                        content = content.trim(),
                        category = "General",
                        imageUri = imageUri,
                        date = date
                    )

                    if (id > 0) {
                        repository.updateNote(fallbackNote)
                    } else {
                        repository.insertNote(fallbackNote)
                    }

                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                } catch (fallbackError: Exception) {
                    withContext(Dispatchers.Main) {
                        onError("فشل في حفظ الملاحظة: ${fallbackError.message}")
                    }
                }
            }
        }
    }

    /**
     * حفظ ملاحظة جديدة (بدون AI)
     */
    fun saveNote(
        title: String,
        content: String,
        imagePath: String?,
        date: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val finalTitle = if (title.isBlank()) "ملاحظة جديدة" else title.trim()
                val finalContent = content.trim()

                val newNote = Note(
                    title = finalTitle,
                    content = finalContent,
                    imageUri = imagePath,
                    date = date,
                    category = "General"
                )
                repository.insertNote(newNote)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("فشل في حفظ الملاحظة: ${e.message}")
                }
            }
        }
    }

    /**
     * تحديث ملاحظة موجودة
     */
    fun updateNote(
        id: Int,
        title: String,
        content: String,
        imagePath: String?,
        date: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val finalTitle = if (title.isBlank()) "ملاحظة جديدة" else title.trim()
                val finalContent = content.trim()

                val updatedNote = Note(
                    id = id,
                    title = finalTitle,
                    content = finalContent,
                    imageUri = imagePath,
                    date = date,
                    category = "General"
                )
                repository.updateNote(updatedNote)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("فشل في تحديث الملاحظة: ${e.message}")
                }
            }
        }
    }

    /**
     * حفظ الصورة في التخزين الداخلي
     */
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

    /**
     * حذف الملاحظة
     */
    fun deleteNote(note: Note, onDeleteSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteNote(note)
                withContext(Dispatchers.Main) {
                    onDeleteSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("فشل في حذف الملاحظة: ${e.message}")
                }
            }
        }
    }
}