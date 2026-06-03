package com.example.notes_taking.RoomDatabase

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("SELECT * FROM tasks_table ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks_table WHERE noteId = :noteId")
    suspend fun deleteTasksByNoteId(noteId: Int)

    @Query("SELECT * FROM tasks_table WHERE noteId = :noteId")
    suspend fun getTasksByNoteId(noteId: Int): List<TaskEntity>
}