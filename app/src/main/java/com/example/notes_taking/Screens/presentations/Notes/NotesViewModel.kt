package com.example.notes_taking.Screens.presentations.Notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedNoteIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _isSelectionMode = MutableStateFlow(false)

    private val _dbNotes = repository.getAllNotes()

    val uiState: StateFlow<NotesUiState> = combine(
        _dbNotes, _searchQuery, _selectedCategory, _selectedNoteIds, _isSelectionMode
    ) { notes, query, category, selectedIds, isSelectionMode ->
        val filteredNotes = notes.filter { note ->
            val matchesSearch = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            val matchesCategory =
                category == "All" || note.category.equals(category, ignoreCase = true)
            matchesSearch && matchesCategory
        }
        NotesUiState(
            notes = filteredNotes,
            isLoading = false,
            selectedNoteIds = selectedIds,
            isSelectionMode = isSelectionMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState(isLoading = true)
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    // ======= Selection =======
    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) clearSelection()
    }

    fun toggleNoteSelection(noteId: Int) {
        val current = _selectedNoteIds.value.toMutableSet()
        if (current.contains(noteId)) current.remove(noteId)
        else current.add(noteId)
        _selectedNoteIds.value = current
        if (current.isEmpty()) _isSelectionMode.value = false
    }

    fun selectAll() {
        val allIds = uiState.value.notes.map { it.id }.toSet()
        _selectedNoteIds.value = allIds
    }

    fun clearSelection() {
        _selectedNoteIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun enterSelectionMode(noteId: Int) {
        _isSelectionMode.value = true
        _selectedNoteIds.value = setOf(noteId)
    }

    // ======= Delete =======
    fun deleteSelectedNotes(onDone: () -> Unit = {}) {
        val ids = _selectedNoteIds.value.toSet()
        if (ids.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val notes = _dbNotes.first()
            notes.filter { it.id in ids }.forEach { note ->
                repository.deleteNote(note)
                // ← حذف الصورة المرتبطة
                note.imageUri?.let { path ->
                    val file = java.io.File(path)
                    if (file.exists()) file.delete()
                }
            }
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                clearSelection()
                onDone()
            }
        }
    }

    fun deleteSingleNote(note: Note, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
            note.imageUri?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) file.delete()
            }
            kotlinx.coroutines.withContext(Dispatchers.Main) { onDone() }
        }
    }
}