package com.example.notes_taking.RoomDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks_table")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val source: String = "",
    val noteId: Int = 0,
    val date: String = "",
    val isCompleted: Boolean = false,
    val isUrgent: Boolean = false
)