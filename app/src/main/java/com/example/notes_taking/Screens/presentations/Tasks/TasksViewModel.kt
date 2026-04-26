package com.example.notes_taking.Screens.presentations.Tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TasksViewModel : ViewModel() {

    var selectedTab by mutableIntStateOf(0)
        private set

    // القائمة الكاملة (كمصدر وحيد للحقيقة داخل الـ ViewModel)
    private var allTasks = sampleTasks

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    var aiProgress by mutableFloatStateOf(0f)
        private set

    init {
        filterTasksByTab(0) // تحميل مهام "قيد التنفيذ" عند البدء
        updateAiProgress()
    }

    fun onTabSelected(index: Int) {
        selectedTab = index
        filterTasksByTab(index)
    }

    private fun filterTasksByTab(index: Int) {
        val status = when (index) {
            0 -> TaskStatus.IN_PROGRESS
            1 -> TaskStatus.COMPLETED
            else -> TaskStatus.SCHEDULED
        }
        _tasks.value = allTasks.filter { it.status == status }
    }

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            // تحديث الحالة في المصدر الرئيسي
            allTasks = allTasks.map { task ->
                if (task.id == taskId) {
                    val newStatus = if (task.status == TaskStatus.COMPLETED)
                        TaskStatus.IN_PROGRESS else TaskStatus.COMPLETED
                    task.copy(status = newStatus)
                } else task
            }
            // إعادة الفلترة لتحديث الواجهة
            filterTasksByTab(selectedTab)
            updateAiProgress()
        }
    }

    private fun updateAiProgress() {
        val total = allTasks.size
        val completed = allTasks.count { it.status == TaskStatus.COMPLETED }
        if (total > 0) {
            aiProgress = completed.toFloat() / total.toFloat()
        }
    }
}