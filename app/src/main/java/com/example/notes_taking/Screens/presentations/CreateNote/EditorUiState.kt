package com.example.notes_taking.Screens.presentations.CreateNote

import com.example.notes_taking.Screens.presentations.Editor.ContentBlock

data class EditorUiState(
    val title: String = "",
    val contentBlocks: List<ContentBlock> = listOf(ContentBlock.TextBlock()),
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isAiLoading: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val wordCount: Int = 0,
    val readingMinutes: Int = 1,
    val characterCount: Int = 0,
    val snackbarMessage: String? = null,
    val shouldNavigateBack: Boolean = false
)