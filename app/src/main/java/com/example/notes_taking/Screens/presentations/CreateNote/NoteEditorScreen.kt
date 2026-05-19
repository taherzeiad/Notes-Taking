package com.example.notes_taking.Screens.presentations.Editor

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.notes_taking.R
import com.example.notes_taking.Screens.presentations.CreateNote.AiRateLimitBadge
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NoteEditorScreen(
    noteId: Int = 0,
    openAudio: Boolean = false,
    openImage: Boolean = false,
    viewModel: NoteViewModel,
    onClose: () -> Unit = {},
    onSave: () -> Unit = {},
) {
    val sdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    val currentDate = remember { sdf.format(Date()) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    // ── UI-only state (dialogs, recording) ───────────────────────────────────
    var aiMenuExpanded by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showRecordingDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }
    val mediaRecorder = remember { mutableStateOf<android.media.MediaRecorder?>(null) }
    var recordedFilePath by remember { mutableStateOf<String?>(null) }

    // ── String resources needed outside Composable scope ─────────────────────
    val permImagesRequired = stringResource(R.string.permission_images_required)
    val permAudioRequired = stringResource(R.string.permission_audio_required)
    val permMicRequired = stringResource(R.string.permission_microphone_required)
    val aiDisabledPrivacy = stringResource(R.string.ai_disabled_privacy)
    val errorRecordStart = stringResource(R.string.error_recording_start)

    // ── Side effects ──────────────────────────────────────────────────────────
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.snackbarShown()
        }
    }
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            viewModel.navigationHandled()
            onSave()
        }
    }
    LaunchedEffect(noteId) { viewModel.loadNote(noteId) }
    LaunchedEffect(openAudio) {
        if (openAudio) {
            delay(350); showAudioDialog = true
        }
    }

    // ── Pickers ───────────────────────────────────────────────────────────────
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.addImageBlock(it) } }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.addAudioBlock(it) } }

    // ── Permission launchers ──────────────────────────────────────────────────
    val readImagesPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) imagePickerLauncher.launch("image/*")
        else scope.launch { snackbarHostState.showSnackbar(permImagesRequired) }
    }
    val readAudioPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) audioPickerLauncher.launch("audio/*")
        else scope.launch { snackbarHostState.showSnackbar(permAudioRequired) }
    }
    val recordPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (openAudio) showRecordingDialog = true else showAudioDialog = true
        } else scope.launch { snackbarHostState.showSnackbar(permMicRequired) }
    }

    // ── Permission helpers ────────────────────────────────────────────────────
    fun launchImagePicker() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, perm) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) imagePickerLauncher.launch("image/*")
        else readImagesPermLauncher.launch(perm)
    }

    fun launchAudioFilePicker() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            android.Manifest.permission.READ_MEDIA_AUDIO
        else android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, perm) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) audioPickerLauncher.launch("audio/*")
        else readAudioPermLauncher.launch(perm)
    }

    fun launchRecordAudio() {
        val perm = android.Manifest.permission.RECORD_AUDIO
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, perm) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) showAudioDialog = true
        else recordPermLauncher.launch(perm)
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .navigationBarsPadding()
                .statusBarsPadding()
                .imePadding(),
        ) {
            // Top Bar
            EditorTopBar(
                isSaving = uiState.isSaving,
                isLoading = uiState.isLoading,
                onClose = onClose,
                onSave = { viewModel.saveNote(noteId, currentDate) },
            )

            HorizontalDivider(
                color = colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 40.dp),
            )

            // Content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Title
                BasicTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = if (uiState.title.isEmpty()) Color(0xFFCEC0B0)
                        else colorScheme.onBackground,
                        textAlign = TextAlign.Start,
                    ),
                    cursorBrush = SolidColor(colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.title.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.editor_title_hint),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MansalvaFontFamily,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date + Reading time
                EditorMetaRow(currentDate = currentDate, readingMinutes = uiState.readingMinutes)

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = colorScheme.primary) }
                }

                // Content blocks
                uiState.contentBlocks.forEachIndexed { index, block ->
                    when (block) {
                        is ContentBlock.TextBlock -> TextBlockItem(
                            block = block,
                            isBold = uiState.isBold,
                            isItalic = uiState.isItalic,
                            isAlone = uiState.contentBlocks.size == 1,
                            onUpdate = { viewModel.updateBlock(index, it) },
                        )

                        is ContentBlock.BulletBlock -> BulletBlockItem(
                            block = block,
                            onUpdate = { viewModel.updateBlock(index, it) },
                            onAdd = { viewModel.addBlockAt(index, ContentBlock.BulletBlock()) },
                            onRemove = { viewModel.removeBlock(index) },
                        )

                        is ContentBlock.ImageBlock -> ImageBlockItem(
                            block = block,
                            onRemove = { viewModel.removeBlock(index) },
                        )

                        is ContentBlock.AudioBlock -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            AudioBlockItem(
                                block = block,
                                onRemove = { viewModel.removeBlock(index) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        is ContentBlock.LinkBlock -> LinkBlockItem(
                            block = block,
                            onRemove = { viewModel.removeBlock(index) },
                        )
                    }
                }
            }
            AiRateLimitBadge(state = uiState.rateLimitState)

            // Bottom Toolbar
            EditorBottomToolbar(
                uiState = uiState,
                aiMenuExpanded = aiMenuExpanded,
                onAiMenuToggle = { aiMenuExpanded = it },
                onAiDisabled = { viewModel.showSnackbar(aiDisabledPrivacy) },
                onRephrase = { aiMenuExpanded = false; viewModel.rephraseText() },
                onDiacritize = { aiMenuExpanded = false; viewModel.diacritizeText() },
                onMicClick = { launchRecordAudio() },
                onLinkClick = { showLinkDialog = true },
                onImageClick = { launchImagePicker() },
                onBulletClick = { viewModel.addBulletBlock() },
                onBoldClick = { viewModel.toggleBold() },
                onItalicClick = { viewModel.toggleItalic() },
                isAiEnabled = viewModel.isAiProcessingEnabled(),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (showAudioDialog) {
        AudioSourceDialog(
            onDismiss = { showAudioDialog = false },
            onChooseFile = { showAudioDialog = false; launchAudioFilePicker() },
            onRecordDirect = { showAudioDialog = false; showRecordingDialog = true },
        )
    }

    if (showRecordingDialog) {
        RecordingDialog(
            onDismiss = {
                showRecordingDialog = false
                mediaRecorder.value?.apply { runCatching { stop(); release() } }
                mediaRecorder.value = null; isRecording = false; recordingSeconds = 0
            },
            onSave = { filePath ->
                showRecordingDialog = false; isRecording = false
                recordingSeconds = 0; mediaRecorder.value = null
                viewModel.addRecordedAudioBlock(filePath)
            },
            isRecording = isRecording,
            recordingSeconds = recordingSeconds,
            onStartRecording = {
                val file = File(context.filesDir, "record_${System.currentTimeMillis()}.mp4")
                recordedFilePath = file.absolutePath
                try {
                    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        android.media.MediaRecorder(context)
                    else @Suppress("DEPRECATION") android.media.MediaRecorder()
                    recorder.apply {
                        setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                        setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                        setOutputFile(file.absolutePath)
                        prepare(); start()
                    }
                    mediaRecorder.value = recorder; isRecording = true
                    scope.launch {
                        while (isRecording) {
                            delay(1000); recordingSeconds++
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModel.showSnackbar(errorRecordStart)
                }
            },
            onStopRecording = {
                mediaRecorder.value?.apply { runCatching { stop(); release() } }
                mediaRecorder.value = null; isRecording = false
            },
            recordedFilePath = recordedFilePath,
        )
    }

    if (showLinkDialog) {
        AddLinkDialog(
            onDismiss = { showLinkDialog = false },
            onConfirm = { url -> viewModel.addLinkBlock(url); showLinkDialog = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditorTopBar(
    isSaving: Boolean,
    isLoading: Boolean,
    onClose: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = colorScheme.onBackground,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = stringResource(R.string.notes_screen_title_bar),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = colorScheme.onBackground,
            )
        }
        Button(
            onClick = onSave,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp),
            enabled = !isLoading && !isSaving,
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.editor_save),
                    fontSize = 14.sp,
                    fontFamily = ManropeFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Meta Row (Date + Reading time)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditorMetaRow(currentDate: String, readingMinutes: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Outlined.CalendarMonth,
                null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = currentDate,
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                color = colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Outlined.Schedule,
                null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.editor_reading_time, readingMinutes),
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Toolbar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditorBottomToolbar(
    uiState: com.example.notes_taking.Screens.presentations.CreateNote.EditorUiState,
    aiMenuExpanded: Boolean,
    isAiEnabled: Boolean,
    onAiMenuToggle: (Boolean) -> Unit,
    onAiDisabled: () -> Unit,
    onRephrase: () -> Unit,
    onDiacritize: () -> Unit,
    onMicClick: () -> Unit,
    onLinkClick: () -> Unit,
    onImageClick: () -> Unit,
    onBulletClick: () -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // AI button
            Box {
                if (uiState.isAiLoading) {
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    IconButton(
                        onClick = { if (!isAiEnabled) onAiDisabled() else onAiMenuToggle(true) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            null,
                            tint = if (isAiEnabled) colorScheme.primary else colorScheme.outline,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                DropdownMenu(
                    expanded = aiMenuExpanded && !uiState.isAiLoading,
                    onDismissRequest = { onAiMenuToggle(false) },
                    modifier = Modifier.background(colorScheme.surface),
                ) {
                    DropdownMenuItem(
                        text = {
                            AiMenuRow(
                                Icons.Outlined.AutoAwesome,
                                stringResource(R.string.rephrase_text)
                            )
                        },
                        onClick = onRephrase,
                    )
                    HorizontalDivider(color = colorScheme.outlineVariant)
                    DropdownMenuItem(
                        text = {
                            AiMenuRow(
                                Icons.Outlined.Spellcheck,
                                stringResource(R.string.diacritize_text)
                            )
                        },
                        onClick = onDiacritize,
                    )
                }
            }

            EditorToolbarButton(Icons.Outlined.Mic, onClick = onMicClick)
            EditorToolbarButton(Icons.Outlined.Link, onClick = onLinkClick)
            EditorToolbarButton(Icons.Outlined.Image, onClick = onImageClick)

            // Character count
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.characterCount.toString(),
                    fontSize = 14.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant,
                )
            }

            EditorToolbarButton(
                Icons.AutoMirrored.Outlined.FormatListBulleted,
                onClick = onBulletClick
            )

            // Italic
            FormatToggleButton(
                label = "I",
                isActive = uiState.isItalic,
                isItalic = true,
                onClick = onItalicClick,
            )

            // Bold
            FormatToggleButton(
                label = "B",
                isActive = uiState.isBold,
                onClick = onBoldClick,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Content Block Items
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TextBlockItem(
    block: ContentBlock.TextBlock,
    isBold: Boolean,
    isItalic: Boolean,
    isAlone: Boolean,
    onUpdate: (ContentBlock.TextBlock) -> Unit,
) {
    BasicTextField(
        value = block.text,
        onValueChange = { onUpdate(block.copy(text = it)) },
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontFamily = ManropeFontFamily,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = colorScheme.onBackground,
            lineHeight = 26.sp,
            textAlign = TextAlign.Start,
        ),
        cursorBrush = SolidColor(colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (isAlone && block.text.isEmpty()) 300.dp else 48.dp),
        decorationBox = { innerTextField ->
            Box {
                if (block.text.isEmpty() && isAlone) {
                    Text(
                        text = stringResource(R.string.editor_content_hint),
                        fontSize = 15.sp,
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Start,
                        lineHeight = 26.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun BulletBlockItem(
    block: ContentBlock.BulletBlock,
    onUpdate: (ContentBlock.BulletBlock) -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            "•",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        BasicTextField(
            value = block.text,
            onValueChange = { newText ->
                if (newText.endsWith("\n")) onAdd()
                else onUpdate(block.copy(text = newText))
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontFamily = ManropeFontFamily,
                color = colorScheme.onBackground,
                lineHeight = 24.sp,
            ),
            cursorBrush = SolidColor(colorScheme.primary),
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(
                Icons.Default.Close,
                null,
                tint = colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ImageBlockItem(block: ContentBlock.ImageBlock, onRemove: () -> Unit) {
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AsyncImage(
            model = block.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(28.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape),
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun LinkBlockItem(block: ContentBlock.LinkBlock, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.secondaryContainer),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Link, null, tint = colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = block.url,
                color = colorScheme.onSecondaryContainer,
                fontSize = 14.sp,
                fontFamily = ManropeFontFamily,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint = colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Audio Block
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AudioBlockItem(block: ContentBlock.AudioBlock, onRemove: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release(); mediaPlayer = null }
    }
    LaunchedEffect(isPlaying) {
        if (isPlaying && mediaPlayer != null) {
            while (isPlaying && mediaPlayer?.isPlaying == true) {
                delay(100); currentPosition = mediaPlayer?.currentPosition ?: 0
            }
        }
    }

    fun togglePlayback() {
        if (mediaPlayer == null) {
            try {
                mediaPlayer = android.media.MediaPlayer().apply {
                    setDataSource(block.filePath); prepare()
                    setOnCompletionListener {
                        isPlaying = false; currentPosition =
                        0; mediaPlayer?.release(); mediaPlayer = null
                    }
                    setOnPreparedListener { duration = it.duration }
                }
            } catch (e: Exception) {
                Log.e("AudioBlock", "${e.message}"); return
            }
        }
        if (isPlaying) {
            mediaPlayer?.pause(); isPlaying = false
        } else {
            mediaPlayer?.apply {
                if (currentPosition > 0) seekTo(currentPosition); start(); isPlaying = true
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isPlaying) colorScheme.tertiaryContainer else colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (isPlaying) Icons.Outlined.Stop else Icons.Outlined.Mic,
                        null,
                        tint = if (isPlaying) colorScheme.tertiary else colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        block.name,
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        stringResource(R.string.audio_file),
                        fontSize = 11.sp,
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { togglePlayback() }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (isPlaying) Icons.Outlined.Stop else Icons.Default.PlayArrow,
                        contentDescription = stringResource(if (isPlaying) R.string.stop_playing else R.string.play_recording),
                        tint = colorScheme.primary, modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Close,
                        stringResource(R.string.action_delete),
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (isPlaying || currentPosition > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        String.format(
                            "%02d:%02d",
                            currentPosition / 1000 / 60,
                            currentPosition / 1000 % 60
                        ),
                        fontSize = 10.sp,
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = {
                            if (!isPlaying) {
                                currentPosition = it.toInt(); mediaPlayer?.seekTo(currentPosition)
                            }
                        },
                        onValueChangeFinished = {
                            if (!isPlaying && mediaPlayer != null) mediaPlayer?.seekTo(
                                currentPosition
                            )
                        },
                        valueRange = 0f..duration.toFloat(),
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.primaryContainer
                        ),
                    )
                    Text(
                        String.format("%02d:%02d", duration / 1000 / 60, duration / 1000 % 60),
                        fontSize = 10.sp,
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AudioSourceDialog(onDismiss: () -> Unit, onChooseFile: () -> Unit, onRecordDirect: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Mic,
                        null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    stringResource(R.string.add_audio),
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.choose_audio_method),
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    onClick = onRecordDirect,
                    shape = RoundedCornerShape(16.dp),
                    color = colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Mic,
                            null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                stringResource(R.string.record_audio),
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                stringResource(R.string.record_now),
                                fontFamily = ManropeFontFamily,
                                color = colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Surface(
                    onClick = onChooseFile,
                    shape = RoundedCornerShape(16.dp),
                    color = colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.FolderOpen,
                            null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                stringResource(R.string.pick_audio),
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                            Text(
                                stringResource(R.string.from_library),
                                fontFamily = ManropeFontFamily,
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.cancel),
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    isRecording: Boolean,
    recordingSeconds: Int,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    recordedFilePath: String?,
) {
    val minutes = recordingSeconds / 60
    val seconds = recordingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }

    DisposableEffect(Unit) { onDispose { mediaPlayer?.release(); mediaPlayer = null } }
    LaunchedEffect(isPlaying) {
        if (isPlaying && mediaPlayer != null) {
            while (isPlaying && mediaPlayer?.isPlaying == true) {
                delay(100); currentPosition = mediaPlayer?.currentPosition ?: 0
            }
        }
    }

    fun stopPlayback() {
        mediaPlayer?.apply { if (isPlaying) stop(); release() }; mediaPlayer = null; isPlaying =
            false; currentPosition = 0
    }

    fun togglePlayback() {
        if (recordedFilePath == null) return
        if (mediaPlayer == null) {
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(recordedFilePath); prepare()
                setOnCompletionListener {
                    isPlaying = false; currentPosition = 0; mediaPlayer?.release(); mediaPlayer =
                    null
                }
                setOnPreparedListener { duration = it.duration }
            }
        }
        if (isPlaying) {
            mediaPlayer?.pause(); isPlaying = false
        } else {
            mediaPlayer?.apply {
                if (currentPosition > 0) seekTo(currentPosition); start(); isPlaying = true
            }
        }
    }

    Dialog(onDismissRequest = { stopPlayback(); onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(R.string.audio_recording),
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            when {
                                isRecording -> colorScheme.errorContainer; isPlaying -> colorScheme.tertiaryContainer; else -> colorScheme.primaryContainer
                            }, CircleShape
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        when {
                            isRecording -> Icons.Outlined.Mic; isPlaying -> Icons.Outlined.Stop; else -> Icons.Outlined.Mic
                        },
                        null,
                        tint = when {
                            isRecording -> colorScheme.error; isPlaying -> colorScheme.tertiary; else -> colorScheme.primary
                        },
                        modifier = Modifier.size(40.dp),
                    )
                }
                Text(
                    text = if (isPlaying) "%02d:%02d / %02d:%02d".format(
                        currentPosition / 1000 / 60,
                        currentPosition / 1000 % 60,
                        minutes,
                        seconds
                    ) else timeText,
                    fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp,
                    color = when {
                        isRecording -> colorScheme.error; isPlaying -> colorScheme.tertiary; else -> colorScheme.onSurface
                    },
                )
                if (recordedFilePath != null && !isRecording && recordingSeconds > 0) {
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = {
                            if (!isPlaying) {
                                currentPosition = it.toInt(); mediaPlayer?.seekTo(currentPosition)
                            }
                        },
                        onValueChangeFinished = {
                            if (!isPlaying && mediaPlayer != null) mediaPlayer?.seekTo(
                                currentPosition
                            )
                        },
                        valueRange = 0f..duration.toFloat(), modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.primaryContainer
                        ),
                    )
                }
                Text(
                    text = when {
                        isRecording -> stringResource(R.string.recording_in_progress)
                        isPlaying -> stringResource(R.string.playing_audio)
                        recordingSeconds > 0 -> stringResource(R.string.recording_stopped)
                        else -> stringResource(R.string.press_to_start)
                    },
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isRecording) {
                                onStopRecording(); stopPlayback()
                            } else {
                                onStartRecording(); stopPlayback()
                            }
                        },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) colorScheme.error else colorScheme.primary),
                    ) {
                        Icon(
                            if (isRecording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            stringResource(if (isRecording) R.string.stop_recording else R.string.start_recording),
                            fontFamily = ManropeFontFamily
                        )
                    }
                    if (recordedFilePath != null && !isRecording && recordingSeconds > 0) {
                        Button(
                            onClick = { togglePlayback() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) colorScheme.tertiary else colorScheme.secondary),
                        ) {
                            Icon(
                                if (isPlaying) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stringResource(if (isPlaying) R.string.stop_playing else R.string.play_recording),
                                fontFamily = ManropeFontFamily
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (recordedFilePath != null && !isRecording && recordingSeconds > 0) {
                        Button(
                            onClick = { recordedFilePath.let { onSave(it) } },
                            enabled = !isRecording && recordingSeconds > 0,
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        ) {
                            Icon(Icons.Outlined.Save, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.save_recording),
                                fontFamily = ManropeFontFamily
                            )
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            elevation = null
                        ) {
                            Text(
                                stringResource(R.string.cancel),
                                color = colorScheme.onSurfaceVariant,
                                fontFamily = ManropeFontFamily,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddLinkDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.add_link),
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    textStyle = TextStyle(
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.onSurface,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(colorScheme.primary),
                    decorationBox = { inner ->
                        if (text.isEmpty()) Text(
                            "https://example.com",
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontFamily = ManropeFontFamily,
                            fontSize = 14.sp
                        )
                        inner()
                    },
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = null
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = colorScheme.onSurfaceVariant,
                            fontFamily = ManropeFontFamily,
                            fontSize = 14.sp
                        )
                    }
                    Button(
                        onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = text.isNotBlank(),
                    ) {
                        Text(
                            stringResource(R.string.add),
                            fontFamily = ManropeFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small reusable Composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AiMenuRow(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(label, fontFamily = ManropeFontFamily, fontSize = 14.sp, color = colorScheme.onSurface)
    }
}

@Composable
private fun FormatToggleButton(
    label: String,
    isActive: Boolean,
    isItalic: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Text(
                label,
                fontSize = 16.sp,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                color = if (isActive) colorScheme.primary else colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EditorToolbarButton(
    icon: ImageVector,
    tint: Color = colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
    }
}