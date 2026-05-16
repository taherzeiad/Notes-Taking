package com.example.notes_taking.Repository

import TasksViewModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notes_taking.Screens.presentations.Editor.NoteViewModel
import com.example.notes_taking.Screens.presentations.Home.HomeViewModel
import com.example.notes_taking.Screens.presentations.Notes.NotesViewModel
import com.example.notes_taking.Screens.presentations.Summary.SummaryViewModel

@Suppress("UNCHECKED_CAST")
class GenericViewModelFactory(
    private val repository: NoteRepository, private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository) as T
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> NotesViewModel(repository) as T
            modelClass.isAssignableFrom(NoteViewModel::class.java) -> NoteViewModel(
                repository, context
            ) as T

            modelClass.isAssignableFrom(SummaryViewModel::class.java) -> SummaryViewModel(repository) as T
            modelClass.isAssignableFrom(TasksViewModel::class.java) -> TasksViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}