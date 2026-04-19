package com.example.notes_taking.Screens.presentations.Home

import com.example.notes_taking.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.notes_taking.Screens.presentations.CreateNote.NoteViewModel
import com.example.notes_taking.ui.theme.*

@Composable
fun HomeScreen(
    onAddNote: () -> Unit,
    onEditNote: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTasks: () -> Unit,
    viewModel: NoteViewModel,
    navController: NavHostController
) {
    val notes by viewModel.allNotes.collectAsState(initial = emptyList())
    val lastNote = notes.firstOrNull()

    // ← اتجاه اللغة
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val textAlign = if (isRtl) TextAlign.End else TextAlign.Start
    val horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
    val rowArrangementStart = if (isRtl) Arrangement.End else Arrangement.Start
    val rowArrangementEnd = if (isRtl) Arrangement.Start else Arrangement.End

    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            BottomNavBar(navController = navController, selectedTab = 3)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ======= Top Bar =======
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRtl) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(26.dp))
                        Text(text = stringResource(R.string.app_name_styled), fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = ManropeFontFamily, color = TextPrimary)
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BrownCard), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BrownCard), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Text(text = stringResource(R.string.app_name_styled), fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = ManropeFontFamily, color = TextPrimary)
                        Icon(imageVector = Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(26.dp))
                    }
                }
            }

            // ======= Welcome =======
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = horizontalAlignment
                ) {
                    Text(
                        text = stringResource(R.string.welcome_user, "Sara"),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MansalvaFontFamily,
                        color = TextPrimary,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.welcome_subtitle),
                        fontSize = 14.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextSecondary,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ======= AI Card =======
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrownCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // AI Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = rowArrangementEnd,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRtl) {
                                Text(text = stringResource(R.string.ai_suggestion), fontSize = 12.sp, fontFamily = ManropeFontFamily, color = Color.White.copy(alpha = 0.7f))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            } else {
                                Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = stringResource(R.string.ai_suggestion), fontSize = 12.sp, fontFamily = ManropeFontFamily, color = Color.White.copy(alpha = 0.7f))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.ai_prompt_text),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MansalvaFontFamily,
                            color = Color.White,
                            textAlign = textAlign,
                            lineHeight = 26.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { },
                            modifier = Modifier.align(if (isRtl) Alignment.End else Alignment.Start),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text(text = stringResource(R.string.start_summary), fontSize = 14.sp, fontFamily = ManropeFontFamily, color = BrownCard, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ======= Upcoming Task =======
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRtl) {
                                Text(text = stringResource(R.string.important_task), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = ManropeFontFamily, color = TextPrimary)
                                Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                Text(text = stringResource(R.string.important_task), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = ManropeFontFamily, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Time Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = rowArrangementEnd
                        ) {
                            Box(
                                modifier = Modifier.background(TagBg, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(text = stringResource(R.string.task_due_time), fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TagText)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.task_book_review),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ManropeFontFamily,
                            color = TextPrimary,
                            textAlign = textAlign,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Location
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = rowArrangementEnd,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRtl) {
                                Text(text = stringResource(R.string.location_library), fontSize = 13.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            } else {
                                Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = stringResource(R.string.location_library), fontSize = 13.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF0EBE6))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.view_all_tasks),
                            fontSize = 14.sp,
                            fontFamily = ManropeFontFamily,
                            color = BrownCard,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToTasks() }
                        )
                    }
                }
            }

            // ======= Last Edited Note =======
            item {
                Column {
                    Text(
                        text = stringResource(R.string.last_edited_note),
                        fontSize = 15.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextSecondary,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { lastNote?.let { onEditNote(it.id) } },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column {
                            WaveChart(modifier = Modifier.fillMaxWidth().height(100.dp))

                            Column(modifier = Modifier.padding(16.dp)) {
                                // Tags
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, if (isRtl) Alignment.End else Alignment.Start)
                                ) {
                                    listOf(
                                        stringResource(R.string.tag_philosophy),
                                        stringResource(R.string.tag_readings)
                                    ).forEach { tag ->
                                        Box(
                                            modifier = Modifier.background(TagBg, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = tag, fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TagText)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = lastNote?.title ?: stringResource(R.string.default_note_title),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MansalvaFontFamily,
                                    color = TextPrimary,
                                    textAlign = textAlign,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = lastNote?.content ?: stringResource(R.string.default_note_content),
                                    fontSize = 14.sp,
                                    fontFamily = ManropeFontFamily,
                                    color = TextPrimary.copy(alpha = 0.7f),
                                    textAlign = textAlign,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 22.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF0EBE6))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Footer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isRtl) {
                                        Text(text = stringResource(R.string.edited_time_ago, "١٥"), fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.clickable { lastNote?.let { onEditNote(it.id) } }
                                        ) {
                                            Text(text = stringResource(R.string.continue_writing), fontSize = 14.sp, fontFamily = ManropeFontFamily, color = BrownCard, fontWeight = FontWeight.Medium)
                                            Text(text = "←", fontSize = 14.sp, color = BrownCard)
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.clickable { lastNote?.let { onEditNote(it.id) } }
                                        ) {
                                            Text(text = "→", fontSize = 14.sp, color = BrownCard)
                                            Text(text = stringResource(R.string.continue_writing), fontSize = 14.sp, fontFamily = ManropeFontFamily, color = BrownCard, fontWeight = FontWeight.Medium)
                                        }
                                        Text(text = stringResource(R.string.edited_time_ago, "15"), fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ======= Quick Actions =======
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = horizontalAlignment,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            label = stringResource(R.string.voice_record),
                            icon = Icons.Outlined.Mic,
                            isRtl = isRtl,
                            modifier = Modifier.weight(1f),
                            onClick = { }
                        )
                        QuickActionButton(
                            label = stringResource(R.string.quick_idea),
                            icon = Icons.Outlined.Lightbulb,
                            isRtl = isRtl,
                            modifier = Modifier.weight(1f),
                            onClick = onAddNote
                        )
                    }
                    QuickActionButton(
                        label = stringResource(R.string.add_document),
                        icon = Icons.Outlined.Image,
                        isRtl = isRtl,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ======= Wave Chart =======
@Composable
fun WaveChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(width * 0.15f, height * 0.8f, width * 0.25f, height * 0.5f, width * 0.4f, height * 0.55f)
            cubicTo(width * 0.55f, height * 0.6f, width * 0.65f, height * 0.3f, width * 0.8f, height * 0.35f)
            cubicTo(width * 0.9f, height * 0.38f, width * 0.95f, height * 0.45f, width, height * 0.4f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path = path, color = WaveColor, style = Fill)
    }
}

// ======= Quick Action Button =======
@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isRtl: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(52.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QuickBtnBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isRtl) Arrangement.End else Arrangement.Start
        ) {
            if (isRtl) {
                Text(text = label, fontSize = 14.sp, fontFamily = ManropeFontFamily, color = TextPrimary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = icon, contentDescription = null, tint = BrownCard, modifier = Modifier.size(20.dp))
            } else {
                Icon(imageVector = icon, contentDescription = null, tint = BrownCard, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, fontSize = 14.sp, fontFamily = ManropeFontFamily, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ======= Bottom Navigation =======
@Composable
fun BottomNavBar(navController: NavHostController, selectedTab: Int) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        val tabs = listOf(
            Triple(stringResource(R.string.nav_settings), Icons.Outlined.Settings, "settings_screen"),
            Triple(stringResource(R.string.nav_tasks), Icons.Outlined.CheckCircle, "tasks_screen"),
            Triple(stringResource(R.string.nav_notes), Icons.Outlined.NoteAlt, "home_screen"),
            Triple(stringResource(R.string.nav_home), Icons.Filled.Home, "home_screen")
        )

        tabs.forEachIndexed { index, (label, icon, route) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = {
                    if (selectedTab != index) {
                        navController.navigate(route) {
                            popUpTo("home_screen") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(24.dp)) },
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