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

    // 1. إدارة التبويب المختار (0: In Progress, 1: Completed, 2: Scheduled)
    var selectedTab by mutableIntStateOf(0)
        private set

    // 2. حل مشكلة Unresolved reference بجعل القائمة تبدأ فارغة أو ببيانات أولية داخل الـ ViewModel
    // وحل مشكلة Cannot infer type بتحديد النوع صراحة <List<Task>>
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    // 3. بيانات الـ AI Insights
    var aiProgress by mutableFloatStateOf(0.7f)
        private set

    init {
        // تحميل البيانات عند تشغيل الـ ViewModel
        loadTasks()
    }

    private fun loadTasks() {
        // يمكنك لاحقاً استبدال هذا بجلب البيانات من Repository
        _tasks.value = sampleTasks
    }

    // منطق تبديل التبويب
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
        // فلترة القائمة بناءً على الحالة
        _tasks.value = sampleTasks.filter { it.status == status }
    }

    // منطق إكمال المهمة
    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            // تحديث الحالة في القائمة الحالية (مؤقتاً حتى نستخدم قاعدة البيانات)
            val updatedList = _tasks.value.map { task ->
                if (task.id == taskId) {
                    val newStatus = if (task.status == TaskStatus.COMPLETED)
                        TaskStatus.IN_PROGRESS else TaskStatus.COMPLETED
                    task.copy(status = newStatus)
                } else task
            }
            _tasks.value = updatedList

            // تحديث شريط التقدم AI بناءً على المهام المكتملة
            updateAiProgress()
        }
    }

    private fun updateAiProgress() {
        val total = sampleTasks.size
        val completed = _tasks.value.count { it.status == TaskStatus.COMPLETED }
        if (total > 0) {
            aiProgress = completed.toFloat() / total.toFloat()
        }
    }
}