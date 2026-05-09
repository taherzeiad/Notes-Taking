package com.example.notes_taking.Screens.presentations.Tasks

import TasksViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.notes_taking.R
import com.example.notes_taking.RoomDatabase.TaskEntity
import com.example.notes_taking.Screens.presentations.Home.BottomNavBar
import com.example.notes_taking.ui.theme.ManropeFontFamily
import com.example.notes_taking.ui.theme.MansalvaFontFamily

enum class TaskStatus { IN_PROGRESS, COMPLETED, SCHEDULED }

data class SourceCategory(val nameRes: Int, val count: Int, val unitRes: Int)

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    navController: NavHostController
) {
    // ← هذا هو المهم: جلب المهام من Room عبر TasksViewModel
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

    val tabs = listOf(
        stringResource(R.string.tab_in_progress),
        stringResource(R.string.tab_completed),
        stringResource(R.string.tab_scheduled)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController, selectedTab = 1) }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = stringResource(R.string.notes_screen_title_bar),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
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
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.tasks_subtitle),
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ======= 3. Tabs =======
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        TaskTab(
                            label = tab,
                            isSelected = viewModel.selectedTab == index,
                            onClick = { viewModel.onTabSelected(index) }
                        )
                    }
                }
            }

            // ======= 4. AI Insights Card =======
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.ai_insights_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MansalvaFontFamily,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.ai_insights_body),
                            fontSize = 14.sp,
                            fontFamily = ManropeFontFamily,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { viewModel.aiProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                        // ← عداد المهام
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${allTasks.count { it.isCompleted }}/${allTasks.size} مهمة مكتملة",
                            fontSize = 12.sp,
                            fontFamily = ManropeFontFamily,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // ======= 5. Tasks List من Room =======
            if (tasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (viewModel.selectedTab) {
                                0 -> "لا توجد مهام قيد التنفيذ"
                                1 -> "لا توجد مهام مكتملة"
                                else -> "لا توجد مهام"
                            },
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = ManropeFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "أضف ملاحظة تحتوي على مهام لتظهر هنا",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // ← items تأخذ TaskEntity من Room
                items(
                    items = tasks,
                    key = { it.id }
                ) { task ->
                    RoomTaskCard(
                        task = task,
                        onCheck = { viewModel.toggleTaskCompletion(task.id) }
                    )
                }
            }

            // ======= 6. Source Categories ديناميكية من Room =======
            if (allTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.source_categories),
                        fontSize = 15.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // ← تجميع المهام حسب المصدر (اسم الملاحظة)
                    val sourceGroups = allTasks
                        .groupBy { it.source.ifBlank { "غير محدد" } }
                        .entries
                        .toList()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            sourceGroups.forEachIndexed { index, (source, sourceTasks) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = source,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = ManropeFontFamily,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${sourceTasks.size} مهام",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = ManropeFontFamily
                                    )
                                }
                                if (index < sourceGroups.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ← بطاقة المهمة من Room
@Composable
fun RoomTaskCard(task: TaskEntity, onCheck: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheck() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                        text = task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                        color = if (task.isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                RadioButton(
                    selected = task.isCompleted,
                    onClick = onCheck,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            if (task.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Description,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = task.source,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = ManropeFontFamily
                    )
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
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondaryContainer
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun UrgentBadge() {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(R.string.tag_urgent),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
            fontFamily = ManropeFontFamily
        )
    }
}