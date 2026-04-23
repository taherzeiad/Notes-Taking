package com.example.notes_taking.Screens.presentations.Editor

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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes_taking.R
import com.example.notes_taking.Screens.presentations.CreateNote.NoteViewModel
import com.example.notes_taking.ui.theme.BrownCard
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import com.example.notes_taking.ui.theme.TextPrimary
import com.example.notes_taking.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun NoteEditorScreen(
    noteId: Int = 0,
    viewModel: NoteViewModel,
    onClose: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val sdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    val currentDate = remember { sdf.format(Date()) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }

    // حساب وقت القراءة
    val wordCount = content.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
    val readingMinutes = maxOf(1, wordCount / 200)

    LaunchedEffect(noteId) {
        if (noteId > 0) {
            val note = viewModel.getNoteById(noteId)
            note?.let {
                title = it.title
                content = it.content
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
            // Start: X + App Name ← ينعكس تلقائياً
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
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

            // End: Avatar + Save ← ينعكس تلقائياً
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save Button
                Button(
                    onClick = {
                        if (noteId > 0) {
                            viewModel.updateNote(noteId, title, content, null, currentDate, onSave)
                        } else {
                            viewModel.saveNote(noteId, title, content, null, currentDate, onSave)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrownCard
                    ),
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

                // Avatar
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

        // ======= Divider =======
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

            // ======= Title Field =======
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    fontSize = 25.sp,
                    fontFamily = ManropeFontFamily,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    color = if (content.isNotEmpty()) Color.Black else TextPrimary,
                    lineHeight = 26.sp,
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
                                modifier = Modifier.fillMaxWidth(),

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
                // Date ← Start ينعكس تلقائياً
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

                // Reading Time
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

            // ======= Content Field =======
            BasicTextField(
                value = content,
                onValueChange = { content = it },
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
                    .defaultMinSize(minHeight = 300.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (content.isEmpty()) {
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
                // AI ← Start ينعكس تلقائياً
                EditorToolbarButton(
                    icon = Icons.Outlined.AutoAwesome,
                    tint = BrownCard,
                    onClick = {}
                )
                EditorToolbarButton(
                    icon = Icons.Outlined.Mic,
                    onClick = {}
                )
                EditorToolbarButton(
                    icon = Icons.Outlined.Link,
                    onClick = {}
                )
                EditorToolbarButton(
                    icon = Icons.Outlined.Image,
                    onClick = {}
                )
                // Quote button
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
                EditorToolbarButton(
                    icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                    onClick = {}
                )
                // Italic
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isItalic) BrownCard.copy(alpha = 0.15f) else Color.Transparent),
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
                // Bold
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBold) BrownCard.copy(alpha = 0.15f) else Color.Transparent),
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
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}