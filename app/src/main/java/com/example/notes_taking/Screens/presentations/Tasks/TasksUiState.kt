package com.example.notes_taking.Screens.presentations.Tasks

import com.example.notes_taking.RoomDatabase.TaskEntity

data class TasksUiState(
    val selectedTab: Int = 0,

    val filteredTasks: List<TaskEntity> = emptyList(),
    val allTasks: List<TaskEntity> = emptyList(),

    val isSearchActive: Boolean = false,
    val searchQuery: String = "",

    val aiProgress: Float = 0f,
    val completedCount: Int = 0,
    val totalCount: Int = 0,

    val sourceGroups: List<SourceGroup> = emptyList(),
)

data class SourceGroup(
    val source: String,
    val tasks: List<TaskEntity>,
)