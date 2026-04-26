package com.example.notes_taking.Screens.presentations.Notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.BrownCard
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import com.example.notes_taking.ui.theme.PageBackground
import com.example.notes_taking.ui.theme.TextPrimary
import com.example.notes_taking.ui.theme.TextSecondary

@Composable
fun NotesScreen(
    viewModel: NotesViewModel, navController: NavHostController
) {
    // مراقبة البيانات من الـ ViewModel
    val notes by viewModel.notesState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val categories = listOf(
        stringResource(R.string.note_cat_all),
        stringResource(R.string.note_cat_philosophy),
        stringResource(R.string.note_cat_literature),
        stringResource(R.string.note_cat_self_dev)
    )

    Scaffold(
        containerColor = PageBackground,
        bottomBar = { BottomNavBar(navController = navController, selectedTab = 2) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Top Bar (Search + Profile)
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    TopBarSection()
                }

                // 2. Page Title
                item { PageTitleSection() }

                // 3. Category Tabs (الديناميكية الآن من الـ ViewModel)
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            CategoryTab(
                                label = category,
                                isSelected = isSelected,
                                onClick = { viewModel.onCategoryChange(category) })
                        }
                    }
                }

                // 4. Notes List (جلب البيانات الفعلية)
                items(notes) { note ->
                    NoteDispatcher(note, navController)
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            // 5. FAB
            AddNoteFAB(
                onAddClick = { navController.navigate(Route.NoteEditor.createRoute(0)) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
fun CategoryTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) BrownCard else Color(0xFFEDE8E3))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = if (isSelected) Color.White else TextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun NoteDispatcher(note: NoteCardData, navController: NavHostController) {
    val onNoteClick = { navController.navigate(Route.NoteEditor.createRoute(note.id)) }

    when (note.type) {
        NoteCardType.IMAGE -> ImageNoteCard(note = note, onClick = onNoteClick)
        NoteCardType.BULLETS -> BulletsNoteCard(note = note, onClick = onNoteClick)
        else -> TextNoteCard(note = note, onClick = onNoteClick)
    }
}

// ======= Text Note Card =======
@Composable
fun TextNoteCard(note: NoteCardData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.dateRes != 0) {
                    Text(
                        text = stringResource(note.dateRes),
                        fontSize = 12.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextSecondary
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                if (note.tagRes != 0) {
                    NoteTag(tagRes = note.tagRes)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(note.titleRes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = TextPrimary,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            if (note.contentRes != 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(note.contentRes),
                    fontSize = 14.sp,
                    fontFamily = ManropeFontFamily,
                    fontStyle = if (note.isItalic) FontStyle.Italic else FontStyle.Normal,
                    color = TextPrimary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Start,
                    lineHeight = 22.sp,
                    maxLines = if (note.isItalic) 3 else 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!note.isItalic && note.contentRes != 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.read_more),
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        color = BrownCard,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = BrownCard,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ======= Image Note Card =======
@Composable
fun ImageNoteCard(note: NoteCardData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = note.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            )
            Text(
                text = stringResource(note.titleRes),
                fontSize = 14.sp,
                fontFamily = ManropeFontFamily,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

// ======= Bullets Note Card =======
@Composable
fun BulletsNoteCard(note: NoteCardData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            if (note.tagRes != 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    NoteTag(tagRes = note.tagRes)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = stringResource(note.titleRes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = TextPrimary,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            note.bulletsRes.forEach { bulletRes ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = BrownCard,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(bulletRes),
                        fontSize = 14.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (note.contentRes != 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(note.contentRes),
                    fontSize = 12.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextSecondary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ======= Note Tag =======
@Composable
fun NoteTag(tagRes: Int) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF0EBE6), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(tagRes),
            fontSize = 12.sp,
            fontFamily = ManropeFontFamily,
            color = BrownCard,
            fontWeight = FontWeight.Medium
        )
    }
}

// ======= Top Bar Section =======
@Composable
fun TopBarSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(26.dp)
        )

        Text(
            text = stringResource(R.string.notes_screen_title_bar),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ManropeFontFamily,
            color = TextPrimary
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrownCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ======= Page Title Section =======
@Composable
fun PageTitleSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.notes_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = TextPrimary,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.notes_subtitle),
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = TextSecondary,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ======= Floating Action Button =======
@Composable
fun AddNoteFAB(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onAddClick,
        containerColor = BrownCard,
        shape = CircleShape,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White
        )
    }
}