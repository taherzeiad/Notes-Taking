package com.example.notes_taking.Screens.presentations.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.notes_taking.Navmain.Route
import com.example.notes_taking.R
import com.example.notes_taking.RoomDatabase.Note
import com.example.notes_taking.Screens.presentations.AppTopBar
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel, navController: NavHostController, onAddNote: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController, selectedTab = 3) }) { padding ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item(key = "topbar") { HomeTopBarSection() }
                item(key = "welcome") { WelcomeSection() }
                item(key = "ai_card") { AICardSection(navController = navController) }
                item(key = "last_note") {
                    LastEditedNoteSection(
                        note = uiState.lastEditedNote,
                        onEditClick = { id -> navController.navigate(Route.NoteEditor.createRoute(id)) },
                        onAddNote = onAddNote
                    )
                }
                item(key = "quick_actions") {
                    QuickActionsSection(onAddNote = onAddNote, onVoiceRecord = {
                        navController.navigate(
                            Route.NoteEditor.createRoute(
                                0, true
                            )
                        )
                    }, onAddImage = {
                        navController.navigate(
                            Route.NoteEditor.createRoute(
                                0, false, true
                            )
                        )
                    })
                }
                item(key = "spacer") { Spacer(modifier = Modifier.height(10.dp)) }
            }
        }
    }
}

@Composable
fun WelcomeSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.welcome_user),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.welcome_subtitle),
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActionsSection(
    onAddNote: () -> Unit, onVoiceRecord: () -> Unit, onAddImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton(
                label = stringResource(R.string.voice_record),
                icon = Icons.Outlined.Mic,
                modifier = Modifier.weight(1f),
                onClick = onVoiceRecord
            )
            QuickActionButton(
                label = stringResource(R.string.quick_idea),
                icon = Icons.Outlined.Lightbulb,
                modifier = Modifier.weight(1f),
                onClick = onAddNote
            )
        }
        QuickActionButton(
            label = stringResource(R.string.add_image),
            icon = Icons.Outlined.Image,
            modifier = Modifier.fillMaxWidth(),
            onClick = onAddImage
        )
    }
}

@Composable
fun LastEditedNoteSection(
    note: Note?, onEditClick: (Int) -> Unit, onAddNote: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.last_edited_note),
            fontSize = 15.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (note == null) {
            EmptyNoteCard(onAddNote = onAddNote)
        } else {
            val onCardClick = remember(note.id) { { onEditClick(note.id) } }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCardClick),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    WaveChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        // عرض وسوم الملاحظة بطريقة تعتمد على التصنيف المخزن في الـ Note
                        val currentCategoryTag = when (note.category) {
                            "Philosophy" -> stringResource(R.string.tag_philosophy)
                            else -> note.category.ifBlank { stringResource(R.string.note_cat_all) }
                        }
                        NoteTagsRow(
                            tags = listOf(
                                currentCategoryTag, stringResource(R.string.tag_readings)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = note.title.ifBlank { stringResource(R.string.editor_title_hint) },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MansalvaFontFamily,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (note.content.isNotBlank()) {
                            Text(
                                text = note.content,
                                fontSize = 14.sp,
                                fontFamily = ManropeFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 22.sp
                            )
                        }
                        NoteCardFooter(note = note, onContinueClick = onCardClick)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNoteCard(onAddNote: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.NoteAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = stringResource(R.string.empty_notes_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MansalvaFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.empty_notes_subtitle),
                fontSize = 14.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onAddNote,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.add_first_note), fontFamily = ManropeFontFamily)
            }
        }
    }
}

@Composable
fun HomeTopBarSection() {
    AppTopBar(title = stringResource(R.string.app_name_styled))
}

@Composable
fun NoteTagsRow(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tag,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontFamily = ManropeFontFamily
                )
            }
        }
    }
}

@Composable
fun NoteCardFooter(note: Note, onContinueClick: () -> Unit) {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
            Text(
                text = stringResource(R.string.continue_writing),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontFamily = ManropeFontFamily
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        // هنا يمكنك لاحقاً حساب الفارق الزمني الحقيقي بدلاً من تثبيت القيمة "15"
        val formattedTime = remember(note.date) { "15" }
        Text(
            text = stringResource(R.string.edited_time_ago, formattedTime),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = ManropeFontFamily
        )
    }
}

@Composable
fun WaveChart(modifier: Modifier = Modifier) {
    val waveColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)

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
        drawPath(path = path, color = waveColor, style = Fill)
    }
}

@Composable
fun BottomNavBar(navController: NavHostController, selectedTab: Int) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp
    ) {
        val tabs = remember {
            listOf(
                Triple("Settings", Icons.Outlined.Settings, Route.Settings.route),
                Triple("Tasks", Icons.Outlined.CheckCircle, Route.Tasks.route),
                Triple("Notes", Icons.Outlined.NoteAlt, Route.Notes.route),
                Triple("Home", Icons.Filled.Home, Route.Home.route)
            )
        }

        val labels = listOf(
            stringResource(R.string.nav_settings),
            stringResource(R.string.nav_tasks),
            stringResource(R.string.nav_notes),
            stringResource(R.string.nav_home)
        )

        tabs.forEachIndexed { index, (_, icon, route) ->
            NavigationBarItem(
                selected = selectedTab == index, onClick = {
                if (selectedTab != index) {
                    navController.navigate(route) {
                        popUpTo(Route.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }, icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = labels[index],
                    modifier = Modifier.size(24.dp)
                )
            }, label = {
                Text(
                    text = labels[index],
                    fontSize = 10.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                )
            }, colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
            )
        }
    }
}

@Composable
fun AICardSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.ai_suggestion),
                    fontSize = 12.sp,
                    fontFamily = ManropeFontFamily,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.ai_prompt_text),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MansalvaFontFamily,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate(Route.Summary.route) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = stringResource(R.string.start_summary), fontFamily = ManropeFontFamily)
            }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = ManropeFontFamily
            )
        }
    }
}