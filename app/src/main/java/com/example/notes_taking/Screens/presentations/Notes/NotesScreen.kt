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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.BrownCard
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import com.example.notes_taking.ui.theme.PageBackground
import com.example.notes_taking.ui.theme.TextPrimary
import com.example.notes_taking.ui.theme.TextSecondary

// ======= Data Models =======
data class NoteCardData(
    val id: Int,
    val tag: String,
    val date: String = "",
    val title: String,
    val content: String = "",
    val imageUrl: String? = null,
    val isItalic: Boolean = false,
    val bullets: List<String> = emptyList(),
    val type: NoteCardType = NoteCardType.TEXT
)

enum class NoteCardType { TEXT, IMAGE, BULLETS }

// ======= Sample Data =======
val sampleNotes = listOf(
    NoteCardData(
        id = 1,
        tag = "فلسفة الوجود",
        date = "١٤ أكتوبر ٢٠٢٣",
        title = "تأملات في مفهوم \"السكينة الرقمية\" في العصر الحديث",
        content = "في خضم الضوضاء الرقمية المتصاعدة، تبرز الحاجة الماسة إلى خلق مساحات من الصمت المتعمد. ليس السكينة مجرد غياب للصوت، بل هي حضور واعٍ للإيجاد",
        type = NoteCardType.TEXT
    ),
    NoteCardData(
        id = 2,
        tag = "أدب",
        title = "شذرات من ديوان المتنبي",
        content = "﷽﷽﷽﷽﷽ . ﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽ - ﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽",
        isItalic = true,
        type = NoteCardType.TEXT
    ),
    NoteCardData(
        id = 3,
        tag = "",
        title = "﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽﷽",
        imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400",
        type = NoteCardType.IMAGE
    ),
    NoteCardData(
        id = 4,
        tag = "تطوير الذات",
        title = "قائمة قراءات الشتاء",
        bullets = listOf(
            "البحث عن المعنى - فيكتور فرانكل",
            "التركيز العميق - كال نيوبورت"
        ),
        content = "محدثة منذ ساعتين",
        type = NoteCardType.BULLETS
    ),
    NoteCardData(
        id = 5,
        tag = "مشاريع",
        title = "مخطط بودكاست \"أصداء\"",
        content = "الحلقة الأولى: ما وراء الكلمات. استضافة د. سارة لمناقشة أثر الله على تشكيل الوعي الجمعي.",
        type = NoteCardType.TEXT
    )
)

val noteCategories = listOf("الكل", "فلسفة", "أدب", "تطوير الذات")

@Composable
fun NotesScreen(
    navController: NavHostController,
    onAddNote: () -> Unit = {},
    onEditNote: (Int) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("الكل") }

    val filteredNotes = if (selectedCategory == "الكل") sampleNotes
    else sampleNotes.filter { it.tag.contains(selectedCategory) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = PageBackground,
            bottomBar = {
                BottomNavBar(navController = navController, selectedTab = 2)
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ======= Top Bar =======
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Start ← ينعكس تلقائياً
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )

                        Text(
                            text = "الملاذ الفكري",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ManropeFontFamily,
                            color = TextPrimary
                        )

                        // Avatar End ← ينعكس تلقائياً
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

                // ======= Page Title =======
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "الملاحظات",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MansalvaFontFamily,
                            color = TextPrimary,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "حيث تسكن الأفكار وتنمو الرؤى",
                            fontSize = 14.sp,
                            fontFamily = ManropeFontFamily,
                            color = TextSecondary,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ======= Category Tabs =======
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = true // ← للعربية تبدأ من اليمين
                    ) {
                        items(noteCategories) { category ->
                            val isSelected = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) BrownCard
                                        else Color(0xFFEDE8E3)
                                    )
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 14.sp,
                                    fontFamily = ManropeFontFamily,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // ======= Notes List =======
                items(filteredNotes) { note ->
                    when (note.type) {
                        NoteCardType.IMAGE -> ImageNoteCard(note = note, onClick = { onEditNote(note.id) })
                        NoteCardType.BULLETS -> BulletsNoteCard(note = note, onClick = { onEditNote(note.id) })
                        else -> TextNoteCard(note = note, onClick = { onEditNote(note.id) })
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // ======= FAB =======
        FloatingActionButton(
            onClick = onAddNote,
            containerColor = BrownCard,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart) // ← Start ينعكس تلقائياً
                .padding(start = 24.dp, bottom = 80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Note",
                tint = Color.White
            )
        }
    }
}

// ======= Text Note Card =======
@Composable
fun TextNoteCard(note: NoteCardData, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Tag + Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Start ← ينعكس تلقائياً
                if (note.date.isNotEmpty()) {
                    Text(
                        text = note.date,
                        fontSize = 12.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextSecondary
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Tag End ← ينعكس تلقائياً
                if (note.tag.isNotEmpty()) {
                    NoteTag(tag = note.tag)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = TextPrimary,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            if (note.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
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

            if (!note.isItalic && note.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "اقرأ المزيد",
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        color = BrownCard,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = "←", fontSize = 13.sp, color = BrownCard)
                }
            }
        }
    }
}

// ======= Image Note Card =======
@Composable
fun ImageNoteCard(note: NoteCardData, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            AsyncImage(
                model = note.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
            )
            // Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            )
            // Text overlay
            Text(
                text = note.title,
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (note.tag.isNotEmpty()) NoteTag(tag = note.tag)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ManropeFontFamily,
                color = TextPrimary,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            note.bullets.forEach { bullet ->
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
                        text = bullet,
                        fontSize = 14.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (note.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
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
fun NoteTag(tag: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF0EBE6), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = tag,
            fontSize = 12.sp,
            fontFamily = ManropeFontFamily,
            color = BrownCard,
            fontWeight = FontWeight.Medium
        )
    }
}