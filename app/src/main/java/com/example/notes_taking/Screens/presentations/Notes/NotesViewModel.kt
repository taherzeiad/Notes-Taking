package com.example.notes_taking.Screens.presentations.Notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.Repository.NoteRepository
import kotlinx.coroutines.flow.*

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _dbNotes = repository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notesState = combine(_dbNotes, _searchQuery, _selectedCategory) { notes, query, category ->
        notes.filter { note ->
            val matchesSearch = if (query.isBlank()) true
            else note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)

            if (category == "All" || category == "الكل") return@filter matchesSearch

            val matchesCategory = when (category) {
                "Philosophy" -> note.category.equals("Philosophy", true) ||
                        note.category.contains("فلسفة")
                "Literature" -> note.category.equals("Literature", true) ||
                        note.category.contains("أدب")
                "Self-Development" -> note.category.equals("Self-Development", true) ||
                        note.category.contains("تطوير")
                else -> note.category.equals(category, ignoreCase = true)
            }

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