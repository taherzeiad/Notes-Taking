package com.example.notes_taking.RoomDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("SELECT * FROM notes_table ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: Int): Note?

    @Update
    suspend fun updateNote(note: Note)
    @Delete
    suspend fun deleteNote(note: Note)

    // جلب ملاحظات يوم معين
    @Query("SELECT * FROM notes_table WHERE date = :date ORDER BY id DESC")
    fun getNotesByDate(date: String): Flow<List<Note>>

    // جلب ملاحظات الأيام الماضية (آخر 7 أيام)
    @Query("SELECT * FROM notes_table ORDER BY id DESC LIMIT 50")
    suspend fun getRecentNotes(): List<Note>
}