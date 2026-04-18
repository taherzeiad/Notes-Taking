package com.example.notes_taking.Screens.presentations.Tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.notes_taking.R
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.*

// ======= Data Models =======
data class Task(
    val id: Int,
    val titleRes: Int,
    val sourceRes: Int,
    val timeRes: Int,
    val isUrgent: Boolean = false,
    val status: TaskStatus = TaskStatus.IN_PROGRESS
)

enum class TaskStatus { IN_PROGRESS, COMPLETED, SCHEDULED }

data class SourceCategory(val nameRes: Int, val count: Int, val unitRes: Int)

// ======= Sample Data =======
val sampleTasks = listOf(
    Task(1, R.string.task_1_title, R.string.task_1_source, R.string.task_1_time, isUrgent = true),
    Task(2, R.string.task_2_title, R.string.task_2_source, R.string.task_2_time),
    Task(3, R.string.task_3_title, R.string.task_3_source, R.string.task_3_time),
    Task(4, R.string.task_4_title, R.string.task_4_source, R.string.task_empty_time),
    Task(5, R.string.task_5_title, R.string.task_5_source, R.string.task_empty_time)
)

@Composable
fun TasksScreen(navController: NavHostController) {
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val textAlign = if (isRtl) TextAlign.End else TextAlign.Start
    val horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
    val rowArrangementStart = if (isRtl) Arrangement.End else Arrangement.Start

    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        stringResource(R.string.tab_in_progress),
        stringResource(R.string.tab_completed),
        stringResource(R.string.tab_scheduled)
    )

    val categories = listOf(
        SourceCategory(R.string.cat_research, 12, R.string.tasks_unit),
        SourceCategory(R.string.cat_personal, 4, R.string.tasks_unit),
        SourceCategory(R.string.cat_meetings, 8, R.string.tasks_unit)
    )

    Scaffold(
        containerColor = PageBackground,
        bottomBar = {
            // تصحيح: تمرير الـ navController والتبويب الصحيح (index 1 للمهام)
            BottomNavBar(
                navController = navController,
                selectedTab = 1
            )
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
                    if (isRtl) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(26.dp))
                        Text(text = stringResource(R.string.app_name_styled), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = ManropeFontFamily, color = TextPrimary)
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(26.dp))
                    } else {
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(26.dp))
                        Text(text = stringResource(R.string.app_name_styled), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = ManropeFontFamily, color = TextPrimary)
                        Icon(imageVector = Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(26.dp))
                    }
                }
            }

            // ======= Page Title =======
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = horizontalAlignment
                ) {
                    Text(
                        text = stringResource(R.string.tasks_title),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MansalvaFontFamily,
                        color = TextPrimary,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.tasks_subtitle),
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextSecondary,
                        textAlign = textAlign,
                        lineHeight = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ======= Tabs =======
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = rowArrangementStart,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        TaskTab(
                            label = tab,
                            isSelected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                        if (index < tabs.size - 1) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            // ======= Tasks List (First 3) =======
            items(sampleTasks.take(3)) { task ->
                TaskCard(task = task, isRtl = isRtl)
            }

            // ======= AI Insights Card =======
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrownCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = rowArrangementStart,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRtl) {
                                Text(text = stringResource(R.string.ai_insights_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = MansalvaFontFamily, color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = stringResource(R.string.ai_insights_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = MansalvaFontFamily, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.ai_insights_body),
                            fontSize = 14.sp,
                            fontFamily = ManropeFontFamily,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = textAlign,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { 0.7f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ======= Source Categories =======
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = horizontalAlignment
                ) {
                    Text(
                        text = stringResource(R.string.source_categories),
                        fontSize = 15.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            categories.forEachIndexed { index, category ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isRtl) {
                                        Text(text = "${category.count} ${stringResource(category.unitRes)}", fontSize = 14.sp, fontFamily = ManropeFontFamily, color = BrownCard, fontWeight = FontWeight.Bold)
                                        Text(text = stringResource(category.nameRes), fontSize = 14.sp, fontFamily = ManropeFontFamily, color = TextPrimary)
                                    } else {
                                        Text(text = stringResource(category.nameRes), fontSize = 14.sp, fontFamily = ManropeFontFamily, color = TextPrimary)
                                        Text(text = "${category.count} ${stringResource(category.unitRes)}", fontSize = 14.sp, fontFamily = ManropeFontFamily, color = BrownCard, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (index < categories.size - 1) {
                                    HorizontalDivider(color = Color(0xFFF0EBE6))
                                }
                            }
                        }
                    }
                }
            }

            // ======= Remaining Tasks =======
            items(sampleTasks.drop(3)) { task ->
                TaskCard(task = task, isRtl = isRtl)
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ======= Helper UI Components =======

@Composable
fun TaskTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) BrownCard else Color(0xFFEDE8E3))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
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
fun TaskCard(task: Task, isRtl: Boolean) {
    val textAlign = if (isRtl) TextAlign.End else TextAlign.Start
    val contentAlignment = if (isRtl) Alignment.End else Alignment.Start
    val timeText = stringResource(task.timeRes)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox
                RadioButton(
                    selected = false,
                    onClick = {},
                    colors = RadioButtonDefaults.colors(unselectedColor = Color(0xFFD0C8C0))
                )

                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    horizontalAlignment = contentAlignment
                ) {
                    if (task.isUrgent) {
                        UrgentBadge()
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Text(
                        text = stringResource(task.titleRes),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary,
                        textAlign = textAlign
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRtl) {
                    if (timeText.isNotEmpty()) {
                        Text(text = timeText, fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
                    } else { Spacer(modifier = Modifier.width(1.dp)) }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = stringResource(task.sourceRes), fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
                        Icon(imageVector = Icons.Outlined.Description, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Outlined.Description, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Text(text = stringResource(task.sourceRes), fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
                    }
                    if (timeText.isNotEmpty()) {
                        Text(text = timeText, fontSize = 12.sp, fontFamily = ManropeFontFamily, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun UrgentBadge() {
    Box(
        modifier = Modifier
            .background(Color(0xFFFFEBEB), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(R.string.tag_urgent),
            fontSize = 11.sp,
            fontFamily = ManropeFontFamily,
            color = Color(0xFFD94F3D),
            fontWeight = FontWeight.SemiBold
        )
    }
}