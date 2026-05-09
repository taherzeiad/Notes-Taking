package com.example.notes_taking.Repository

import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.RoomDatabase.NoteDao
import com.example.notes_taking.RoomDatabase.TaskDao
import com.example.notes_taking.RoomDatabase.TaskEntity
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Int): Note?
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    fun getNotesByDate(date: String): Flow<List<Note>>
    suspend fun getRecentNotes(): List<Note>
    suspend fun getLastNote(): Note?

    // Task operations
    fun getAllTasks(): Flow<List<TaskEntity>>
    suspend fun insertTasks(tasks: List<TaskEntity>)
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTasksByNoteId(noteId: Int)
}

class NoteRepositoryImpl(
    private val dao: NoteDao, private val taskDao: TaskDao
) : NoteRepository {
    override fun getAllNotes() = dao.getAllNotes()
    override suspend fun getNoteById(id: Int) = dao.getNoteById(id)
    override suspend fun insertNote(note: Note) = dao.insertNote(note)
    override suspend fun updateNote(note: Note) = dao.updateNote(note)
    override suspend fun deleteNote(note: Note) = dao.deleteNote(note)
    override fun getNotesByDate(date: String) = dao.getNotesByDate(date)
    override suspend fun getRecentNotes() = dao.getRecentNotes()
    override suspend fun getLastNote() = dao.getLastNote()

    override fun getAllTasks() = taskDao.getAllTasks()
    override suspend fun insertTasks(tasks: List<TaskEntity>) = taskDao.insertTasks(tasks)
    override suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    override suspend fun deleteTasksByNoteId(noteId: Int) = taskDao.deleteTasksByNoteId(noteId)
}