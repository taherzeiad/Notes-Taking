package com.example.notes_taking.Screens.presentations.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes_taking.R
import com.example.notes_taking.ui.theme.BackgroundColor
import com.example.notes_taking.ui.theme.CardPurple
import com.example.notes_taking.ui.theme.CardWhite
import com.example.notes_taking.ui.theme.CardYellow
import com.example.notes_taking.ui.theme.FabColor
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.TextPrimary
import com.example.notes_taking.ui.theme.TextSecondary

// ======= Data Class =======
data class Note(
    val id: Int,
    val title: String,
    val date: String,
    val preview: String,
    val isPinned: Boolean = false,
    val cardColor: Color = CardWhite,
    val imageRes: Int? = null
)

// ======= Sample Data =======
val sampleNotes = listOf(
    Note(
        1,
        "Untitled",
        "14/09/2023",
        "Lorem Ipsum is simply dummy text of the prin...",
        isPinned = true,
        cardColor = CardYellow
    ), Note(
        2,
        "Travel Plan",
        "2h Ago",
        "Lorem Ipsum is simply dummy text of the prin...",
        isPinned = true,
        cardColor = CardPurple
    ), Note(
        2,
        "Travel Plan",
        "2h Ago",
        "Lorem Ipsum is simply dummy text of the prin...",
        isPinned = true,
        cardColor = CardPurple
    ), Note(3, "Accounts", "Yesterday", "Lorem Ipsum is simply dummy text of the prin..."), Note(
        4,
        "Movies List",
        "14/09/2023",
        "Lorem Ipsum is simply dummy text of the prin...",
        imageRes = R.drawable.movies_bg
    ), Note(5, "Todo's 2023 Plan", "01/01/2023", "Lorem Ipsum is simply dummy text of the prin...")
)

// ======= Main Screen =======
@Composable
fun HomeScreen(onAddNote: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val pinnedNotes = sampleNotes.filter { it.isPinned }
    val otherNotes = sampleNotes.filter { !it.isPinned }

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Notes",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = TextPrimary
                    )
                }
            }

            // ======= Search Bar =======
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search...", color = TextSecondary, fontFamily = ManropeFontFamily
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = FabColor.copy(alpha = 0.5f),
                        unfocusedContainerColor = CardWhite,
                        focusedContainerColor = CardWhite
                    ),
                    singleLine = true
                )
            }

            // ======= Pinned Section =======
            item {
                Text(
                    text = "Pinned",
                    fontSize = 14.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pinnedNotes) { note ->
                        PinnedNoteCard(note = note)
                    }
                }
            }

            // ======= Others Section =======
            item {
                Text(
                    text = "Others",
                    fontSize = 14.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(otherNotes) { note ->
                OtherNoteCard(note = note)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // ======= FAB =======
        FloatingActionButton(
            onClick = { onAddNote() },
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
fun PinnedNoteCard(note: Note) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = note.cardColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
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
                color = TextPrimary
            )
            Text(
                text = note.date,
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = note.preview,
                fontSize = 13.sp,
                fontFamily = ManropeFontFamily,
                color = TextPrimary.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

// ======= Other Note Card =======
@Composable
fun OtherNoteCard(note: Note) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        // إذا فيه صورة
        if (note.imageRes != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = note.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                // overlay داكن فوق الصورة
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

        // النص دائماً موجود
        Column(modifier = Modifier.padding(14.dp)) {
            if (note.imageRes == null) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = ManropeFontFamily,
                    color = TextPrimary
                )
                Text(
                    text = note.date,
                    fontSize = 12.sp,
                    fontFamily = ManropeFontFamily,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = note.preview,
                fontSize = 13.sp,
                fontFamily = ManropeFontFamily,
                color = TextPrimary.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}
