package com.example.notes_taking.Screens.presentations.Home

import HomeViewModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.notes_taking.Navmain.Route
import com.example.notes_taking.R
import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.ui.theme.BrownCard
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import com.example.notes_taking.ui.theme.PageBackground
import com.example.notes_taking.ui.theme.TagBg
import com.example.notes_taking.ui.theme.TagText
import com.example.notes_taking.ui.theme.TextPrimary
import com.example.notes_taking.ui.theme.TextSecondary
import com.example.notes_taking.ui.theme.WaveColor

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavHostController,
    onAddNote: () -> Unit,
    onEditNote: (Int) -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val lastNote by viewModel.lastEditedNote.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PageBackground,
        bottomBar = { BottomNavBar(navController = navController, selectedTab = 3) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. الترويسة والترحيب
            item { HomeTopBarSection() }
            item { WelcomeSection(userName = "طاهر") }

            // 2. بطاقة الذكاء الاصطناعي
            item { AICardSection() }

            // 3. المهام القادمة
            item { UpcomingTaskSection(onViewAll = onNavigateToTasks) }

            // 4. آخر ملاحظة (البيانات تأتي من الـ ViewModel)
            item {
                LastEditedNoteSection(
                    note = lastNote,
                    onEditClick = { id -> onEditNote(id) },
                    onAddNote = onAddNote  
                )
            }

            // 5. الإجراءات السريعة
            item { QuickActionsSection(onAddNote = onAddNote) }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }
    }
}

@Composable
fun HomeTopBarSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrownCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Text(
            stringResource(R.string.app_name_styled),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ManropeFontFamily,
            color = TextPrimary
        )
        Icon(
            Icons.AutoMirrored.Outlined.MenuBook,
            null,
            tint = TextPrimary,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun WelcomeSection(userName: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.welcome_user, userName),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = TextPrimary
        )
        Text(
            text = stringResource(R.string.welcome_subtitle),
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = TextSecondary
        )
    }
}

@Composable
fun QuickActionsSection(onAddNote: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton(
                label = stringResource(R.string.voice_record),
                icon = Icons.Outlined.Mic,
                modifier = Modifier.weight(1f),
                onClick = {})
            QuickActionButton(
                label = stringResource(R.string.quick_idea),
                icon = Icons.Outlined.Lightbulb,
                modifier = Modifier.weight(1f),
                onClick = onAddNote
            )
        }
        QuickActionButton(
            label = stringResource(R.string.add_document),
            icon = Icons.Outlined.Image,
            modifier = Modifier.fillMaxWidth(),
            onClick = {})
    }
}

// ======= المكونات المنفصلة (Components) =======

@Composable
fun LastEditedNoteSection(note: Note?, onEditClick: (Int) -> Unit, onAddNote: () -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.last_edited_note),
            fontSize = 15.sp,
            fontFamily = ManropeFontFamily,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (note == null) {
            // ======= Empty State =======
            EmptyNoteCard(onAddNote = onAddNote)
        } else {
            // ======= Note Card =======
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditClick(note.id) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    WaveChart(modifier = Modifier.fillMaxWidth().height(100.dp))
                    Column(modifier = Modifier.padding(16.dp)) {
                        NoteTagsRow(tags = listOf(
                            stringResource(R.string.tag_philosophy),
                            stringResource(R.string.tag_readings)
                        ))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = note.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MansalvaFontFamily,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = note.content,
                            fontSize = 14.sp,
                            fontFamily = ManropeFontFamily,
                            color = TextPrimary.copy(alpha = 0.7f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 22.sp
                        )
                        NoteCardFooter(onContinueClick = { onEditClick(note.id) })
                    }
                }
            }
        }
    }
}

// ======= Empty State Card =======
@Composable
fun EmptyNoteCard(onAddNote: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // أيقونة فارغة
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFF5F0EB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.NoteAlt,
                    contentDescription = null,
                    tint = BrownCard.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = stringResource(R.string.empty_notes_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MansalvaFontFamily,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.empty_notes_subtitle),
                fontSize = 14.sp,
                fontFamily = ManropeFontFamily,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // زر إضافة ملاحظة
            androidx.compose.material3.Button(
                onClick = onAddNote,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = BrownCard
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.add_first_note),
                    fontFamily = ManropeFontFamily,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun NoteTagsRow(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Box(
                modifier = Modifier
                    .background(TagBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = tag, fontSize = 12.sp, color = TagText)
            }
        }
    }
}

@Composable
fun NoteCardFooter(onContinueClick: () -> Unit) {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = Color(0xFFF0EBE6))
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.clickable { onContinueClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                null,
                tint = BrownCard,
                modifier = Modifier.size(16.dp)
            )
            Text(
                stringResource(R.string.continue_writing),
                color = BrownCard,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            stringResource(R.string.edited_time_ago, "15"),
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

// ======= Wave Chart =======
@Composable
fun WaveChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clipToBounds()) {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(
                width * 0.15f,
                height * 0.8f,
                width * 0.25f,
                height * 0.5f,
                width * 0.4f,
                height * 0.55f
            )
            cubicTo(
                width * 0.55f,
                height * 0.6f,
                width * 0.65f,
                height * 0.3f,
                width * 0.8f,
                height * 0.35f
            )
            cubicTo(
                width * 0.9f, height * 0.38f, width * 0.95f, height * 0.45f, width, height * 0.4f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path = path, color = WaveColor, style = Fill)
    }
}

// ======= Bottom Navigation =======
@Composable
fun BottomNavBar(navController: NavHostController, selectedTab: Int) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        val tabs = listOf(
            Triple(
                stringResource(R.string.nav_settings), Icons.Outlined.Settings, Route.Settings.route
            ),
            Triple(
                stringResource(R.string.nav_tasks), Icons.Outlined.CheckCircle, Route.Tasks.route
            ),
            Triple(stringResource(R.string.nav_notes), Icons.Outlined.NoteAlt, Route.Notes.route),
            Triple(stringResource(R.string.nav_home), Icons.Filled.Home, Route.Home.route)
        )

        tabs.forEachIndexed { index, (label, icon, route) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = {
                    if (selectedTab != index) {
                        navController.navigate(route) {
                            popUpTo(Route.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(text = label, fontSize = 10.sp, fontFamily = ManropeFontFamily) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrownCard,
                    selectedTextColor = BrownCard,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun AICardSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrownCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.ai_suggestion),
                    fontSize = 12.sp,
                    fontFamily = ManropeFontFamily,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.ai_prompt_text),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MansalvaFontFamily,
                color = Color.White,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Button(
                onClick = { /* TODO: AI Action */ },
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(stringResource(R.string.start_summary), color = BrownCard)
            }
        }
    }
}

@Composable
fun UpcomingTaskSection(onViewAll: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.important_task), fontWeight = FontWeight.Bold)
                Icon(Icons.Outlined.CheckCircle, null, tint = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "مراجعة متطلبات المشروع", fontSize = 17.sp, fontFamily = ManropeFontFamily)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0EBE6))
            Text(
                text = stringResource(R.string.view_all_tasks),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewAll() }
                    .padding(top = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = BrownCard
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(52.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2EF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = BrownCard, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}