import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    // 1. مصدر البيانات الرئيسي من قاعدة البيانات
    private val dbNotes = repository.getAllNotes()

    // 2. بيانات وهمية للعرض عند بدء التشغيل أو التطوير
    private val mockNote = Note(
        id = 99,
        title = "تأملات في الفلسفة المعاصرة",
        content = "هذه ملاحظة وهمية من الفيو مودل. يمكنك البدء بكتابة أفكارك هنا وتنسيقها كما تشاء...",
        date = "25 ابريل 2026",
        imageUri = null
    )

    // 3. دمج البيانات (إذا كانت قاعدة البيانات فارغة، نستخدم الوهمية)
    val lastEditedNote: StateFlow<Note?> = dbNotes
        .map { notes ->
            if (notes.isEmpty()) mockNote else notes.maxByOrNull { it.id }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = mockNote // نبدأ بالقيمة الوهمية فوراً
        )
}