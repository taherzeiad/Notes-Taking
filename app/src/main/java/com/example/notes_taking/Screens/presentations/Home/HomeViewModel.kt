import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    // نقوم بجلب كل الملاحظات وتحويلها إلى StateFlow
    // ملاحظة: الـ Repository يجب أن يعيد الملاحظات مرتبة حسب التاريخ تنازلياً
    val allNotes: StateFlow<List<Note>> = repository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // يمكننا أيضاً عمل Flow مخصص فقط لآخر ملاحظة لزيادة الأداء
    val lastEditedNote: StateFlow<Note?> = allNotes
        .map { it.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // إذا أردت إضافة منطق لجلب عدد المهام المتبقية مستقبلاً
    // val pendingTasksCount: StateFlow<Int> = ...
}