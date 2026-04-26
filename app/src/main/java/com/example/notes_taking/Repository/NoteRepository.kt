package com.example.notes_taking.Repository

import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.RoomDatabase.NoteDao
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Int): Note?
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
}

// Implementation (مثال باستخدام Room)
class NoteRepositoryImpl(private val dao: NoteDao) : NoteRepository {
    override fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    override suspend fun getNoteById(id: Int): Note? = dao.getNoteById(id)
    override suspend fun insertNote(note: Note) = dao.insertNote(note)
    override suspend fun updateNote(note: Note) = dao.updateNote(note)
    override suspend fun deleteNote(note: Note) = dao.deleteNote(note)
}