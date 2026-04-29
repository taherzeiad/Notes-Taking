package com.example.notes_taking.Screens.presentations.Editor

import android.annotation.SuppressLint
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spellcheck
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.notes_taking.API.GroqService
import com.example.notes_taking.R
import com.example.notes_taking.ui.theme.BrownCard
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import com.example.notes_taking.ui.theme.TextPrimary
import com.example.notes_taking.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ======= Content Block Types =======
sealed class ContentBlock {
    data class TextBlock(
        val id: String = UUID.randomUUID().toString(),
        var text: String = ""
    ) : ContentBlock()

    data class ImageBlock(
        val id: String = UUID.randomUUID().toString(),
        val uri: Uri
    ) : ContentBlock()

    data class AudioBlock(
        val id: String = UUID.randomUUID().toString(),
        val uri: Uri,
        val name: String
    ) : ContentBlock()

    data class BulletBlock(
        val id: String = UUID.randomUUID().toString(),
        var text: String = ""
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
    viewModel: NoteViewModel,
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val sdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    val currentDate = remember { sdf.format(Date()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var aiMenuExpanded by remember { mutableStateOf(false) }
    var isAiLoading by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var linkUrl by remember { mutableStateOf("") }

    val contentBlocks = remember { mutableStateListOf<ContentBlock>(ContentBlock.TextBlock()) }

    // حساب عدد الكلمات ووقت القراءة
    val wordCount = remember(contentBlocks) {
        derivedStateOf {
            contentBlocks.filterIsInstance<ContentBlock.TextBlock>()
                .sumOf { it.text.trim().split("\\s+".toRegex()).filter { w -> w.isNotEmpty() }.size }
        }
    }
    val readingMinutes = derivedStateOf { maxOf(1, wordCount.value / 200) }

    // حساب عدد الأحرف
    val characterCount = remember(contentBlocks) {
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

    // التحقق من وجود محتوى للحفظ
    val hasContent = derivedStateOf {
        title.isNotBlank() || contentBlocks.any { block ->
            when (block) {
                is ContentBlock.TextBlock -> block.text.isNotBlank()
                is ContentBlock.BulletBlock -> block.text.isNotBlank()
                is ContentBlock.ImageBlock -> true
                is ContentBlock.AudioBlock -> true
                is ContentBlock.LinkBlock -> block.url.isNotBlank()
            }
        }
    }

    // ======= Image Picker =======
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            try {
                val permanentPath = viewModel.saveImageToInternalStorage(context, it)
                permanentPath?.let { path ->
                    val fileUri = Uri.fromFile(File(path))
                    contentBlocks.add(ContentBlock.ImageBlock(uri = fileUri))
                    contentBlocks.add(ContentBlock.TextBlock())
                } ?: run {
                    scope.launch {
                        snackbarHostState.showSnackbar("فشل في حفظ الصورة")
                    }
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("حدث خطأ أثناء حفظ الصورة")
                }
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // ======= Audio Picker =======
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val name = uri.lastPathSegment?.substringAfterLast("/") ?: "تسجيل_${System.currentTimeMillis()}"
                contentBlocks.add(ContentBlock.AudioBlock(uri = it, name = name))
                contentBlocks.add(ContentBlock.TextBlock())
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("حدث خطأ أثناء إضافة الملف الصوتي")
                }
                e.printStackTrace()
            }
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

                    // تحميل المحتوى النصي
                    if (it.content.isNotBlank()) {
                        contentBlocks.add(ContentBlock.TextBlock(text = it.content))
                    } else {
                        contentBlocks.add(ContentBlock.TextBlock())
                    }

                    // تحميل الصورة إذا وجدت
                    it.imageUri?.let { path ->
                        val imageFile = File(path)
                        if (imageFile.exists()) {
                            contentBlocks.add(ContentBlock.ImageBlock(uri = Uri.fromFile(imageFile)))
                        }
                    }
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("فشل في تحميل الملاحظة")
                }
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
            .statusBarsPadding()
            .imePadding()
    ) {
        // ======= Top Bar =======
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.notes_screen_title_bar),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ManropeFontFamily,
                    color = TextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!hasContent.value) {
                            scope.launch {
                                snackbarHostState.showSnackbar("لا يوجد محتوى لحفظه")
                            }
                            return@Button
                        }

