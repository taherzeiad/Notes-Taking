package com.example.notes_taking.Screens.presentations.CreateNote

import com.example.notes_taking.Screens.presentations.Editor.ContentBlock
import com.example.notes_taking.Screens.presentations.Editor.RateLimitState

data class EditorUiState(
    // ── محتوى ──────────────────────────────────────────────
    val title: String = "",
    val contentBlocks: List<ContentBlock> = listOf(ContentBlock.TextBlock()),
    val isBold: Boolean = false,
    val isItalic: Boolean = false,

    // ── تحميل / حفظ ────────────────────────────────────────
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isAiLoading: Boolean = false,

    // ── إحصاءات ────────────────────────────────────────────
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val readingMinutes: Int = 1,

    // ── أحداث ──────────────────────────────────────────────
    val snackbarMessage: String? = null,
    val shouldNavigateBack: Boolean = false,

    // ── Rate Limit ──────────────────────────────────────────
    val rateLimitState: RateLimitState = RateLimitState(),
)