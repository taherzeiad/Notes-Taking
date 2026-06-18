package com.example.myuniqueapp.Screens.presentations.Notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myuniqueapp.Navmain.Route
import com.example.myuniqueapp.RoomDatabase.Note
import com.example.myuniqueapp.Screens.presentations.AppTopBar
import com.example.myuniqueapp.Screens.presentations.Home.BottomNavBar
import com.example.myuniqueapp.ui.theme.ManropeFontFamily
import com.example.myuniqueapp.ui.theme.MansalvaFontFamily
import com.notestalking.myuniqueapp.R


data class CategoryItem(val key: String, val labelRes: Int)


@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    val categories = remember {
        listOf(
            CategoryItem("All", R.string.note_cat_all),
            CategoryItem("Philosophy", R.string.note_cat_philosophy),
            CategoryItem("Literature", R.string.note_cat_literature),
            CategoryItem("Self-Development", R.string.note_cat_self_dev)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController, selectedTab = 2) }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "topbar") {
                        TopBarSection(
                            isSearchActive = isSearchActive,
                            searchQuery = searchQuery,
                            onSearchClick = { isSearchActive = true },
                            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                            onSearchClose = {
                                isSearchActive = false
                                viewModel.onSearchQueryChange("")
                            }
                        )
                    }

                    if (!isSearchActive) {
                        item(key = "title") { PageTitleSection() }
                    }

                    item(key = "categories") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(items = categories, key = { it.key }) { category ->
                                CategoryTab(
                                    label = stringResource(id = category.labelRes),
                                    isSelected = selectedCategory == category.key,
                                    onClick = { viewModel.onCategoryChange(category.key) }
                                )
                            }
                        }
                    }

                    if (uiState.notes.isEmpty()) {
                        item(key = "empty") {
                            EmptyNotesState(
                                message = if (searchQuery.isNotBlank())
                                    stringResource(R.string.no_search_results)
                                else stringResource(R.string.empty_notes_subtitle)
                            )
                        }
                    } else {
                        items(items = uiState.notes, key = { it.id }) { note ->
                            RoomNoteCard(
                                note = note,
                                onClick = remember(note.id) {
                                    { navController.navigate(Route.NoteEditor.createRoute(note.id)) }
                                },
                                onDelete = { noteToDelete = note }
                            )
                        }
                    }

                    item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            if (!isSearchActive) {
                AddNoteFAB(
                    onAddClick = remember {
                        { navController.navigate(Route.NoteEditor.createRoute(0)) }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 24.dp)
                )
            }
        }
    }

    // ======= Delete Dialog =======
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            icon = {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_note_title),
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_note_desc),
                    fontFamily = ManropeFontFamily,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNote(note)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.delete), fontFamily = ManropeFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(stringResource(R.string.cancel), fontFamily = ManropeFontFamily)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ======= Note Card =======
@Composable
fun RoomNoteCard(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.date,
                    fontSize = 12.sp,
                    fontFamily = ManropeFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (note.isPinned) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pinned),
                                fontSize = 12.sp,
                                fontFamily = ManropeFontFamily,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    // ← زر الحذف
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note.title.ifBlank { stringResource(R.string.editor_title_hint) },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    fontFamily = ManropeFontFamily,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 22.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!note.imageUri.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = note.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.read_more),
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        Icons.AutoMirrored.Outlined.MenuBook,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopBarSection(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit
) {
    if (isSearchActive) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.search),
                    fontFamily = ManropeFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onBackground
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    onSearchQueryChange("")
                    onSearchClose()
                }) {
                    Icon(
                        Icons.Outlined.Close,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            singleLine = true
        )
    } else {
        AppTopBar(
            title = stringResource(R.string.notes_screen_title_bar), onSearchClick = onSearchClick
        )
    }
}

@Composable
fun CategoryTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyNotesState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = stringResource(R.string.empty_notes_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = message,
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun PageTitleSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.notes_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.notes_subtitle),
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AddNoteFAB(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onAddClick,
        containerColor = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        modifier = modifier
    ) {
        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
    }
}