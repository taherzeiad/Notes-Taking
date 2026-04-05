package com.example.notes_taking.Screens.presentations.CreateNote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.API.GroqService
import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.RoomDatabase.NoteDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {

    // حالة التحميل والخطأ
    var isLoading by mutableStateOf(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun saveNote(
        title: String, content: String, imageUri: String?, date: String, onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val note = Note(
                    title = title, content = content, imageUri = imageUri, date = date
                )
                noteDao.insertNote(note)
                onSuccess() // العودة للخلف بعد النجاح
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred"
            }
        }
    }

    // دالة إعادة الصياغة عبر Groq
    fun rephrase(content: String, onResult: (String) -> Unit) {
        executeAiTask {
            val result = GroqService.rephraseText(content)
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

    fun clearError() {
        _errorMessage.value = null
    }

    // دالة مساعدة لتجنب تكرار كود التحميل ومعالجة الأخطاء
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
class NoteViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(noteDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}