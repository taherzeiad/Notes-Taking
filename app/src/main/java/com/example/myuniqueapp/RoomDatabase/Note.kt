package com.example.myuniqueapp.RoomDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val category: String,
    val imageUri: String? = null,
    val audioPaths: String? = null,
    val date: String,
    val isPinned: Boolean = false
)