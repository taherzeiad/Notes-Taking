package com.example.notes_taking.Screens.presentations.Editor

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
}

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

    var title by remember { mutableStateOf("") }
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var aiMenuExpanded by remember { mutableStateOf(false) }
    var isAiLoading by remember { mutableStateOf(false) }

    val contentBlocks = remember { mutableStateListOf<ContentBlock>(ContentBlock.TextBlock()) }

    val wordCount = contentBlocks
        .filterIsInstance<ContentBlock.TextBlock>()
        .sumOf { it.text.trim().split("\\s+".toRegex()).filter { w -> w.isNotEmpty() }.size }
    val readingMinutes = maxOf(1, wordCount / 200)

    // ======= Image Picker =======
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val permanentPath = viewModel.saveImageToInternalStorage(context, it)
            permanentPath?.let { path ->
                val fileUri = Uri.fromFile(File(path))
                contentBlocks.add(ContentBlock.ImageBlock(uri = fileUri))
                contentBlocks.add(ContentBlock.TextBlock())
            }
        }
    }

    // ======= Audio Picker =======
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val name = it.lastPathSegment ?: "audio_${System.currentTimeMillis()}"
            contentBlocks.add(ContentBlock.AudioBlock(uri = it, name = name))
            contentBlocks.add(ContentBlock.TextBlock())
        }
    }

    // ======= Load Note =======
    LaunchedEffect(noteId) {
        if (noteId > 0) {
            val note = viewModel.getNoteById(noteId)
            note?.let {
                title = it.title
                contentBlocks.clear()
                contentBlocks.add(ContentBlock.TextBlock(text = it.content))
                it.imageUri?.let { path ->
                    val imageFile = File(path)
                    if (imageFile.exists()) {
                        contentBlocks.add(ContentBlock.ImageBlock(uri = Uri.fromFile(imageFile)))
                    }
                }
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
                        contentDescription = null,
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
                            onComplete = onSave
                        )
                        onSave()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrownCard),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = stringResource(R.string.editor_save),
                        fontSize = 14.sp,
                        fontFamily = ManropeFontFamily,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        contentDescription = null,
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
                    color = if (title.isEmpty()) Color(0xFFCEC0B0) else Color.Black,
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
                        contentDescription = null,
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
                        contentDescription = null,
                        tint = Color(0xFFB8A898),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.editor_reading_time, readingMinutes),
                        fontSize = 12.sp,
                        fontFamily = ManropeFontFamily,
                        color = Color(0xFFB8A898)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ======= Content Blocks =======
            contentBlocks.forEachIndexed { index, block ->
                when (block) {

                    // ← Text Block
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
                                    minHeight = if (contentBlocks.size == 1) 300.dp else 48.dp
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

                    // ← Bullet Block
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
                                    null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // ← Image Block
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

                    // ← Audio Block
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
                                        contentDescription = null,
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
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
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
                        modifier = Modifier.size(36.dp)
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
                                contentDescription = null,
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

                        // ← إعادة صياغة
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
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

                                if (currentText.isBlank()) return@DropdownMenuItem

                                scope.launch {
                                    isAiLoading = true
                                    try {
                                        val result = GroqService.rephraseText(currentText)
                                        val firstTextIndex = contentBlocks
                                            .indexOfFirst { it is ContentBlock.TextBlock }
                                        if (firstTextIndex != -1) {
                                            contentBlocks[firstTextIndex] =
                                                ContentBlock.TextBlock(text = result)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isAiLoading = false
                                    }
                                }
                            }
                        )

                        HorizontalDivider(color = Color(0xFFF0EBE6))

                        // ← تشكيل النص
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Spellcheck,
                                        contentDescription = null,
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

                                if (currentText.isBlank()) return@DropdownMenuItem

                                scope.launch {
                                    isAiLoading = true
                                    try {
                                        val result = GroqService.diacritizeText(currentText)
                                        val firstTextIndex = contentBlocks
                                            .indexOfFirst { it is ContentBlock.TextBlock }
                                        if (firstTextIndex != -1) {
                                            contentBlocks[firstTextIndex] =
                                                ContentBlock.TextBlock(text = result)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isAiLoading = false
                                    }
                                }
                            }
                        )
                    }
                }

                // ← Mic
                EditorToolbarButton(
                    icon = Icons.Outlined.Mic,
                    onClick = { audioPickerLauncher.launch("audio/*") }
                )

                // ← Link
                EditorToolbarButton(
                    icon = Icons.Outlined.Link,
                    onClick = {}
                )

                // ← Image
                EditorToolbarButton(
                    icon = Icons.Outlined.Image,
                    onClick = { imagePickerLauncher.launch("image/*") }
                )

                // ← Quote
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "99",
                        fontSize = 16.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                // ← Bullets
                EditorToolbarButton(
                    icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                    onClick = { contentBlocks.add(ContentBlock.BulletBlock()) }
                )

                // ← Italic
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

                // ← Bold
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