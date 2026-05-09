import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(private val repository: NoteRepository) : ViewModel() {

    var selectedTab by mutableIntStateOf(0)
        private set

    var aiProgress by mutableFloatStateOf(0f)
        private set

    private val _selectedTabFlow = MutableStateFlow(0)

    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ← المهام المفلترة حسب التبويب
    val tasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        _selectedTabFlow
    ) { all, tab ->
        when (tab) {
            0 -> all.filter { !it.isCompleted }
            1 -> all.filter { it.isCompleted }
            else -> all
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            allTasks.collect { all ->
                val total = all.size
                val completed = all.count { it.isCompleted }
                aiProgress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
            }
        }
    }

    fun onTabSelected(index: Int) {
        selectedTab = index
        _selectedTabFlow.value = index
    }

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            val task = allTasks.value.find { it.id == taskId } ?: return@launch
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }
}