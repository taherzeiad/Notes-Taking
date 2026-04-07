package com.example.notes_taking.Screens.presentations.CreateNote

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.API.GroqService
import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.RoomDatabase.NoteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    var isLoading by mutableStateOf(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun saveNote(
        id: Int = 0,
        title: String,
        content: String,
        imageUri: String?,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val note = Note(
                    id = id, title = title, content = content, imageUri = imageUri, date = date
                )
                noteDao.insertNote(note)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred"
            }
        }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return noteDao.getNoteById(noteId)
    }

    // دالة إعادة الصياغة عبر Groq
    fun rephrase(content: String, onResult: (String) -> Unit) {
        executeAiTask {
            val result = GroqService.raseText(content)
            onResult(result)
        }
    }

    // دالة التشكيل عبر Groq
    fun diacritize(content: String, onResult: (String) -> Unit) {
        executeAiTask {
            val result = GroqService.diacritizeText(content)
            onResult(result)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteDao.deleteNote(note)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete note"
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteDao.insertNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun updateNote(
        id: Int,
        title: String,
        content: String,
        imageUri: String?,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val note = Note(
                    id = id, title = title, content = content, imageUri = imageUri, date = date
                )
                noteDao.insertNote(note)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث الملاحظة"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun executeAiTask(block: suspend () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            _errorMessage.value = null
            try {
                block()
            } catch (e: Exception) {
                _errorMessage.value = "AI Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}

fun saveImageToInternalStorage(context: Context, uri: Uri): Uri? {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.filesDir, "note_image_${System.currentTimeMillis()}.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return Uri.fromFile(file)
}

class NoteViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return NoteViewModel(noteDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}