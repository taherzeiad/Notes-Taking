package com.example.notes_taking.Screens.presentations.Home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.notes_taking.R
import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.Screens.presentations.CreateNote.NoteViewModel
import com.example.notes_taking.ui.theme.*

@Composable
fun HomeScreen(
    onAddNote: () -> Unit, onEditNote: (Int) -> Unit, viewModel: NoteViewModel
) {

    val notes by viewModel.allNotes.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        stringResource(R.string.all_notes),
        stringResource(R.string.pinned_notes),
        stringResource(R.string.others)
    )

    var selectedFilter by remember { mutableStateOf(filterOptions[0]) }

    LaunchedEffect(filterOptions) {
        if (!filterOptions.contains(selectedFilter)) {
            selectedFilter = filterOptions[0]
        }
    }
    val searchedNotes = notes.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(
            searchQuery, ignoreCase = true
        )
    }

    val pinnedNotes = when (selectedFilter) {
        stringResource(R.string.others) -> emptyList()
        else -> searchedNotes.filter { it.isPinned }
    }

    val otherNotes = when (selectedFilter) {
        stringResource(R.string.pinned_notes) -> emptyList()
        else -> searchedNotes.filter { !it.isPinned }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ======= Header =======
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // 1. قسم اختيار نوع الملاحظات (All Notes / Pinned / Others)
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { expanded = true }) {
                            Text(
                                text = selectedFilter,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = ManropeFontFamily,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ArrowDropUp
                                else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(CardWhite)
                        ) {
                            filterOptions.forEach { option ->
                                DropdownMenuItem(text = {
                                    Text(
                                        text = option,
                                        fontFamily = ManropeFontFamily,
                                        fontSize = 15.sp,
                                        color = if (option == selectedFilter) FabColor else TextPrimary,
                                        fontWeight = if (option == selectedFilter) FontWeight.Bold else FontWeight.Normal
                                    )
                                }, onClick = {
                                    selectedFilter = option
                                    expanded = false
                                }, leadingIcon = {
                                    if (option == selectedFilter) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = FabColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                })
                            }
                        }
                    }

                    LanguageToggleButton()
                }
            }

            // ======= Search Bar =======
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search),
                            color = TextSecondary,
                            fontFamily = ManropeFontFamily
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search, contentDescription = null, tint = TextSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardWhite,
                        focusedContainerColor = CardWhite,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = FabColor.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
            }

            // ======= Pinned Section =======
            if (pinnedNotes.isNotEmpty()) {
                item {
                    Text(
                        text = "Pinned",
                        fontSize = 14.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pinnedNotes) { note ->
                            PinnedNoteCard(
                                note = note,
                                onClick = { onEditNote(note.id) },
                                onUnpin = { viewModel.togglePin(note) })
                        }
                    }
                }
            }

            // ======= Others Section =======
            if (otherNotes.isNotEmpty()) {
                item {
                    Text(
                        text = "Others",
                        fontSize = 14.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(otherNotes) { note ->
                    OtherNoteCard(
                        note = note,
                        onDelete = { viewModel.deleteNote(note) },
                        onClick = { onEditNote(note.id) },
                        onPin = { viewModel.togglePin(note) })
                }
            }

            // ======= Empty State =======
            if (pinnedNotes.isEmpty() && otherNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notes yet!\nTap + to add one",
                            color = TextSecondary,
                            fontFamily = ManropeFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // ======= FAB =======
        FloatingActionButton(
            onClick = onAddNote,
            containerColor = FabColor,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.material_symbols),
                contentDescription = "Add Note",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ======= Pinned Note Card =======
@Composable
fun PinnedNoteCard(note: Note, onClick: () -> Unit, onUnpin: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardYellow),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = ManropeFontFamily,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = note.date,
                    fontSize = 12.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    fontSize = 13.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextPrimary.copy(alpha = 0.8f),
                    lineHeight = 18.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // زر إلغاء التثبيت
            IconButton(
                onClick = onUnpin, modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Unpin",
                    tint = FabColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ======= Other Note Card =======
@Composable
fun OtherNoteCard(
    note: Note, onClick: () -> Unit, onDelete: () -> Unit, onPin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            // صورة إن وجدت
            if (!note.imageUri.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = note.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                Color.Black.copy(alpha = 0.35f),
                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = note.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = ManropeFontFamily,
                            color = Color.White
                        )
                        Text(
                            text = note.date,
                            fontSize = 12.sp,
                            fontFamily = ManropeFontFamily,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (note.imageUri.isNullOrEmpty()) {
                            Text(
                                text = note.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = ManropeFontFamily,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = note.date,
                                fontSize = 12.sp,
                                fontFamily = ManropeFontFamily,
                                color = TextSecondary
                            )
                        }
                    }

                    // أزرار التثبيت والحذف
                    Row {
                        IconButton(onClick = onPin, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pin",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note.content,
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary.copy(alpha = 0.8f),
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageToggleButton() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // يفضل جلب اللغة الحالية من الإعدادات مباشرة
    var currentLang by remember {
        mutableStateOf(prefs.getString("language", "en") ?: "en")
    }

    OutlinedButton(
        onClick = {
            val newLang = if (currentLang == "en") "ar" else "en"

            // 1. حفظ اللغة الجديدة في SharedPreferences
            prefs.edit().putString("language", newLang).apply()

            // 2. تحديث الحالة المحلية للزر
            currentLang = newLang

            com.example.notes_taking.utils.LocaleUtils.setLocale(context, newLang)

            (context as? android.app.Activity)?.recreate()
        },
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FabColor.copy(alpha = 0.5f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = if (currentLang == "en") "عربي" else "English",
            fontFamily = ManropeFontFamily,
            fontSize = 13.sp,
            color = FabColor
        )
    }
}