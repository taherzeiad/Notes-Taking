package com.example.notes_taking.Screens.presentations.Notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.R
import com.example.notes_taking.Repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    // 1. حالة البحث
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 2. القسم المختار (Category)
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // 3. قائمة البيانات الوهمية (يمكنك لاحقاً استبدال repository.getAllNotes() بها)
    private val _mockNotes = MutableStateFlow(getSampleData())

    // 4. دمج البحث مع الأقسام مع البيانات (المنطق الأساسي)
    val notesState =
        combine(_mockNotes, _searchQuery, _selectedCategory) { notes, query, category ->
            notes.filter { note ->
                val matchesSearch = if (query.isBlank()) true
                else note.title.contains(query, ignoreCase = true)

                val matchesCategory = if (category == "All" || category == "الكل") true
                else note.category == category

                matchesSearch && matchesCategory
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    // بيانات وهمية منظمة لسهولة الفلترة
    private fun getSampleData() = listOf(
        NoteCardData(
            id = 1,
            tagRes = R.string.note_tag_philosophy,
            dateRes = R.string.note_date_1,
            title = "Philosophy of Mind",
            titleRes = R.string.notes_title,
            contentRes = R.string.notes_subtitle,
            category = "Philosophy"
        ),
        NoteCardData(
            id = 2,
            tagRes = R.string.note_tag_literature,
            title = "Modern Literature",
            titleRes = R.string.note_cat_literature,
            contentRes = R.string.notes_subtitle,
            isItalic = true,
            category = "Literature"
        ),
        NoteCardData(
            id = 3,
            title = "Visual Notes",
            titleRes = R.string.notes_screen_title_bar,
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400",
            type = NoteCardType.IMAGE,
            category = "All"
        )
    )
}

data class NoteCardData(
    val id: Int,
    val tagRes: Int = 0,
    val dateRes: Int = 0,
    val title: String = "", // أضف هذا الحقل للبحث
    val titleRes: Int,
    val contentRes: Int = 0,
    val imageUrl: String? = null,
    val isItalic: Boolean = false,
    val bulletsRes: List<Int> = emptyList(),
    val type: NoteCardType = NoteCardType.TEXT,
    val category: String = "All" // أضفنا هذا الحقل للفلترة
)

enum class NoteCardType { TEXT, IMAGE, BULLETS }