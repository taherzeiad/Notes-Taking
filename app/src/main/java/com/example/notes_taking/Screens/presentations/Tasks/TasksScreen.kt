package com.example.notes_taking.Screens.presentations.Tasks

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.notes_taking.R
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.BrownCard
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily
import com.example.notes_taking.ui.theme.PageBackground
import com.example.notes_taking.ui.theme.TextPrimary
import com.example.notes_taking.ui.theme.TextSecondary

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

// البيانات التجريبية يجب أن تكون خارج أي دالة لكي يراها الـ ViewModel
val sampleTasks = listOf(
    Task(1, R.string.task_1_title, R.string.task_1_source, R.string.task_1_time, isUrgent = true),
    Task(2, R.string.task_2_title, R.string.task_2_source, R.string.task_2_time),
    Task(3, R.string.task_3_title, R.string.task_3_source, R.string.task_3_time),
    Task(4, R.string.task_4_title, R.string.task_4_source, R.string.task_empty_time),
    Task(5, R.string.task_5_title, R.string.task_5_source, R.string.task_empty_time)
)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    navController: NavHostController
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

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
            BottomNavBar(navController = navController, selectedTab = 1)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ======= 1. Top Bar =======
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp).clickable { /* TODO: Search */ }
                    )
                    Text(
                        text = stringResource(R.string.app_name_styled),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = TextPrimary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // ======= 2. Page Title =======
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.tasks_title),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MansalvaFontFamily,
                        color = TextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.tasks_subtitle),
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        color = TextSecondary,
                        lineHeight = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ======= 3. Tabs =======
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        TaskTab(
                            label = tab,
                            isSelected = viewModel.selectedTab == index,
                            onClick = { viewModel.onTabSelected(index) }
                        )
                        if (index < tabs.size - 1) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            // ======= 4. AI Insights Card =======
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrownCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                text = stringResource(R.string.ai_insights_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MansalvaFontFamily,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.ai_insights_body),
                            fontSize = 14.sp,
                            fontFamily = ManropeFontFamily,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { viewModel.aiProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ======= 5. Dynamic Tasks List =======
            if (tasks.isEmpty()) {
                item {
                    Text(
                        text = "لا توجد مهام حالياً",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        textAlign = TextAlign.Center,
                        color = TextSecondary
                    )
                }
            } else {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onCheck = { viewModel.toggleTaskCompletion(task.id) }
                    )
                }
            }

            // ======= 6. Source Categories =======
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.source_categories),
                    fontSize = 15.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
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
                                Text(stringResource(category.nameRes), fontSize = 14.sp, color = TextPrimary)
                                Text(
                                    text = "${category.count} ${stringResource(category.unitRes)}",
                                    fontSize = 14.sp,
                                    color = BrownCard,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (index < categories.size - 1) HorizontalDivider(color = Color(0xFFF0EBE6))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ======= Helper UI Components =======

@Composable
fun TaskCard(task: com.example.notes_taking.Screens.presentations.Tasks.Task, onCheck: () -> Unit) {
    val isCompleted = task.status == TaskStatus.COMPLETED

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onCheck() },
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
                Column(modifier = Modifier.weight(1f)) {
                    if (task.isUrgent) {
                        UrgentBadge()
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Text(
                        text = stringResource(task.titleRes),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = if (isCompleted) TextSecondary else TextPrimary
                    )
                }
                RadioButton(
                    selected = isCompleted,
                    onClick = onCheck,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = BrownCard,
                        unselectedColor = Color(0xFFD0C8C0)
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Description, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text(stringResource(task.sourceRes), fontSize = 12.sp, color = TextSecondary)
                }
                val time = stringResource(task.timeRes)
                if (time.isNotEmpty()) {
                    Text(time, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

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
fun UrgentBadge() {
    Box(
        modifier = Modifier
            .background(Color(0xFFFFEBEB), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(R.string.tag_urgent),
            fontSize = 11.sp,
            color = Color(0xFFD94F3D),
            fontWeight = FontWeight.SemiBold
        )
    }
}