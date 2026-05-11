import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.TaskEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel(private val repository: NoteRepository) : ViewModel() {

    var selectedTab by mutableIntStateOf(0)
        private set
    var aiProgress by mutableFloatStateOf(0f)
        private set
    private val _selectedTabFlow = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    var isSearchActive by mutableStateOf(false)
        private set
    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ← أضف البحث للفلترة
    val tasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        _selectedTabFlow,
        _searchQuery
    ) { all, tab, query ->
        val filteredByTab = when (tab) {
            0 -> all.filter { !it.isCompleted }
            1 -> all.filter { it.isCompleted }
            else -> all
        }
        // ← فلترة بالبحث
        if (query.isBlank()) {
            filteredByTab
        } else {
            filteredByTab.filter { task ->
                task.title.contains(query, ignoreCase = true) ||
                        task.source.contains(query, ignoreCase = true)
            }
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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun openSearch() {
        isSearchActive = true
    }

    fun closeSearch() {
        isSearchActive = false
        _searchQuery.value = ""
    }
}