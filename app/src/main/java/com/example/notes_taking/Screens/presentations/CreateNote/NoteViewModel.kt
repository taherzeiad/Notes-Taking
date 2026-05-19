package com.example.notes_taking.Screens.presentations.Editor

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes_taking.API.GroqService
import com.example.notes_taking.R
import com.example.notes_taking.Repository.NoteRepository
import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.RoomDatabase.TaskEntity
import com.example.notes_taking.Screens.presentations.CreateNote.EditorUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NoteViewModel(
    private val repository: NoteRepository,
    private val appContext: Context,
) : ViewModel() {

    private val aiRateLimiter = AiRateLimiter.getInstance()

    init {
        startRateLimitTicker()
    }

    private val privacyPrefs by lazy {
        appContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** استخراج نص من string resources بدون Composable context */
    private fun str(resId: Int): String = appContext.getString(resId)
    private fun str(resId: Int, vararg args: Any): String = appContext.getString(resId, *args)

    // ── Title ─────────────────────────────────────────────────────────────────

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    fun toggleBold() = _uiState.update { it.copy(isBold = !it.isBold) }
    fun toggleItalic() = _uiState.update { it.copy(isItalic = !it.isItalic) }

    // ── Content Blocks ────────────────────────────────────────────────────────

    fun updateBlock(index: Int, block: ContentBlock) {
        val updated = _uiState.value.contentBlocks.toMutableList()
        if (index in updated.indices) {
            updated[index] = block
            _uiState.update { it.copy(contentBlocks = updated) }
            recalculateCounts(updated)
        }
    }

    fun addBlock(block: ContentBlock) {
        val updated = _uiState.value.contentBlocks.toMutableList().also { it.add(block) }
        _uiState.update { it.copy(contentBlocks = updated) }
        recalculateCounts(updated)
    }

    fun addBlockAt(index: Int, block: ContentBlock) {
        val updated = _uiState.value.contentBlocks.toMutableList().also { it.add(index + 1, block) }
        _uiState.update { it.copy(contentBlocks = updated) }
        recalculateCounts(updated)
    }

    fun removeBlock(index: Int) {
        val updated = _uiState.value.contentBlocks.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _uiState.update { it.copy(contentBlocks = updated) }
            recalculateCounts(updated)
        }
    }

    fun addBulletBlock() = addBlock(ContentBlock.BulletBlock())

    fun addLinkBlock(url: String) {
        if (url.isBlank()) return
        val finalUrl =
            if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
        val updated = _uiState.value.contentBlocks.toMutableList().also {
            it.add(ContentBlock.LinkBlock(url = finalUrl))
            it.add(ContentBlock.TextBlock())
        }
        _uiState.update { it.copy(contentBlocks = updated) }
    }

    fun addImageBlock(uri: Uri) {
        viewModelScope.launch {
            val permanentPath = withContext(Dispatchers.IO) { saveImageToInternalStorage(uri) }
            if (permanentPath != null) {
                val updated = _uiState.value.contentBlocks.toMutableList().also {
                    it.add(ContentBlock.ImageBlock(uri = Uri.fromFile(File(permanentPath))))
                    it.add(ContentBlock.TextBlock())
                }
                _uiState.update { it.copy(contentBlocks = updated) }
                recalculateCounts(updated)
            } else {
                showSnackbar(str(R.string.error_add_image))
            }
        }
    }

    fun addAudioBlock(uri: Uri) {
        val name =
            uri.lastPathSegment?.substringAfterLast("/") ?: "تسجيل_${System.currentTimeMillis()}"
        val updated = _uiState.value.contentBlocks.toMutableList().also {
            it.add(ContentBlock.AudioBlock(uri = uri, name = name))
            it.add(ContentBlock.TextBlock())
        }
        _uiState.update { it.copy(contentBlocks = updated) }
    }

    fun addRecordedAudioBlock(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return
        val updated = _uiState.value.contentBlocks.toMutableList().also {
            it.add(
                ContentBlock.AudioBlock(
                    uri = Uri.fromFile(file),
                    name = file.name,
                    filePath = file.absolutePath,
                )
            )
            it.add(ContentBlock.TextBlock())
        }
        _uiState.update { it.copy(contentBlocks = updated) }
    }

    // ── Load Note ─────────────────────────────────────────────────────────────

    fun loadNote(noteId: Int) {
        if (noteId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val note = withContext(Dispatchers.IO) { repository.getNoteById(noteId) }
                note?.let {
                    val blocks = buildList {
                        add(
                            if (it.content.isNotBlank()) ContentBlock.TextBlock(text = it.content)
                            else ContentBlock.TextBlock()
                        )
                        it.imageUri?.let { path ->
                            val f = File(path)
                            if (f.exists()) add(ContentBlock.ImageBlock(uri = Uri.fromFile(f)))
                        }
                        it.audioPaths?.split(",")?.forEach { audioPath ->
                            if (audioPath.isNotBlank()) {
                                val f = File(audioPath)
                                if (f.exists()) add(
                                    ContentBlock.AudioBlock(
                                        uri = Uri.fromFile(f),
                                        name = f.name,
                                        filePath = audioPath,
                                    )
                                )
                            }
                        }
                    }
                    _uiState.update { _ ->
                        EditorUiState(
                            title = note.title, contentBlocks = blocks
                        )
                    }
                    recalculateCounts(blocks)
                }
            } catch (e: Exception) {
                Log.e("NoteViewModel", "loadNote: ${e.message}")
                showSnackbar(str(R.string.error_load_note))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    fun hasContent(): Boolean {
        val s = _uiState.value
        return s.title.isNotBlank() || s.contentBlocks.any {
            (it is ContentBlock.TextBlock && it.text.isNotBlank()) || it is ContentBlock.ImageBlock
        }
    }

    // ── AI — Rephrase ─────────────────────────────────────────────────────────

    private fun startRateLimitTicker() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1_000)
                aiRateLimiter.tick()
                _uiState.update { it.copy(rateLimitState = aiRateLimiter.state.value) }
            }
        }
    }

    fun rephraseText() {
        val text = _uiState.value.contentBlocks
            .filterIsInstance<ContentBlock.TextBlock>()
            .joinToString("\n") { it.text }.trim()

        if (text.isBlank()) {
            showSnackbar(str(R.string.error_no_text_rephrase)); return
        }

        if (!aiRateLimiter.tryConsume()) {
            _uiState.update { it.copy(rateLimitState = aiRateLimiter.state.value) }
            showSnackbar(
                str(
                    R.string.error_ai_rate_limit,
                    aiRateLimiter.state.value.secondsRemaining
                )
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) { GroqService.rephraseText(text) }
                val updated = _uiState.value.contentBlocks.toMutableList()
                val i = updated.indexOfFirst { it is ContentBlock.TextBlock }
                if (i != -1) updated[i] = ContentBlock.TextBlock(text = result)
                _uiState.update { it.copy(contentBlocks = updated, isAiLoading = false) }
                recalculateCounts(updated)
                showSnackbar(str(R.string.success_rephrase))
            } catch (e: Exception) {
                Log.e("NoteViewModel", "rephraseText: ${e.message}")
                aiRateLimiter.refundCall()
                _uiState.update {
                    it.copy(
                        isAiLoading = false,
                        rateLimitState = aiRateLimiter.state.value
                    )
                }
                showSnackbar(str(R.string.error_ai_failed, e.message ?: ""))
            }
        }
    }

    fun diacritizeText() {
        val text = _uiState.value.contentBlocks
            .filterIsInstance<ContentBlock.TextBlock>()
            .joinToString("\n") { it.text }.trim()

        if (text.isBlank()) {
            showSnackbar(str(R.string.error_no_text_diacritize)); return
        }

        if (!aiRateLimiter.tryConsume()) {
            _uiState.update { it.copy(rateLimitState = aiRateLimiter.state.value) }
            showSnackbar(
                str(
                    R.string.error_ai_rate_limit,
                    aiRateLimiter.state.value.secondsRemaining
                )
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) { GroqService.diacritizeText(text) }
                val updated = _uiState.value.contentBlocks.toMutableList()
                val i = updated.indexOfFirst { it is ContentBlock.TextBlock }
                if (i != -1) updated[i] = ContentBlock.TextBlock(text = result)
                _uiState.update { it.copy(contentBlocks = updated, isAiLoading = false) }
                recalculateCounts(updated)
                showSnackbar(str(R.string.success_diacritize))
            } catch (e: Exception) {
                Log.e("NoteViewModel", "diacritizeText: ${e.message}")
                aiRateLimiter.refundCall() // ← أعد المحاولة عند فشل الـ API
                _uiState.update {
                    it.copy(
                        isAiLoading = false,
                        rateLimitState = aiRateLimiter.state.value
                    )
                }
                showSnackbar(str(R.string.error_ai_failed, e.message ?: ""))
            }
        }
    }
    // ── Save Note ─────────────────────────────────────────────────────────────

    fun saveNote(noteId: Int, date: String) {
        if (!hasContent()) {
            showSnackbar(str(R.string.error_no_content_save)); return
        }

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { _uiState.update { it.copy(isSaving = true) } }
            val state = _uiState.value
            try {
                val blocks = state.contentBlocks
                val finalTitle = state.title.trim().ifBlank {
                    blocks.filterIsInstance<ContentBlock.TextBlock>().firstOrNull()?.text?.take(30)
                        ?.trim() ?: str(R.string.default_note_title2)
                }
                val fullContent = blocks.joinToString("\n") { b ->
                    when (b) {
                        is ContentBlock.TextBlock -> b.text
                        is ContentBlock.BulletBlock -> "• ${b.text}"
                        else -> ""
                    }
                }.trim()

                val imageUri =
                    blocks.filterIsInstance<ContentBlock.ImageBlock>().firstOrNull()?.uri?.path
                val audioPaths = blocks.filterIsInstance<ContentBlock.AudioBlock>()
                    .joinToString(",") { it.filePath }.takeIf { isVoiceStorageEnabled() }
                val manualTasks =
                    blocks.filterIsInstance<ContentBlock.BulletBlock>().map { it.text.trim() }
                        .filter { it.isNotBlank() }

                var autoCategory = "General"
                if (isAiProcessingEnabled() && fullContent.isNotBlank()) {
                    runCatching {
                        GroqService.classifyNoteContent(fullContent).let { c ->
                            autoCategory = when {
                                c.contains("Philo", ignoreCase = true) -> "Philosophy"
                                c.contains("Liter", ignoreCase = true) -> "Literature"
                                c.contains("Dev", ignoreCase = true) -> "Self-Development"
                                c.contains("Task", ignoreCase = true) -> "Task"
                                c.contains("Work", ignoreCase = true) -> "Work"
                                else -> "General"
                            }
                        }
                    }.onFailure { Log.e("NoteViewModel", "classify: ${it.message}") }
                }

                val note = Note(
                    id = if (noteId > 0) noteId else 0,
                    title = finalTitle,
                    content = fullContent,
                    audioPaths = audioPaths,
                    category = autoCategory,
                    imageUri = imageUri,
                    date = date,
                )
                if (noteId > 0) repository.updateNote(note) else repository.insertNote(note)

                val savedId = if (noteId > 0) noteId else repository.getLastNote()?.id ?: 0
                if (savedId > 0) repository.deleteTasksByNoteId(savedId)

                val allTasks = manualTasks.toMutableList()
                if (isAiProcessingEnabled() && fullContent.isNotBlank()) {
                    val textOnly =
                        fullContent.lines().filter { !it.startsWith("•") }.joinToString("\n").trim()
                    if (textOnly.isNotBlank()) {
                        runCatching {
                            GroqService.extractTasksFromNote(finalTitle, textOnly)
                                .forEach { aiTask ->
                                    val dup = allTasks.any {
                                        it.contains(aiTask, true) || aiTask.contains(
                                            it, true
                                        )
                                    }
                                    if (!dup && aiTask.isNotBlank()) allTasks.add(aiTask)
                                }
                        }.onFailure { Log.e("NoteViewModel", "extractTasks: ${it.message}") }
                    }
                }

                if (allTasks.isNotEmpty() && savedId > 0) {
                    repository.insertTasks(allTasks.mapIndexed { i, t ->
                        TaskEntity(
                            title = t,
                            source = finalTitle,
                            noteId = savedId,
                            date = date,
                            isUrgent = i < manualTasks.size,
                        )
                    })
                }

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isSaving = false, shouldNavigateBack = true) }
                    showSnackbar(str(R.string.success_note_saved))
                }
            } catch (e: Exception) {
                Log.e("NoteViewModel", "saveNote: ${e.message}")
                runCatching {
                    val fallback = Note(
                        id = if (noteId > 0) noteId else 0,
                        title = state.title.trim().ifBlank { str(R.string.default_note_title2) },
                        content = state.contentBlocks.filterIsInstance<ContentBlock.TextBlock>()
                            .joinToString("\n") { it.text },
                        category = "General",
                        imageUri = state.contentBlocks.filterIsInstance<ContentBlock.ImageBlock>()
                            .firstOrNull()?.uri?.path,
                        audioPaths = if (isVoiceStorageEnabled()) state.contentBlocks.filterIsInstance<ContentBlock.AudioBlock>()
                            .joinToString(",") { it.filePath } else null,
                        date = date,
                    )
                    if (noteId > 0) repository.updateNote(fallback)
                    else repository.insertNote(fallback)
                }
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isSaving = false, shouldNavigateBack = true) }
                    showSnackbar(str(R.string.error_save_with_issues))
                }
            }
        }
    }

    // ── Snackbar & Navigation ─────────────────────────────────────────────────

    fun showSnackbar(message: String) = _uiState.update { it.copy(snackbarMessage = message) }
    fun snackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }
    fun navigationHandled() = _uiState.update { it.copy(shouldNavigateBack = false) }

    // ── Privacy helpers ───────────────────────────────────────────────────────

    fun isAiProcessingEnabled() = privacyPrefs.getBoolean("privacy_ai_processing", true)
    private fun isVoiceStorageEnabled() = privacyPrefs.getBoolean("privacy_voice_storage", true)

    // ── Statistics ────────────────────────────────────────────────────────────

    private fun recalculateCounts(blocks: List<ContentBlock>) {
        val wordCount = blocks.filterIsInstance<ContentBlock.TextBlock>().sumOf {
            it.text.trim().split("\\s+".toRegex()).filter { w -> w.isNotEmpty() }.size
        }
        val charCount = blocks.sumOf { b ->
            when (b) {
                is ContentBlock.TextBlock -> b.text.length
                is ContentBlock.BulletBlock -> b.text.length
                else -> 0
            }
        }
        _uiState.update {
            it.copy(
                wordCount = wordCount,
                readingMinutes = maxOf(1, wordCount / 200),
                characterCount = charCount,
            )
        }
    }

    // ── Image storage ─────────────────────────────────────────────────────────

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                val file = File(appContext.filesDir, "note_image_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { input.copyTo(it) }
                file.absolutePath
            }
        } catch (e: Exception) {
            Log.e("NoteViewModel", "saveImage: ${e.message}")
            null
        }
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri) = saveImageToInternalStorage(uri)

    // ── Misc ──────────────────────────────────────────────────────────────────

    suspend fun getNoteById(id: Int) = withContext(Dispatchers.IO) {
        runCatching { repository.getNoteById(id) }.getOrNull()
    }

    fun deleteNote(note: Note, onDeleteSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteNote(note)
                withContext(Dispatchers.Main) { onDeleteSuccess() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(str(R.string.error_delete_note, e.message ?: ""))
                }
            }
        }
    }
}