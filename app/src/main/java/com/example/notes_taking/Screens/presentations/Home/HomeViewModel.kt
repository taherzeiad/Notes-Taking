package com.example.notes_taking.Screens.presentations.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    private val dbNotes = repository.getAllNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lastEditedNote: StateFlow<Note?> = dbNotes.map { notes ->
        notes.maxByOrNull { it.id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val isEmpty: StateFlow<Boolean> = dbNotes.map { it.isEmpty() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
}