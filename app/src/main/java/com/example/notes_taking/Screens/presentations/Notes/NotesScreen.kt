package com.example.notes_taking.Screens.presentations.Notes

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
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
import com.example.notes_taking.Navmain.Route
import com.example.notes_taking.R
import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily

data class CategoryItem(val key: String, val labelRes: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // ← إخفاء BottomBar في وضع التحديد
            if (!uiState.isSelectionMode) {
                BottomNavBar(navController = navController, selectedTab = 2)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    // ======= Top Bar =======
                    item(key = "topbar") {
                        AnimatedContent(
                            targetState = uiState.isSelectionMode,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "topbar"
                        ) { isSelectionMode ->
                            if (isSelectionMode) {
                                // ← Top Bar وضع التحديد
                                SelectionTopBar(
                                    selectedCount = uiState.selectedNoteIds.size,
                                    totalCount = uiState.notes.size,
                                    onClose = { viewModel.clearSelection() },
                                    onSelectAll = { viewModel.selectAll() },
                                    onDelete = { showDeleteDialog = true }
                                )
                            } else {
                                // ← Top Bar عادي
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
                        }
                    }

                    // ======= Page Title =======
                    if (!isSearchActive && !uiState.isSelectionMode) {
                        item(key = "title") { PageTitleSection() }
                    }

                    // ======= Category Tabs =======
                    if (!uiState.isSelectionMode) {
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
                    }

                    // ======= Notes List =======
                    if (uiState.notes.isEmpty()) {
                        item(key = "empty") {
                            EmptyNotesState(
                                message = if (searchQuery.isNotBlank())
                                    stringResource(R.string.no_search_results)
                                else stringResource(R.string.empty_notes_subtitle)
                            )
                        }
                    } else {
                        items(items = uiState.notes, key = { note -> note.id }) { note ->
                            val isSelected = note.id in uiState.selectedNoteIds

                            SelectableNoteCard(
                                note = note,
                                isSelected = isSelected,
                                isSelectionMode = uiState.isSelectionMode,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleNoteSelection(note.id)
                                    } else {
                                        navController.navigate(
                                            Route.NoteEditor.createRoute(note.id)
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (!uiState.isSelectionMode) {
                                        viewModel.enterSelectionMode(note.id)
                                    }
                                },
                                onDeleteSingle = {
                                    noteToDelete = note
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            // ======= FAB =======
            if (!isSearchActive && !uiState.isSelectionMode) {
                AddNoteFAB(
                    onAddClick = remember {
                        { navController.navigate(Route.NoteEditor.createRoute(0)) }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 24.dp)
                )
            }

            // ======= Bottom Action Bar في وضع التحديد =======
            AnimatedVisibility(
                visible = uiState.isSelectionMode,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                SelectionBottomBar(
                    selectedCount = uiState.selectedNoteIds.size,
                    onDelete = { showDeleteDialog = true },
                    onCancel = { viewModel.clearSelection() }
                )
            }
        }
    }

    // ======= Delete Dialog =======
    if (showDeleteDialog) {
        val isMultiple = uiState.isSelectionMode && uiState.selectedNoteIds.size > 1
        val isSingle = noteToDelete != null

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                noteToDelete = null
            },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = when {
                        isSingle -> if (isArabicLocale()) "حذف الملاحظة" else "Delete Note"
                        isMultiple -> if (isArabicLocale())
                            "حذف ${uiState.selectedNoteIds.size} ملاحظات"
                        else "Delete ${uiState.selectedNoteIds.size} Notes"
                        else -> if (isArabicLocale()) "حذف الملاحظة" else "Delete Note"
                    },
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = if (isArabicLocale())
                        "هذا الإجراء لا يمكن التراجع عنه. هل أنت متأكد؟"
                    else
                        "This action cannot be undone. Are you sure?",
                    fontFamily = ManropeFontFamily,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        if (noteToDelete != null) {
                            val note = noteToDelete!!
                            noteToDelete = null
                            viewModel.deleteSingleNote(note)
                        } else {
                            viewModel.deleteSelectedNotes()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isArabicLocale()) "حذف" else "Delete",
                        fontFamily = ManropeFontFamily
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    noteToDelete = null
                }) {
                    Text(
                        if (isArabicLocale()) "إلغاء" else "Cancel",
                        fontFamily = ManropeFontFamily
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ======= Helper =======
fun isArabicLocale() = java.util.Locale.getDefault().language == "ar"

// ======= Selection Top Bar =======
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    totalCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ← زر الإغلاق
        IconButton(onClick = onClose) {
            Icon(
                Icons.Outlined.Close,
                null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        // ← عدد المحدد
        Text(
            text = if (isArabicLocale()) "تم تحديد $selectedCount"
            else "$selectedCount selected",
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row {
            // ← تحديد الكل
            TextButton(onClick = onSelectAll) {
                Text(
                    text = if (isArabicLocale()) "الكل" else "All",
                    fontFamily = ManropeFontFamily,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // ← حذف
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ======= Selection Bottom Bar =======
@Composable
fun SelectionBottomBar(
    selectedCount: Int,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ← زر إلغاء
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isArabicLocale()) "إلغاء" else "Cancel",
                    fontFamily = ManropeFontFamily
                )
            }

            // ← زر حذف
            Button(
                onClick = onDelete,
                enabled = selectedCount > 0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabicLocale()) "حذف ($selectedCount)"
                    else "Delete ($selectedCount)",
                    fontFamily = ManropeFontFamily
                )
            }
        }
    }
}

// ======= Selectable Note Card =======
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableNoteCard(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteSingle: () -> Unit
) {
    var showSwipeActions by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                // ← Border عند التحديد
                .then(
                    if (isSelected) Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(20.dp)
                    ) else Modifier
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // ← Checkbox في وضع التحديد
                AnimatedVisibility(
                    visible = isSelectionMode,
                    enter = slideInHorizontally { -it } + fadeIn(),
                    exit = slideOutHorizontally { -it } + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                Icons.Outlined.RadioButtonUnchecked,
                                null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // ← محتوى الكارد
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
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
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                            // ← زر حذف سريع (يظهر فقط في الوضع العادي)
                            if (!isSelectionMode) {
                                IconButton(
                                    onClick = onDeleteSingle,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.5f
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
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
    }
}

// ======= باقي الـ Composables =======
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
                Text(stringResource(R.string.search), fontFamily = ManropeFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onBackground)
            },
            trailingIcon = {
                IconButton(onClick = { onSearchQueryChange(""); onSearchClose() }) {
                    Icon(Icons.Outlined.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Outlined.Search, null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp))
            }
            Text(
                text = stringResource(R.string.notes_screen_title_bar),
                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onBackground
            )
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun CategoryTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondaryContainer
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = label, fontSize = 14.sp, fontFamily = ManropeFontFamily,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun EmptyNotesState(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Outlined.MenuBook, null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp))
        }
        Text(stringResource(R.string.empty_notes_title), fontSize = 18.sp,
            fontWeight = FontWeight.Bold, fontFamily = MansalvaFontFamily,
            color = MaterialTheme.colorScheme.onBackground)
        Text(message, fontSize = 14.sp, fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp))
    }
}

@Composable
fun PageTitleSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.notes_title), fontSize = 36.sp,
            fontWeight = FontWeight.Bold, fontFamily = MansalvaFontFamily,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.notes_subtitle), fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
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