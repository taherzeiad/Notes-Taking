package com.example.myuniqueapp.Screens.presentations.Tasks

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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
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
import com.example.myuniqueapp.RoomDatabase.TaskEntity
import com.example.myuniqueapp.Screens.presentations.AppTopBar
import com.example.myuniqueapp.Screens.presentations.Home.BottomNavBar
import com.example.myuniqueapp.ui.theme.ManropeFontFamily
import com.example.myuniqueapp.ui.theme.MansalvaFontFamily
import com.notestalking.myuniqueapp.R


@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    navController: NavHostController,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TasksContent(
        state = state,
        onIntent = { intent -> handleIntent(intent, viewModel) },
        navController = navController,
    )
}

private fun handleIntent(intent: TasksIntent, viewModel: TasksViewModel) {
    when (intent) {
        is TasksIntent.SelectTab -> viewModel.onTabSelected(intent.index)
        is TasksIntent.SearchQueryChanged -> viewModel.onSearchQueryChange(intent.query)
        is TasksIntent.OpenSearch -> viewModel.openSearch()
        is TasksIntent.CloseSearch -> viewModel.closeSearch()
        is TasksIntent.ToggleTask -> viewModel.toggleTaskCompletion(intent.taskId)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Intents (UI Events)
// ─────────────────────────────────────────────────────────────────────────────

sealed interface TasksIntent {
    data class SelectTab(val index: Int) : TasksIntent
    data class SearchQueryChanged(val query: String) : TasksIntent
    data object OpenSearch : TasksIntent
    data object CloseSearch : TasksIntent
    data class ToggleTask(val taskId: Int) : TasksIntent
}

// ─────────────────────────────────────────────────────────────────────────────
// Stateless Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TasksContent(
    state: TasksUiState,
    onIntent: (TasksIntent) -> Unit,
    navController: NavHostController,
) {
    val tabs = listOf(
        stringResource(R.string.tab_in_progress),
        stringResource(R.string.tab_completed),
        stringResource(R.string.tab_scheduled),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController, selectedTab = 1) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1 ── Top Bar / Search Bar
            item {
                if (state.isSearchActive) {
                    TasksSearchBar(
                        query = state.searchQuery,
                        onChange = { onIntent(TasksIntent.SearchQueryChanged(it)) },
                        onClose = { onIntent(TasksIntent.CloseSearch) },
                    )
                } else {
                    AppTopBar(
                        title = stringResource(R.string.notes_screen_title_bar),
                        onSearchClick = { onIntent(TasksIntent.OpenSearch) },
                    )
                }
            }

            // 2 ── Page Title
            if (!state.isSearchActive) {
                item { TasksHeader() }
            }

            // 3 ── Tabs
            item {
                TasksTabRow(
                    tabs = tabs,
                    selectedTab = state.selectedTab,
                    onTabClick = { onIntent(TasksIntent.SelectTab(it)) },
                )
            }

            // 4 ── AI Insights Card
            if (!state.isSearchActive && state.totalCount > 0) {
                item {
                    AiInsightsCard(
                        progress = state.aiProgress,
                        completedCount = state.completedCount,
                        totalCount = state.totalCount,
                    )
                }
            }

            // 5 ── Tasks List
            if (state.filteredTasks.isEmpty()) {
                item {
                    EmptyTasksMessage(
                        isSearching = state.searchQuery.isNotBlank(),
                        selectedTab = state.selectedTab,
                    )
                }
            } else {
                items(items = state.filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onCheck = { onIntent(TasksIntent.ToggleTask(task.id)) },
                    )
                }
            }

            // 6 ── Source Categories
            if (state.allTasks.isNotEmpty() && !state.isSearchActive) {
                item {
                    SourceCategoriesSection(groups = state.sourceGroups)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small Composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TasksSearchBar(
    query: String,
    onChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        placeholder = {
            Text(
                text = stringResource(R.string.search_tasks),
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        singleLine = true,
    )
}

@Composable
private fun TasksHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.tasks_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MansalvaFontFamily,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.tasks_subtitle),
            fontSize = 13.sp,
            fontFamily = ManropeFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun TasksTabRow(
    tabs: List<String>,
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, label ->
            TaskTab(
                label = label,
                isSelected = selectedTab == index,
                onClick = { onTabClick(index) },
            )
        }
    }
}

@Composable
private fun AiInsightsCard(
    progress: Float,
    completedCount: Int,
    totalCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.ai_insights_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MansalvaFontFamily,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.ai_insights_body),
                fontSize = 14.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                lineHeight = 22.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tasks_completed_count, completedCount, totalCount),
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun EmptyTasksMessage(
    isSearching: Boolean,
    selectedTab: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (isSearching) {
                stringResource(R.string.no_search_results)
            } else {
                when (selectedTab) {
                    0 -> stringResource(R.string.no_tasks_in_progress)
                    1 -> stringResource(R.string.no_tasks_completed)
                    else -> stringResource(R.string.no_tasks)
                }
            },
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = ManropeFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        if (!isSearching) {
            Text(
                text = stringResource(R.string.add_note_with_tasks),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontFamily = ManropeFontFamily,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun SourceCategoriesSection(groups: List<SourceGroup>) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.source_categories),
            fontSize = 15.sp,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                groups.forEachIndexed { index, group ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = group.source,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = ManropeFontFamily,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(R.string.tasks_count_label, group.tasks.size),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ManropeFontFamily,
                        )
                    }
                    if (index < groups.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TaskCard(task: TaskEntity, onCheck: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheck() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
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
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
                RadioButton(
                    selected = task.isCompleted,
                    onClick = onCheck,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }

            if (task.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = task.source,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = ManropeFontFamily,
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
                else MaterialTheme.colorScheme.surface,
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = ManropeFontFamily,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onBackground,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun UrgentBadge() {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = stringResource(R.string.tag_urgent),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
            fontFamily = ManropeFontFamily,
        )
    }
}