                        val firstImageBlock = contentBlocks
                            .filterIsInstance<ContentBlock.ImageBlock>()
                            .firstOrNull()
                        val imagePathToSave = firstImageBlock?.uri?.path

                        val fullContent = contentBlocks.joinToString("\n") { block ->
                            when (block) {
                                is ContentBlock.TextBlock -> block.text
                                is ContentBlock.BulletBlock -> "• ${block.text}"
                                else -> ""
                            }
                        }

                        viewModel.saveNoteWithAI(
                            id = noteId,
                            title = title,
                            content = fullContent,
                            imageUri = imagePathToSave,
                            date = currentDate,
                            onComplete = {
                                onSave()
                                scope.launch {
                                    snackbarHostState.showSnackbar("تم حفظ الملاحظة بنجاح")
                                }
                            },
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            }
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrownCard),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
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

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "الملف الشخصي",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = Color(0xFFE8E0D8),
            modifier = Modifier.padding(horizontal = 40.dp)
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
                    color = if (title.isEmpty()) Color(0xFFCEC0B0) else TextPrimary,
                    textAlign = TextAlign.Start
                ),
                cursorBrush = SolidColor(BrownCard),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                text = stringResource(R.string.editor_title_hint),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MansalvaFontFamily,
                                color = Color(0xFFCEC0B0),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    }
                }
            )

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
                        contentDescription = "التاريخ",
                        tint = Color(0xFFB8A898),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = currentDate,
                        fontSize = 12.sp,
                        fontFamily = ManropeFontFamily,
                        color = Color(0xFFB8A898)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "وقت القراءة",
                        tint = Color(0xFFB8A898),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.editor_reading_time, readingMinutes.value),
                        fontSize = 12.sp,
                        fontFamily = ManropeFontFamily,
                        color = Color(0xFFB8A898)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // عرض مؤشر التحميل
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrownCard)
                }
            }

            // ======= Content Blocks =======
            contentBlocks.forEachIndexed { index, block ->
                when (block) {
                    is ContentBlock.TextBlock -> {
                        BasicTextField(
                            value = block.text,
                            onValueChange = { newText ->
                                contentBlocks[index] = block.copy(text = newText)
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = ManropeFontFamily,
                                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimary,
                                lineHeight = 26.sp,
                                textAlign = TextAlign.Start
                            ),
                            cursorBrush = SolidColor(BrownCard),
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
                                            color = Color(0xFFCEC0B0),
                                            textAlign = TextAlign.Start,
                                            lineHeight = 26.sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
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
                                color = BrownCard,
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
                                    color = TextPrimary,
                                    lineHeight = 24.sp
                                ),
                                cursorBrush = SolidColor(BrownCard),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { contentBlocks.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "حذف",
                                    tint = Color.LightGray,
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
                                contentDescription = "صورة مرفقة",
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
                                    contentDescription = "حذف الصورة",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    is ContentBlock.AudioBlock -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0EB)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(BrownCard, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Mic,
                                        contentDescription = "تسجيل صوتي",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = block.name,
                                        fontSize = 13.sp,
                                        fontFamily = ManropeFontFamily,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = stringResource(R.string.audio_file),
                                        fontSize = 11.sp,
                                        fontFamily = ManropeFontFamily,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { contentBlocks.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف التسجيل",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Link,
                                    contentDescription = "رابط",
                                    tint = Color.Blue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = block.url,
                                    color = Color.Blue,
                                    fontSize = 14.sp,
                                    fontFamily = ManropeFontFamily,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { contentBlocks.removeAt(index) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "حذف الرابط",
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
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF5F0EB),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ======= AI Button + DropdownMenu =======
                Box {
                    IconButton(
                        onClick = { aiMenuExpanded = true },
                        modifier = Modifier.size(36.dp),
                        enabled = !isAiLoading
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                color = BrownCard,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "الذكاء الاصطناعي",
                                tint = BrownCard,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = aiMenuExpanded,
                        onDismissRequest = { aiMenuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = "إعادة صياغة",
                                        tint = BrownCard,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.rephrase_text),
                                        fontFamily = ManropeFontFamily,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                }
                            },
                            onClick = {
                                aiMenuExpanded = false
                                val currentText = contentBlocks
                                    .filterIsInstance<ContentBlock.TextBlock>()
                                    .joinToString("\n") { it.text }
                                    .trim()

                                if (currentText.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("لا يوجد نص لإعادة صياغته")
                                    }
                                    return@DropdownMenuItem
                                }

                                scope.launch {
                                    isAiLoading = true
                                    try {
                                        val result = GroqService.rephraseText(currentText)
                                        val firstTextIndex = contentBlocks
                                            .indexOfFirst { it is ContentBlock.TextBlock }
                                        if (firstTextIndex != -1) {
                                            contentBlocks[firstTextIndex] =
                                                ContentBlock.TextBlock(text = result)
                                            snackbarHostState.showSnackbar("تمت إعادة الصياغة بنجاح")
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        snackbarHostState.showSnackbar("فشل في إعادة الصياغة")
                                    } finally {
                                        isAiLoading = false
                                    }
                                }
                            }
                        )

                        HorizontalDivider(color = Color(0xFFF0EBE6))

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Spellcheck,
                                        contentDescription = "تشكيل النص",
                                        tint = BrownCard,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.diacritize_text),
                                        fontFamily = ManropeFontFamily,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                }
                            },
                            onClick = {
                                aiMenuExpanded = false
                                val currentText = contentBlocks
                                    .filterIsInstance<ContentBlock.TextBlock>()
                                    .joinToString("\n") { it.text }
                                    .trim()

                                if (currentText.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("لا يوجد نص لتشكيله")
                                    }
                                    return@DropdownMenuItem
                                }

                                scope.launch {
                                    isAiLoading = true
                                    try {
                                        val result = GroqService.diacritizeText(currentText)
                                        val firstTextIndex = contentBlocks
                                            .indexOfFirst { it is ContentBlock.TextBlock }
                                        if (firstTextIndex != -1) {
                                            contentBlocks[firstTextIndex] =
                                                ContentBlock.TextBlock(text = result)
                                            snackbarHostState.showSnackbar("تم تشكيل النص بنجاح")
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        snackbarHostState.showSnackbar("فشل في تشكيل النص")
                                    } finally {
                                        isAiLoading = false
                                    }
                                }
                            }
                        )
                    }
                }

                EditorToolbarButton(
                    icon = Icons.Outlined.Mic,
                    onClick = { audioPickerLauncher.launch("audio/*") }
                )

                EditorToolbarButton(
                    icon = Icons.Outlined.Link,
                    onClick = { showLinkDialog = true }
                )

                EditorToolbarButton(
                    icon = Icons.Outlined.Image,
                    onClick = { imagePickerLauncher.launch("image/*") }
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
                        color = TextSecondary
                    )
                }

                EditorToolbarButton(
                    icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                    onClick = { contentBlocks.add(ContentBlock.BulletBlock()) }
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isItalic) BrownCard.copy(alpha = 0.15f) else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { isItalic = !isItalic },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "I",
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = if (isItalic) BrownCard else TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isBold) BrownCard.copy(alpha = 0.15f) else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { isBold = !isBold },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "B",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isBold) BrownCard else TextSecondary
                        )
                    }
                }
            }
        }
    }

    // ======= Link Dialog =======
    if (showLinkDialog) {
        AddLinkDialog(
            onDismiss = { showLinkDialog = false },
            onConfirm = { url ->
                if (url.isNotBlank()) {
                    contentBlocks.add(ContentBlock.LinkBlock(url = url))
                    contentBlocks.add(ContentBlock.TextBlock())
                }
                showLinkDialog = false
            }
        )
    }
}

// ======= Toolbar Button =======
@Composable
fun EditorToolbarButton(
    icon: ImageVector,
    tint: Color = TextSecondary,
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
fun AddLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
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
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F0EB), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    textStyle = TextStyle(
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary,
                        fontSize = 14.sp
                    ),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                "https://example.com",
                                color = Color.Gray.copy(alpha = 0.5f),
                                fontFamily = ManropeFontFamily,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
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
                            text = stringResource(R.string.cancel),
                            color = TextSecondary,
                            fontFamily = ManropeFontFamily,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                var url = text.trim()
                                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                    url = "https://$url"
                                }
                                onConfirm(url)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrownCard),
                        shape = RoundedCornerShape(12.dp),
                        enabled = text.isNotBlank()
                    ) {
                        Text(
                            text = stringResource(R.string.add),
                            color = Color.White,
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