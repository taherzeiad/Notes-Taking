package com.example.notes_taking.Repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notes_taking.Screens.presentations.Editor.NoteViewModel
import com.example.notes_taking.Screens.presentations.Home.HomeViewModel
import com.example.notes_taking.Screens.presentations.Notes.NotesViewModel
import com.example.notes_taking.Screens.presentations.Summary.SummaryViewModel

@Suppress("UNCHECKED_CAST")
class GenericViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> {
                NotesViewModel(repository) as T
            }
            modelClass.isAssignableFrom(NoteViewModel::class.java) -> {
                NoteViewModel(repository) as T
            }
            // أضف هذا الجزء لتعريف الـ SummaryViewModel
            modelClass.isAssignableFrom(SummaryViewModel::class.java) -> {
                SummaryViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}