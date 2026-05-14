package com.example.notes_taking.Screens.presentations.Editor

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.example.notes_taking.API.GroqService
import com.example.notes_taking.R
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ======= Content Block Types =======
sealed class ContentBlock {
    data class TextBlock(
        val id: String = UUID.randomUUID().toString(), var text: String = ""
    ) : ContentBlock()

    data class ImageBlock(
        val id: String = UUID.randomUUID().toString(), val uri: Uri
    ) : ContentBlock()

    data class AudioBlock(
        val id: String = UUID.randomUUID().toString(),
        val uri: Uri,
        val name: String,
        val filePath: String = uri.path ?: ""
    ) : ContentBlock()

    data class BulletBlock(
        val id: String = UUID.randomUUID().toString(), var text: String = ""
    ) : ContentBlock()

    data class LinkBlock(
        val id: String = UUID.randomUUID().toString(),
        var url: String = "",
        var description: String = ""
    ) : ContentBlock()
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun NoteEditorScreen(
    noteId: Int = 0,
    openAudio: Boolean = false,
    openImage: Boolean = false,
    viewModel: NoteViewModel,
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val sdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    val currentDate = remember { sdf.format(Date()) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var aiMenuExpanded by remember { mutableStateOf(false) }
    var isAiLoading by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var isSavingInternally by remember { mutableStateOf(false) }

    // ← متغيرات الصوت
    var showAudioDialog by remember { mutableStateOf(false) }
    var showRecordingDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }
    val mediaRecorder = remember { mutableStateOf<android.media.MediaRecorder?>(null) }
    var recordedFilePath by remember { mutableStateOf<String?>(null) }

    val contentBlocks = remember { mutableStateListOf<ContentBlock>(ContentBlock.TextBlock()) }
    val scope = rememberCoroutineScope()

    val wordCount = remember(contentBlocks) {
        derivedStateOf {
            contentBlocks.filterIsInstance<ContentBlock.TextBlock>().sumOf {
                it.text.trim().split("\\s+".toRegex()).filter { w -> w.isNotEmpty() }.size
            }
        }
    }
    val readingMinutes = derivedStateOf { maxOf(1, wordCount.value / 200) }

    val characterCount = remember {
        derivedStateOf {
            contentBlocks.sumOf { block ->
                when (block) {
                    is ContentBlock.TextBlock -> block.text.length
                    is ContentBlock.BulletBlock -> block.text.length
                    else -> 0
                }
            }
        }
    }

    // ======= Image Picker =======
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                val permanentPath = viewModel.saveImageToInternalStorage(context, it)
                withContext(Dispatchers.Main) {
                    permanentPath?.let { path ->
                        contentBlocks.add(ContentBlock.ImageBlock(uri = Uri.fromFile(File(path))))
                        contentBlocks.add(ContentBlock.TextBlock())
                    }
                }
            }
        }
    }

    // ======= Audio Picker =======
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val name = uri.lastPathSegment?.substringAfterLast("/")
                    ?: "تسجيل_${System.currentTimeMillis()}"
                contentBlocks.add(ContentBlock.AudioBlock(uri = it, name = name))
                contentBlocks.add(ContentBlock.TextBlock())
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("حدث خطأ أثناء إضافة الملف الصوتي") }
                e.printStackTrace()
            }
        }
    }

    // ======= Permission Launchers =======
    // إذن قراءة الصور (للأجهزة Android 13+)
    val readImagesPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    if (Locale.getDefault().language == "ar")
                        "يجب منح صلاحية الوصول للصور"
                    else
                        "Images access permission required"
                )
            }
        }
    }

    // إذن قراءة الملفات الصوتية (للأجهزة Android 13+)
    val readAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            audioPickerLauncher.launch("audio/*")
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    if (Locale.getDefault().language == "ar")
                        "يجب منح صلاحية الوصول للملفات الصوتية"
                    else
                        "Audio access permission required"
                )
            }
        }
    }

    // إذن التسجيل الصوتي
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (openAudio) {
                showRecordingDialog = true
            } else {
                showAudioDialog = true
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    if (Locale.getDefault().language == "ar")
                        "يجب منح صلاحية الميكروفون"
                    else
                        "Microphone permission required"
                )
            }
        }
    }

    // ======= Helper Functions =======
    // دالة للتحقق من الأذونات وطلبها
    fun checkAndRequestImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - نحتاج إذن READ_MEDIA_IMAGES
            val permission = android.Manifest.permission.READ_MEDIA_IMAGES
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (granted) {
                imagePickerLauncher.launch("image/*")
            } else {
                readImagesPermissionLauncher.launch(permission)
            }
        } else {
            // الإصدارات الأقدم - نحتاج إذن READ_EXTERNAL_STORAGE
            val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (granted) {
                imagePickerLauncher.launch("image/*")
            } else {
                readImagesPermissionLauncher.launch(permission)
            }
        }
    }

    fun checkAndRequestAudioFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - نحتاج إذن READ_MEDIA_AUDIO
            val permission = android.Manifest.permission.READ_MEDIA_AUDIO
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (granted) {
                audioPickerLauncher.launch("audio/*")
            } else {
                readAudioPermissionLauncher.launch(permission)
            }
        } else {
            // الإصدارات الأقدم - نحتاج إذن READ_EXTERNAL_STORAGE
            val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (granted) {
                audioPickerLauncher.launch("audio/*")
            } else {
                readAudioPermissionLauncher.launch(permission)
            }
        }
    }

    fun checkAndRequestRecordAudioPermission() {
        val permission = android.Manifest.permission.RECORD_AUDIO
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (granted) {
            showAudioDialog = true
        } else {
            recordAudioPermissionLauncher.launch(permission)
        }
    }

    // ======= Load Note =======
    LaunchedEffect(noteId) {
        if (noteId > 0) {
            isLoading = true
            try {
                val note = viewModel.getNoteById(noteId)
                note?.let {
                    title = it.title
                    contentBlocks.clear()
                    if (it.content.isNotBlank()) {
                        contentBlocks.add(ContentBlock.TextBlock(text = it.content))
                    } else {
                        contentBlocks.add(ContentBlock.TextBlock())
                    }
                    it.imageUri?.let { path ->
                        val imageFile = File(path)
                        if (imageFile.exists()) {
                            contentBlocks.add(ContentBlock.ImageBlock(uri = Uri.fromFile(imageFile)))
                        }
                    }
                    it.audioPaths?.split(",")?.forEach { audioPath ->
                        if (audioPath.isNotBlank()) {
                            val audioFile = File(audioPath)
                            if (audioFile.exists()) {
                                contentBlocks.add(
                                    ContentBlock.AudioBlock(
                                        uri = Uri.fromFile(audioFile),
                                        name = audioFile.name,
                                        filePath = audioPath
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("فشل في تحميل الملاحظة") }
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(key1 = openAudio) {
        if (openAudio) {
            delay(350)
            checkAndRequestRecordAudioPermission()
        }
    }

    LaunchedEffect(key1 = openImage) {
        if (openImage) {
            delay(400)
            checkAndRequestImagePermission()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .navigationBarsPadding()
            .statusBarsPadding()
            .imePadding()
    ) {

        // ======= Top Bar =======
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "إغلاق",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.notes_screen_title_bar),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ManropeFontFamily,
                    color = colorScheme.onBackground
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val hasContent = title.isNotBlank() || contentBlocks.any {
                            (it is ContentBlock.TextBlock && it.text.isNotBlank()) || it is ContentBlock.ImageBlock
                        }
                        if (!hasContent) {
                            scope.launch { snackbarHostState.showSnackbar("لا يوجد محتوى لحفظه") }
                            return@Button
                        }
                        isSavingInternally = true

                        val firstImageBlock =
                            contentBlocks.filterIsInstance<ContentBlock.ImageBlock>().firstOrNull()
                        val imagePathToSave = firstImageBlock?.uri?.path

                        val fullContent = contentBlocks.joinToString("\n") { block ->
                            when (block) {
                                is ContentBlock.TextBlock -> block.text
                                is ContentBlock.BulletBlock -> "• ${block.text}"
                                else -> ""
                            }
                        }

                        // ← استخراج BulletBlocks كمهام يدوية
                        val manualTasks = contentBlocks.filterIsInstance<ContentBlock.BulletBlock>()
                            .map { it.text.trim() }.filter { it.isNotBlank() }
                        val audioPaths = contentBlocks
                            .filterIsInstance<ContentBlock.AudioBlock>()
                            .map { it.filePath }
                            .joinToString(",")

                        viewModel.saveNoteWithAI(
                            id = noteId,
                            title = title,
                            content = fullContent,
                            audioPaths = audioPaths,
                            imageUri = imagePathToSave,
                            date = currentDate,
                            manualTasks = manualTasks,
                            onComplete = {
                                isSavingInternally = false
                                onSave()
                                scope.launch { snackbarHostState.showSnackbar("تم حفظ الملاحظة بنجاح") }
                            },
                            onError = { error ->
                                isSavingInternally = false
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            })
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary, contentColor = colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.editor_save),
                            fontSize = 14.sp,
                            fontFamily = ManropeFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            color = colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 40.dp)
        )

        // ======= Content Area =======
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ======= Title =======
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ManropeFontFamily,
                    color = if (title.isEmpty()) Color(0xFFCEC0B0) else colorScheme.onBackground,
                    textAlign = TextAlign.Start
                ),
                cursorBrush = SolidColor(colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                text = stringResource(R.string.editor_title_hint),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MansalvaFontFamily,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    }
                })

            Spacer(modifier = Modifier.height(12.dp))

            // ======= Date + Reading Time =======
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
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
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.editor_reading_time, readingMinutes.value),
                        fontSize = 12.sp,
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }

            // ======= Content Blocks =======
            contentBlocks.forEachIndexed { index, block ->
                when (block) {

                    is ContentBlock.TextBlock -> {
                        BasicTextField(
                            value = block.text,
                            onValueChange = { contentBlocks[index] = block.copy(text = it) },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = ManropeFontFamily,
                                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                color = colorScheme.onBackground,
                                lineHeight = 26.sp,
                                textAlign = TextAlign.Start
                            ),
                            cursorBrush = SolidColor(colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(
                                    minHeight = if (contentBlocks.size == 1 && block.text.isEmpty()) 300.dp else 48.dp
                                ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (block.text.isEmpty() && contentBlocks.size == 1) {
                                        Text(
                                            text = stringResource(R.string.editor_content_hint),
                                            fontSize = 15.sp,
                                            fontFamily = ManropeFontFamily,
                                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Start,
                                            lineHeight = 26.sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    innerTextField()
                                }
                            })
                    }

                    is ContentBlock.BulletBlock -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            BasicTextField(
                                value = block.text,
                                onValueChange = { newText ->
                                    if (newText.endsWith("\n")) {
                                        contentBlocks.add(index + 1, ContentBlock.BulletBlock())
                                    } else {
                                        contentBlocks[index] = block.copy(text = newText)
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = ManropeFontFamily,
                                    color = colorScheme.onBackground,
                                    lineHeight = 24.sp
                                ),
                                cursorBrush = SolidColor(colorScheme.primary),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { contentBlocks.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = colorScheme.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    is ContentBlock.ImageBlock -> {
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
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            IconButton(
                                onClick = { contentBlocks.removeAt(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    is ContentBlock.AudioBlock -> {
                        Spacer(modifier = Modifier.height(8.dp))

                        var isPlaying by remember { mutableStateOf(false) }
                        var mediaPlayer by remember {
                            mutableStateOf<android.media.MediaPlayer?>(
                                null
                            )
                        }
                        var currentPosition by remember { mutableStateOf(0) }
                        var duration by remember { mutableStateOf(0) }
                        val scope = rememberCoroutineScope()

                        // تنظيف MediaPlayer عند إزالة البلوك
                        DisposableEffect(Unit) {
                            onDispose {
                                mediaPlayer?.release()
                                mediaPlayer = null
                            }
                        }

                        // تحديث position أثناء التشغيل
                        LaunchedEffect(isPlaying) {
                            if (isPlaying && mediaPlayer != null) {
                                while (isPlaying && mediaPlayer?.isPlaying == true) {
                                    delay(100)
                                    currentPosition = mediaPlayer?.currentPosition ?: 0
                                }
                            }
                        }

                        // دالة تشغيل/إيقاف
                        fun togglePlayback() {
                            if (mediaPlayer == null) {
                                try {
                                    val player = android.media.MediaPlayer().apply {
                                        setDataSource(block.filePath)
                                        prepare()
                                        setOnCompletionListener {
                                            isPlaying = false
                                            currentPosition = 0
                                            mediaPlayer?.release()
                                            mediaPlayer = null
                                        }
                                        setOnPreparedListener {
                                            duration = it.duration
                                        }
                                    }
                                    mediaPlayer = player
                                } catch (e: Exception) {
                                    Log.e(
                                        "AudioBlock",
                                        "Error initializing MediaPlayer: ${e.message}"
                                    )
                                    return
                                }
                            }

                            if (isPlaying) {
                                mediaPlayer?.pause()
                                isPlaying = false
                            } else {
                                mediaPlayer?.apply {
                                    if (currentPosition > 0) {
                                        seekTo(currentPosition)
                                    }
                                    start()
                                    isPlaying = true
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // الصف العلوي: معلومات الملف
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                if (isPlaying) colorScheme.tertiaryContainer
                                                else colorScheme.primaryContainer,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Outlined.Stop else Icons.Outlined.Mic,
                                            contentDescription = null,
                                            tint = if (isPlaying) colorScheme.tertiary else colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = block.name,
                                            fontSize = 13.sp,
                                            fontFamily = ManropeFontFamily,
                                            color = colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = stringResource(R.string.audio_file),
                                            fontSize = 11.sp,
                                            fontFamily = ManropeFontFamily,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // زر تشغيل
                                    IconButton(
                                        onClick = { togglePlayback() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Outlined.Stop else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    // زر حذف
                                    IconButton(
                                        onClick = { contentBlocks.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "حذف",
                                            tint = colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // شريط التقدم (يظهر فقط أثناء التشغيل)
                                if (isPlaying || currentPosition > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = String.format(
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
                                            onValueChange = { newPosition ->
                                                if (!isPlaying) {
                                                    currentPosition = newPosition.toInt()
                                                    mediaPlayer?.seekTo(currentPosition)
                                                }
                                            },
                                            onValueChangeFinished = {
                                                if (!isPlaying && mediaPlayer != null) {
                                                    mediaPlayer?.seekTo(currentPosition)
                                                }
                                            },
                                            valueRange = 0f..duration.toFloat(),
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = colorScheme.primary,
                                                activeTrackColor = colorScheme.primary,
                                                inactiveTrackColor = colorScheme.primaryContainer
                                            )
                                        )

                                        Text(
                                            text = String.format(
                                                "%02d:%02d",
                                                duration / 1000 / 60,
                                                duration / 1000 % 60
                                            ),
                                            fontSize = 10.sp,
                                            fontFamily = ManropeFontFamily,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    is ContentBlock.LinkBlock -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.secondaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Link,
                                    contentDescription = null,
                                    tint = colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = block.url,
                                    color = colorScheme.onSecondaryContainer,
                                    fontSize = 14.sp,
                                    fontFamily = ManropeFontFamily,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { contentBlocks.removeAt(index) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ======= Bottom Toolbar =======
        Surface(
            modifier = Modifier.fillMaxWidth(), color = colorScheme.surface, shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ======= AI Button =======
                Box {
                    IconButton(
                        onClick = { aiMenuExpanded = true },
                        modifier = Modifier.size(36.dp),
                        enabled = !isAiLoading
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                color = colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = aiMenuExpanded,
                        onDismissRequest = { aiMenuExpanded = false },
                        modifier = Modifier.background(colorScheme.surface)
                    ) {
                        DropdownMenuItem(text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.rephrase_text),
                                    fontFamily = ManropeFontFamily,
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurface
                                )
                            }
                        }, onClick = {
                            aiMenuExpanded = false
                            val currentText =
                                contentBlocks.filterIsInstance<ContentBlock.TextBlock>()
                                    .joinToString("\n") { it.text }.trim()
                            if (currentText.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("لا يوجد نص لإعادة صياغته") }
                                return@DropdownMenuItem
                            }
                            scope.launch {
                                isAiLoading = true
                                try {
                                    val result = GroqService.rephraseText(currentText)
                                    val firstTextIndex =
                                        contentBlocks.indexOfFirst { it is ContentBlock.TextBlock }
                                    if (firstTextIndex != -1) {
                                        contentBlocks[firstTextIndex] =
                                            ContentBlock.TextBlock(text = result)
                                        snackbarHostState.showSnackbar("تمت إعادة الصياغة بنجاح")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("فشل في إعادة الصياغة")
                                } finally {
                                    isAiLoading = false
                                }
                            }
                        })

                        HorizontalDivider(color = colorScheme.outlineVariant)

                        DropdownMenuItem(text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Spellcheck,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.diacritize_text),
                                    fontFamily = ManropeFontFamily,
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurface
                                )
                            }
                        }, onClick = {
                            aiMenuExpanded = false
                            val currentText =
                                contentBlocks.filterIsInstance<ContentBlock.TextBlock>()
                                    .joinToString("\n") { it.text }.trim()
                            if (currentText.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("لا يوجد نص لتشكيله") }
                                return@DropdownMenuItem
                            }
                            scope.launch {
                                isAiLoading = true
                                try {
                                    val result = GroqService.diacritizeText(currentText)
                                    val firstTextIndex =
                                        contentBlocks.indexOfFirst { it is ContentBlock.TextBlock }
                                    if (firstTextIndex != -1) {
                                        contentBlocks[firstTextIndex] =
                                            ContentBlock.TextBlock(text = result)
                                        snackbarHostState.showSnackbar("تم تشكيل النص بنجاح")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("فشل في تشكيل النص")
                                } finally {
                                    isAiLoading = false
                                }
                            }
                        })
                    }
                }

                // ← Mic - يطلب الإذن أولاً
                EditorToolbarButton(
                    icon = Icons.Outlined.Mic,
                    onClick = { checkAndRequestRecordAudioPermission() }
                )

                EditorToolbarButton(icon = Icons.Outlined.Link, onClick = { showLinkDialog = true })

                // ← Image - يطلب الإذن أولاً
                EditorToolbarButton(
                    icon = Icons.Outlined.Image,
                    onClick = { checkAndRequestImagePermission() }
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = characterCount.value.toString(),
                        fontSize = 14.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                EditorToolbarButton(
                    icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                    onClick = { contentBlocks.add(ContentBlock.BulletBlock()) })

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isItalic) colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { isItalic = !isItalic }, modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "I",
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = if (isItalic) colorScheme.primary else colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBold) colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { isBold = !isBold }, modifier = Modifier.size(36.dp)) {
                        Text(
                            text = "B",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isBold) colorScheme.primary else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // ======= Audio Source Dialog =======
    if (showAudioDialog) {
        AudioSourceDialog(
            onDismiss = { showAudioDialog = false },
            onChooseFile = {
                showAudioDialog = false
                checkAndRequestAudioFilePermission()
            },
            onRecordDirect = {
                showAudioDialog = false
                showRecordingDialog = true
            }
        )
    }

    // ======= Recording Dialog =======
    if (showRecordingDialog) {
        RecordingDialog(
            onDismiss = {
                showRecordingDialog = false
                mediaRecorder.value?.apply {
                    try {
                        stop(); release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                mediaRecorder.value = null
                isRecording = false
                recordingSeconds = 0
            },
            onSave = { filePath ->
                showRecordingDialog = false
                isRecording = false
                recordingSeconds = 0
                mediaRecorder.value = null
                val file = File(filePath)
                if (file.exists()) {
                    contentBlocks.add(
                        ContentBlock.AudioBlock(
                            uri = Uri.fromFile(file),
                            name = file.name,
                            filePath = file.absolutePath
                        )
                    )
                    contentBlocks.add(ContentBlock.TextBlock())
                }
            }, isRecording = isRecording, recordingSeconds = recordingSeconds, onStartRecording = {
                val fileName = "record_${System.currentTimeMillis()}.mp4"
                val file = File(context.filesDir, fileName)
                recordedFilePath = file.absolutePath
                try {
                    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        android.media.MediaRecorder(context)
                    } else {
                        @Suppress("DEPRECATION") android.media.MediaRecorder()
                    }
                    recorder.apply {
                        setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                        setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                        setOutputFile(file.absolutePath)
                        prepare()
                        start()
                    }
                    mediaRecorder.value = recorder
                    isRecording = true
                    scope.launch {
                        while (isRecording) {
                            delay(1000)
                            recordingSeconds++
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    scope.launch { snackbarHostState.showSnackbar("فشل في بدء التسجيل") }
                }
            }, onStopRecording = {
                mediaRecorder.value?.apply {
                    try {
                        stop(); release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                mediaRecorder.value = null
                isRecording = false
            }, recordedFilePath = recordedFilePath
        )
    }

    // ======= Link Dialog =======
    if (showLinkDialog) {
        AddLinkDialog(onDismiss = { showLinkDialog = false }, onConfirm = { url ->
            if (url.isNotBlank()) {
                contentBlocks.add(ContentBlock.LinkBlock(url = url))
                contentBlocks.add(ContentBlock.TextBlock())
            }
            showLinkDialog = false
        })
    }
}

// ======= Audio Source Dialog =======
@Composable
fun AudioSourceDialog(
    onDismiss: () -> Unit, onChooseFile: () -> Unit, onRecordDirect: () -> Unit
) {
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
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.add_audio),
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.choose_audio_method),
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ← تسجيل مباشر
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
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.record_audio),

                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = stringResource(R.string.record_now),
                                fontFamily = ManropeFontFamily,
                                color = colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // ← اختيار ملف
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
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.pick_audio),
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                            Text(
                                text = stringResource(R.string.from_library),
                                fontFamily = ManropeFontFamily,
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontFamily = ManropeFontFamily,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ======= Recording Dialog =======
@Composable
fun RecordingDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    isRecording: Boolean,
    recordingSeconds: Int,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    recordedFilePath: String?
) {
    val minutes = recordingSeconds / 60
    val seconds = recordingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    // متغيرات التشغيل
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // تنظيف MediaPlayer عند الخروج
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // تحديث position أثناء التشغيل
    LaunchedEffect(isPlaying) {
        if (isPlaying && mediaPlayer != null) {
            while (isPlaying && mediaPlayer?.isPlaying == true) {
                delay(100)
                currentPosition = mediaPlayer?.currentPosition ?: 0
            }
        }
    }

    // دالة تشغيل/إيقاف التسجيل
    fun togglePlayback() {
        if (recordedFilePath == null) return

        if (mediaPlayer == null) {
            // إنشاء MediaPlayer جديد
            val player = android.media.MediaPlayer().apply {
                setDataSource(recordedFilePath)
                prepare()
                setOnCompletionListener {
                    isPlaying = false
                    currentPosition = 0
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
                setOnPreparedListener {
                    duration = it.duration
                }
            }
            mediaPlayer = player
        }

        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        } else {
            mediaPlayer?.apply {
                if (currentPosition > 0) {
                    seekTo(currentPosition)
                }
                start()
                isPlaying = true
            }
        }
    }

    // دالة إيقاف التشغيل وإعادة الضبط
    fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
        currentPosition = 0
    }

    Dialog(onDismissRequest = {
        stopPlayback()
        onDismiss()
    }) {
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
                    text = stringResource(R.string.audio_recording),
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (isRecording) colorScheme.errorContainer
                            else if (isPlaying) colorScheme.tertiaryContainer
                            else colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isRecording -> Icons.Outlined.Mic
                            isPlaying -> Icons.Outlined.Stop
                            else -> Icons.Outlined.Mic
                        },
                        contentDescription = null,
                        tint = when {
                            isRecording -> colorScheme.error
                            isPlaying -> colorScheme.tertiary
                            else -> colorScheme.primary
                        },
                        modifier = Modifier.size(40.dp)
                    )
                }

                // وقت التسجيل أو التشغيل
                Text(
                    text = if (isPlaying) {
                        val posMinutes = currentPosition / 1000 / 60
                        val posSeconds = currentPosition / 1000 % 60
                        "%02d:%02d / %02d:%02d".format(posMinutes, posSeconds, minutes, seconds)
                    } else {
                        timeText
                    },
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = when {
                        isRecording -> colorScheme.error
                        isPlaying -> colorScheme.tertiary
                        else -> colorScheme.onSurface
                    }
                )

                // شريط التقدم (إذا كان هناك تسجيل محفوظ وغير مسجل حالياً)
                if (recordedFilePath != null && !isRecording && recordingSeconds > 0) {
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = { newPosition ->
                            if (!isPlaying) {
                                currentPosition = newPosition.toInt()
                                mediaPlayer?.seekTo(currentPosition)
                            }
                        },
                        onValueChangeFinished = {
                            if (!isPlaying && mediaPlayer != null) {
                                mediaPlayer?.seekTo(currentPosition)
                            }
                        },
                        valueRange = 0f..duration.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.primaryContainer
                        )
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
                    color = colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // زر التسجيل/الإيقاف
                    Button(
                        onClick = {
                            if (isRecording) {
                                onStopRecording()
                                stopPlayback()
                            } else {
                                onStartRecording()
                                stopPlayback()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) colorScheme.error else colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRecording) stringResource(R.string.stop_recording)
                            else stringResource(R.string.start_recording),
                            fontFamily = ManropeFontFamily
                        )
                    }

                    // زر التشغيل (يظهر فقط عند وجود تسجيل وغير مسجل حالياً)
                    if (recordedFilePath != null && !isRecording && recordingSeconds > 0) {
                        Button(
                            onClick = { togglePlayback() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) colorScheme.tertiary else colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) stringResource(R.string.stop_playing)
                                else stringResource(R.string.play_recording),
                                fontFamily = ManropeFontFamily
                            )
                        }
                    }
                }

                // الصف الثاني من الأزرار (حفظ + إلغاء أو تشغيل + حفظ)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (recordedFilePath != null && !isRecording && recordingSeconds > 0) {
                        Button(
                            onClick = { recordedFilePath?.let { onSave(it) } },
                            enabled = !isRecording && recordingSeconds > 0,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.save_recording),
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
                                text = stringResource(R.string.cancel),
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

// ======= Toolbar Button =======
@Composable
fun EditorToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ======= Add Link Dialog =======
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
                    text = stringResource(R.string.add_link),
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
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                "https://example.com",
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontFamily = ManropeFontFamily,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    })

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
                            text = stringResource(R.string.cancel),
                            color = colorScheme.onSurfaceVariant,
                            fontFamily = ManropeFontFamily,
                            fontSize = 14.sp
                        )
                    }
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                var url = text.trim()
                                if (!url.startsWith("http://") && !url.startsWith("https://")) url =
                                    "https://$url"
                                onConfirm(url)
                            }
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ), shape = RoundedCornerShape(12.dp), enabled = text.isNotBlank()
                    ) {
                        Text(
                            text = stringResource(R.string.add),
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