package com.example.notes_taking.Screens.presentations.Notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.API.GroqService
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _dbNotes = repository.getAllNotes()
    val notesState = combine(_dbNotes, _searchQuery, _selectedCategory) { notes, query, category ->
        notes.filter { note ->
            val matchesSearch = if (query.isBlank()) true
            else note.title.contains(query, ignoreCase = true) || note.content.contains(
                query,
                ignoreCase = true
            )

            val matchesCategory = if (category == "All" || category == "الكل") true
            else note.category.equals(category, ignoreCase = true)

            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }
}

data class NoteCardData(
    val id: Int,
    val tagRes: Int = 0,
    val dateRes: Int = 0,
    val title: String = "",
    val titleRes: Int,
    val contentRes: Int = 0,
    val imageUrl: String? = null,
    val isItalic: Boolean = false,
    val bulletsRes: List<Int> = emptyList(),
    val type: NoteCardType = NoteCardType.TEXT,
    val category: String = "All"
)

enum class NoteCardType { TEXT, IMAGE, BULLETS }