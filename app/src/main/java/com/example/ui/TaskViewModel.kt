package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Task
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.receiver.ReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterOption {
    ALL, ACTIVE, COMPLETED
}

enum class SortOption {
    CREATION_DATE, DUE_DATE
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val sharedPrefs = application.getSharedPreferences("todo_settings", Context.MODE_PRIVATE)

    // UI state states
    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(FilterOption.ALL)
    val selectedSort = MutableStateFlow(SortOption.CREATION_DATE)

    // Settings states
    val isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
    val isNotificationsEnabled = MutableStateFlow(sharedPrefs.getBoolean("notifications_enabled", true))

    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
    }

    // Combine tasks from repository with search, filter, and sort options
    val tasksState: StateFlow<List<Task>> = combine(
        repository.allTasks,
        searchQuery,
        selectedFilter,
        selectedSort
    ) { allTasks, query, filter, sort ->
        var filteredList = allTasks

        // Apply Search Filter
        if (query.isNotBlank()) {
            filteredList = filteredList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.notes.contains(query, ignoreCase = true)
            }
        }

        // Apply Filter Option
        filteredList = when (filter) {
            FilterOption.ALL -> filteredList
            FilterOption.ACTIVE -> filteredList.filter { !it.isCompleted }
            FilterOption.COMPLETED -> filteredList.filter { it.isCompleted }
        }

        // Apply Sort Option
        filteredList = when (sort) {
            SortOption.CREATION_DATE -> filteredList.sortedByDescending { it.creationDate }
            SortOption.DUE_DATE -> filteredList.sortedWith(
                compareBy<Task> { it.dueDate == null } // put nulls at the end
                    .thenBy { it.dueDate ?: Long.MAX_VALUE }
                    .thenByDescending { it.creationDate }
            )
        }

        filteredList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Derive stats from all tasks (unfiltered)
    val statsState: StateFlow<TaskStats> = repository.allTasks.combine(MutableStateFlow(Unit)) { allTasks, _ ->
        val total = allTasks.size
        val completed = allTasks.count { it.isCompleted }
        val pending = total - completed
        val progress = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0
        TaskStats(total, completed, pending, progress)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskStats()
    )

    // Task operations
    fun addTask(
        title: String,
        notes: String,
        priority: String,
        dueDate: Long?,
        reminderTime: Long?
    ) {
        viewModelScope.launch {
            val isReminderActive = reminderTime != null && isNotificationsEnabled.value
            val newTask = Task(
                title = title,
                notes = notes,
                priority = priority,
                dueDate = dueDate,
                reminderTime = reminderTime,
                isReminderActive = isReminderActive
            )
            val id = repository.insert(newTask)
            if (isReminderActive) {
                ReminderManager.scheduleReminder(
                    getApplication(),
                    id.toInt(),
                    title,
                    notes,
                    reminderTime
                )
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)

            // Sync reminder on update
            if (task.isReminderActive && task.reminderTime != null && !task.isCompleted) {
                ReminderManager.scheduleReminder(
                    getApplication(),
                    task.id,
                    task.title,
                    task.notes,
                    task.reminderTime
                )
            } else {
                ReminderManager.cancelReminder(getApplication(), task.id)
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        updateTask(updatedTask)
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            ReminderManager.cancelReminder(getApplication(), task.id)
            repository.delete(task)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            // Cancel reminders for completed tasks before deleting them
            // Normally we'd fetch them first, but standard procedure is ok
            repository.clearCompleted()
        }
    }

    // Settings operations
    fun toggleDarkMode() {
        val newVal = !isDarkMode.value
        isDarkMode.value = newVal
        sharedPrefs.edit().putBoolean("dark_mode", newVal).apply()
    }

    fun toggleNotifications() {
        val newVal = !isNotificationsEnabled.value
        isNotificationsEnabled.value = newVal
        sharedPrefs.edit().putBoolean("notifications_enabled", newVal).apply()

        // If disabled, cancel all? Or we check setting at broadcast trigger, which we already do!
        // Checking at broadcast trigger is extremely robust because it respects the runtime state of the toggle.
    }
}

data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val progressPercent: Int = 0
)
