package com.example.myuniqueapp.Screens.presentations.Notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myuniqueapp.Repository.NoteRepository
import com.example.myuniqueapp.RoomDatabase.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _dbNotes = repository.getAllNotes()

    val uiState: StateFlow<NotesUiState> =
        combine(_dbNotes, _searchQuery, _selectedCategory) { notes, query, category ->
            val filteredNotes = notes.filter { note ->
                val matchesSearch = query.isBlank() ||
                        note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true)
                val matchesCategory =
                    category == "All" || note.category.equals(category, ignoreCase = true)
                matchesSearch && matchesCategory
            }
            NotesUiState(notes = filteredNotes, isLoading = false)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesUiState(isLoading = true)
        )

    fun onSearchQueryChange(newQuery: String) { _searchQuery.value = newQuery }
    fun onCategoryChange(category: String) { _selectedCategory.value = category }

    fun deleteNote(note: Note, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
            note.imageUri?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) file.delete()
            }
            withContext(Dispatchers.Main) { onDone() }
        }
    }
}