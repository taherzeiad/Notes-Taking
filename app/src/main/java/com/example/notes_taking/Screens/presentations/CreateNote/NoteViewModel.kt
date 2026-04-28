package com.example.notes_taking.Screens.presentations.Editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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