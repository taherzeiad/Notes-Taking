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
     * نستخدم [withContext] لضمان أن العملية تتم في خيط الخلفية (IO Thread)
     */
    suspend fun getNoteById(id: Int): Note? {
        return withContext(Dispatchers.IO) {
            repository.getNoteById(id)
        }
    }

    /**
     * حفظ ملاحظة جديدة في قاعدة البيانات
     */
    fun saveNote(
        title: String,
        content: String,
        imagePath: String?,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newNote = Note(
                title = title,
                content = content,
                imageUri = imagePath,
                date = date
            )
            repository.insertNote(newNote)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
    fun saveNoteWithAI(id: Int, title: String, content: String, imageUri: String?, date: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { // نستخدم IO للعمليات الثقيلة
            try {
                // 1. تنظيف النص لضمان أفضل نتيجة من الـ AI
                val cleanContent = content.trim()

                // 2. استدعاء الذكاء الاصطناعي
                // تأكد أن classifyNoteContent تعيد تصنيفاً مطابقاً للقيم التي تستخدمها في التطبيق
                var autoCategory = if (cleanContent.isNotBlank()) {
                    GroqService.classifyNoteContent(cleanContent)
                } else {
                    "General"
                }

                // 3. تصحيح يدوي بسيط لضمان مطابقة التصنيفات في التطبيق (All, Philosophy, Literature, Self-Development)
                // هذا الجزء يضمن أن الـ AI لو رد بكلمة عربية أو بصيغة مختلفة يتم تحويلها للقيمة البرمجية الصحيحة
                autoCategory = when {
                    autoCategory.contains("Philo", ignoreCase = true) || autoCategory.contains("فلسفة") -> "Philosophy"
                    autoCategory.contains("Liter", ignoreCase = true) || autoCategory.contains("أدب") -> "Literature"
                    autoCategory.contains("Dev", ignoreCase = true) || autoCategory.contains("تطوير") -> "Self-Development"
                    else -> "General"
                }

                val noteToSave = Note(
                    id = if (id > 0) id else 0,
                    title = title.ifBlank { "Smart Note - ${System.currentTimeMillis() % 1000}" },
                    content = content,
                    category = autoCategory,
                    imageUri = imageUri,
                    date = date
                )

                // 4. الحفظ
                repository.insertNote(noteToSave)

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("AI_SAVE", "Error during AI classification: ${e.message}")
                // في حال فشل الـ AI، نحفظ الملاحظة كـ General ولا نعطل المستخدم
                val fallbackNote = Note(id = if (id > 0) id else 0, title = title, content = content, category = "General", imageUri = imageUri, date = date)
                repository.insertNote(fallbackNote)
                withContext(Dispatchers.Main) { onComplete() }
            }
        }
    }

    /**
     * تحديث ملاحظة موجودة مسبقاً
     */
    fun updateNote(
        id: Int,
        title: String,
        content: String,
        imagePath: String?,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedNote = Note(
                id = id,
                title = title,
                content = content,
                imageUri = imagePath,
                date = date
            )
            repository.updateNote(updatedNote)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            // فتح تيار بيانات من الـ Uri
            val inputStream = context.contentResolver.openInputStream(uri)
            // إنشاء اسم ملف فريد
            val fileName = "note_image_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * حذف الملاحظة (اختياري ولكن مفيد لشاشة المحرر)
     */
    fun deleteNote(note: Note, onDeleteSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
            withContext(Dispatchers.Main) {
                onDeleteSuccess()
            }
        }
    }
}