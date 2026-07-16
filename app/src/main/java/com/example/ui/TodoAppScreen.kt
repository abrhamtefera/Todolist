package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.Task
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodoAppScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasksState.collectAsStateWithLifecycle()
    val stats by viewModel.statsState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("tasks") } // "tasks", "calendar", "settings"
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var showSearchRow by remember { mutableStateOf(false) }

    val currentDateStr = remember {
        val formatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        formatter.format(Date())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Bento Style Navigation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BentoNavItem(
                        icon = Icons.AutoMirrored.Rounded.ListAlt,
                        label = "Tasks",
                        isActive = activeTab == "tasks",
                        onClick = { activeTab = "tasks" },
                        testTag = "nav_tab_tasks"
                    )
                    BentoNavItem(
                        icon = Icons.Rounded.CalendarToday,
                        label = "Calendar",
                        isActive = activeTab == "calendar",
                        onClick = { activeTab = "calendar" },
                        testTag = "nav_tab_calendar"
                    )
                    BentoNavItem(
                        icon = Icons.Rounded.Settings,
                        label = "Settings",
                        isActive = activeTab == "settings",
                        onClick = { activeTab = "settings" },
                        testTag = "nav_tab_settings"
                    )
                }
            }
        },
        floatingActionButton = {
            if (activeTab == "tasks") {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("add_task_fab")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Task",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .statusBarsPadding()
        ) {
            // Header Area
            HeaderBar(
                dateStr = currentDateStr,
                showSearch = showSearchRow,
                onSearchToggle = { showSearchRow = !showSearchRow },
                searchQuery = searchQuery,
                onSearchChange = { viewModel.searchQuery.value = it }
            )

            // Dynamic Tab Switch
            Box(
                modifier = Modifier
                    .fillWeightAndWidth()
                    .padding(horizontal = 24.dp)
            ) {
                when (activeTab) {
                    "tasks" -> {
                        TasksTabContent(
                            tasks = tasks,
                            stats = stats,
                            selectedFilter = selectedFilter,
                            selectedSort = selectedSort,
                            onFilterSelect = { viewModel.selectedFilter.value = it },
                            onSortSelect = { viewModel.selectedSort.value = it },
                            onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { taskToEdit = it },
                            onDeleteTask = { taskToDelete = it }
                        )
                    }
                    "calendar" -> {
                        CalendarTabContent(
                            tasks = tasks,
                            onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { taskToEdit = it },
                            onDeleteTask = { taskToDelete = it }
                        )
                    }
                    "settings" -> {
                        SettingsTabContent(
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            isNotificationsEnabled = isNotificationsEnabled,
                            onToggleNotifications = { viewModel.toggleNotifications() },
                            onClearCompleted = { viewModel.clearCompletedTasks() }
                        )
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        AddEditTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, notes, priority, dueDate, reminderTime ->
                viewModel.addTask(title, notes, priority, dueDate, reminderTime)
                showAddDialog = false
            }
        )
    }

    // Edit Task Dialog
    taskToEdit?.let { task ->
        AddEditTaskDialog(
            task = task,
            onDismiss = { taskToEdit = null },
            onConfirm = { title, notes, priority, dueDate, reminderTime ->
                viewModel.updateTask(
                    task.copy(
                        title = title,
                        notes = notes,
                        priority = priority,
                        dueDate = dueDate,
                        reminderTime = reminderTime,
                        isReminderActive = reminderTime != null && isNotificationsEnabled
                    )
                )
                taskToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    taskToDelete?.let { task ->
        DeleteConfirmationDialog(
            taskTitle = task.title,
            onDismiss = { taskToDelete = null },
            onConfirm = {
                viewModel.deleteTask(task)
                taskToDelete = null
            }
        )
    }
}

@Composable
fun Modifier.fillWeightAndWidth(): Modifier = this.fillMaxWidth().fillMaxHeight()

@Composable
fun HeaderBar(
    dateStr: String,
    showSearch: Boolean,
    onSearchToggle: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "My Day",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search Toggle Button
                IconButton(
                    onClick = onSearchToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("search_toggle_button")
                ) {
                    Icon(
                        imageVector = if (showSearch) Icons.Rounded.Close else Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // JD User Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showSearch,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("search_text_input"),
                placeholder = { Text("Search tasks...") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun BentoNavItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = MaterialTheme.colorScheme.onPrimaryContainer
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) activeColor else inactiveColor
            )
        )
    }
}

@Composable
fun TasksTabContent(
    tasks: List<Task>,
    stats: TaskStats,
    selectedFilter: FilterOption,
    selectedSort: SortOption,
    onFilterSelect: (FilterOption) -> Unit,
    onSortSelect: (SortOption) -> Unit,
    onToggleComplete: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    val isDark = !MaterialTheme.colorScheme.primary.let {
        MaterialTheme.colorScheme.background == BentoBackgroundLight
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Dashboard Bento Grid
        BentoDashboardGrid(stats = stats, isDark = isDark)

        Spacer(modifier = Modifier.height(16.dp))

        // Filters Chip Row & Sort Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(FilterOption.values()) { option ->
                    val isSelected = option == selectedFilter
                    FilterChip(
                        label = when (option) {
                            FilterOption.ALL -> "All"
                            FilterOption.ACTIVE -> "Active"
                            FilterOption.COMPLETED -> "Completed"
                        },
                        isSelected = isSelected,
                        onClick = { onFilterSelect(option) },
                        testTag = "filter_chip_${option.name.lowercase()}"
                    )
                }
            }

            // Sort Selector
            SortSelectorButton(
                selectedSort = selectedSort,
                onSortSelect = onSortSelect
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task List
        if (tasks.isEmpty()) {
            EmptyTasksState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("tasks_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        onToggleComplete = { onToggleComplete(task) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun BentoDashboardGrid(stats: TaskStats, isDark: Boolean) {
    val progressBg = if (isDark) BentoWeeklyProgressBgDark else BentoWeeklyProgressBgLight
    val progressText = if (isDark) BentoWeeklyProgressTextDark else BentoWeeklyProgressTextLight
    val completedBg = if (isDark) BentoCompletedBgDark else BentoCompletedBgLight
    val pendingBg = if (isDark) BentoPendingBgDark else BentoPendingBgLight
    val pendingBorder = if (isDark) BentoPendingBorderDark else BentoPendingBorderLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bento_dashboard")
    ) {
        // Progress Card (Spans Full Width / Top row of bento)
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = progressBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Weekly Progress",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = progressText,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${stats.progressPercent}%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = progressText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp
                        )
                    )
                    Text(
                        text = "${stats.completed}/${stats.total} Tasks",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = progressText.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { stats.progressPercent.toFloat() / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = progressText,
                    trackColor = progressText.copy(alpha = 0.15f)
                )
            }
        }

        // Bottom row of bento (Completed and Pending cards side by side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Completed Count Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = completedBg),
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TaskAlt,
                        contentDescription = null,
                        tint = progressText,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = String.format("%02d", stats.completed),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = progressText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        )
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Pending Count Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .background(pendingBg, RoundedCornerShape(28.dp))
                    .border(1.dp, pendingBorder, RoundedCornerShape(28.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PendingActions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = String.format("%02d", stats.pending),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        )
                        Text(
                            text = "Pending",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(40.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun SortSelectorButton(
    selectedSort: SortOption,
    onSortSelect: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .testTag("sort_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = "Sort Tasks",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Creation Date") },
                onClick = {
                    onSortSelect(SortOption.CREATION_DATE)
                    expanded = false
                },
                leadingIcon = {
                    if (selectedSort == SortOption.CREATION_DATE) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                    }
                },
                modifier = Modifier.testTag("sort_creation_date")
            )
            DropdownMenuItem(
                text = { Text("Due Date") },
                onClick = {
                    onSortSelect(SortOption.DUE_DATE)
                    expanded = false
                },
                leadingIcon = {
                    if (selectedSort == SortOption.DUE_DATE) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                    }
                },
                modifier = Modifier.testTag("sort_due_date")
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItemRow(
    task: Task,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority) {
        "HIGH" -> MaterialTheme.colorScheme.error
        "MEDIUM" -> Color(0xFFFF9800) // Amber
        else -> Color(0xFF4CAF50) // Green
    }

    val context = LocalContext.current
    val dueDateStr = task.dueDate?.let {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(it))
    }
    val reminderStr = task.reminderTime?.let {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(it))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onDelete
            )
            .testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                    .border(
                        2.dp,
                        if (task.isCompleted) MaterialTheme.colorScheme.primary else priorityColor,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable(onClick = onToggleComplete)
                    .testTag("task_complete_check_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Task info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f
                        ) else MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Sub-details (Priority, Due Date, Reminders)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Priority tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(priorityColor, CircleShape)
                        )
                        Text(
                            text = when (task.priority) {
                                "HIGH" -> "High"
                                "MEDIUM" -> "Medium"
                                else -> "Low"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = priorityColor
                            )
                        )
                    }

                    // Due Date Tag
                    dueDateStr?.let { date ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Reminder Tag
                    reminderStr?.let { time ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isReminderActive) Icons.Rounded.NotificationsActive else Icons.Rounded.Notifications,
                                contentDescription = null,
                                tint = if (task.isReminderActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = time,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (task.isReminderActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Description note icon if present
            if (task.notes.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = "Has notes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyTasksState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Inbox,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Tasks Found",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the '+' button to schedule your first modern task.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun CalendarTabContent(
    tasks: List<Task>,
    onToggleComplete: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayEnd = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    val todayTasks = tasks.filter { it.dueDate in todayStart..todayEnd }
    val upcomingTasks = tasks.filter { it.dueDate != null && it.dueDate > todayEnd }
    val unscheduledTasks = tasks.filter { it.dueDate == null }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Today section
        if (todayTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Today")
            }
            items(todayTasks) { task ->
                TaskItemRow(
                    task = task,
                    onToggleComplete = { onToggleComplete(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) }
                )
            }
        }

        // Upcoming section
        if (upcomingTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Upcoming")
            }
            items(upcomingTasks) { task ->
                TaskItemRow(
                    task = task,
                    onToggleComplete = { onToggleComplete(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) }
                )
            }
        }

        // Unscheduled section
        if (unscheduledTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Unscheduled")
            }
            items(unscheduledTasks) { task ->
                TaskItemRow(
                    task = task,
                    onToggleComplete = { onToggleComplete(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) }
                )
            }
        }

        if (todayTasks.isEmpty() && upcomingTasks.isEmpty() && unscheduledTasks.isEmpty()) {
            item {
                EmptyTasksState()
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingsTabContent(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    isNotificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit,
    onClearCompleted: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_tab_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "App Settings",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        // Dark Mode Card
        SettingToggleCard(
            title = "Dark Theme",
            description = "Toggle light and dark color modes",
            icon = Icons.Rounded.DarkMode,
            checked = isDarkMode,
            onCheckedChange = { onToggleDarkMode() },
            testTag = "dark_mode_toggle"
        )

        // Notifications Card
        SettingToggleCard(
            title = "Task Notifications",
            description = "Get notified when a task is due",
            icon = Icons.Rounded.Notifications,
            checked = isNotificationsEnabled,
            onCheckedChange = { onToggleNotifications() },
            testTag = "notifications_toggle"
        )

        // Danger Zone Clear All Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(20.dp)
                )
                .clickable { showClearConfirm = true }
                .testTag("clear_completed_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Column {
                        Text(
                            text = "Clear Completed Tasks",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                        Text(
                            text = "Remove all archived tasks permanently",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Completed Tasks") },
            text = { Text("Are you sure you want to permanently delete all completed tasks? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearCompleted()
                        showClearConfirm = false
                    },
                    modifier = Modifier.testTag("confirm_clear_completed")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
fun AddEditTaskDialog(
    task: Task? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long?, Long?) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: "MEDIUM") }
    var dueDate by remember { mutableStateOf(task?.dueDate) }
    var reminderTime by remember { mutableStateOf(task?.reminderTime) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val dueDateStr = dueDate?.let {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
    } ?: "Set Due Date"

    val reminderTimeStr = reminderTime?.let {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it))
    } ?: "Set Reminder"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .testTag("add_edit_task_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (task == null) "New Task" else "Edit Task",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("What needs to be done?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Description") },
                    placeholder = { Text("Add more details here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("task_notes_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Priority Selection
                Column {
                    Text(
                        text = "Priority Level",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { level ->
                            val isSelected = priority == level
                            val color = when (level) {
                                "HIGH" -> MaterialTheme.colorScheme.error
                                "MEDIUM" -> Color(0xFFFF9800)
                                else -> Color(0xFF4CAF50)
                            }
                            val displayLevel = when (level) {
                                "LOW" -> "Low"
                                "MEDIUM" -> "Medium"
                                "HIGH" -> "High"
                                else -> level
                            }
                            Surface(
                                onClick = { priority = level },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) color else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("priority_button_$level")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = displayLevel,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Due Date selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Surface(
                        onClick = {
                            val dialog = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    dueDate = selCal.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            dialog.show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("set_due_date_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dueDateStr,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            if (dueDate != null) {
                                IconButton(
                                    onClick = { dueDate = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Cancel,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Reminder Time Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Surface(
                        onClick = {
                            val dialog = TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val selCal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        set(Calendar.MINUTE, minute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    reminderTime = selCal.timeInMillis
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            )
                            dialog.show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("set_reminder_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reminderTimeStr,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            if (reminderTime != null) {
                                IconButton(
                                    onClick = { reminderTime = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Cancel,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, notes, priority, dueDate, reminderTime)
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    taskTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Task") },
        text = { Text("Are you sure you want to delete '$taskTitle'? This task will be permanently deleted.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = Modifier.testTag("delete_confirmation_dialog")
    )
}
