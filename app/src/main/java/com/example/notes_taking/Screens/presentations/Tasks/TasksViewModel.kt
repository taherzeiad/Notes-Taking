package com.example.notes_taking.Screens.presentations.Tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.TaskEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel(private val repository: NoteRepository) : ViewModel() {

    // ── Private mutable intents ───────────────────────────────────────────────
    private val _selectedTab   = MutableStateFlow(0)
    private val _searchQuery   = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)

    // ── Single source of truth exposed to the UI ──────────────────────────────
    val uiState: StateFlow<TasksUiState> = combine(
        repository.getAllTasks(),
        _selectedTab,
        _searchQuery,
        _isSearchActive,
    ) { all, tab, query, searchActive ->
        buildUiState(
            allTasks      = all,
            selectedTab   = tab,
            searchQuery   = query,
            isSearchActive = searchActive,
        )
    }.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5_000),
        initialValue  = TasksUiState(),
    )

    // ── User intents ──────────────────────────────────────────────────────────

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun openSearch() {
        _isSearchActive.value = true
    }

    fun closeSearch() {
        _isSearchActive.value = false
        _searchQuery.value = ""
    }

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            val task = uiState.value.allTasks.find { it.id == taskId } ?: return@launch
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun buildUiState(
        allTasks: List<TaskEntity>,
        selectedTab: Int,
        searchQuery: String,
        isSearchActive: Boolean,
    ): TasksUiState {
        val completedCount = allTasks.count { it.isCompleted }
        val totalCount     = allTasks.size
        val aiProgress     = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

        val filteredByTab = when (selectedTab) {
            0    -> allTasks.filter { !it.isCompleted }
            1    -> allTasks.filter { it.isCompleted }
            else -> allTasks
        }

        val filteredTasks = if (searchQuery.isBlank()) {
            filteredByTab
        } else {
            filteredByTab.filter { task ->
                task.title.contains(searchQuery, ignoreCase = true) ||
                        task.source.contains(searchQuery, ignoreCase = true)
            }
        }

        val sourceGroups = allTasks
            .groupBy { it.source.ifBlank { "غير محدد" } }
            .map { (source, tasks) -> SourceGroup(source, tasks) }

        return TasksUiState(
            selectedTab    = selectedTab,
            filteredTasks  = filteredTasks,
            allTasks       = allTasks,
            isSearchActive = isSearchActive,
            searchQuery    = searchQuery,
            aiProgress     = aiProgress,
            completedCount = completedCount,
            totalCount     = totalCount,
            sourceGroups   = sourceGroups,
        )
    }
}