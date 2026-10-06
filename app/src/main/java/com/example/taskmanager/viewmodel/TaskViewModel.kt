package com.example.taskmanager.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.repository.TaskRepository
import com.example.taskmanager.utils.DateUtils
import com.example.taskmanager.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _currentUserId = MutableStateFlow("demo_user_123")

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())

    val statusFilter = MutableStateFlow("All")
    val priorityFilter = MutableStateFlow("All")
    val categoryFilter = MutableStateFlow("All")
    val selectedDateKey = MutableStateFlow("ALL")
    val searchQuery = MutableStateFlow("")
    val sortOption = MutableStateFlow("Due Date")

    val customCategories = MutableStateFlow(listOf("College", "Work", "Personal", "Shopping", "Other"))

    fun addCustomCategory(newCategory: String) {
        if (newCategory.isNotBlank() && !customCategories.value.contains(newCategory.trim())) {
            customCategories.value = customCategories.value + newCategory.trim()
        }
    }

    fun triggerReminder(context: Context, task: Task) {
        NotificationHelper.showTaskReminder(context, task.title, task.description)
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadTasks()
    }

    fun setUserId(userId: String) {
        if (_currentUserId.value != userId) {
            _currentUserId.value = userId
            loadTasks()
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTasksFlow(_currentUserId.value).collect { tasks ->
                _allTasks.value = tasks
                _isLoading.value = false
            }
        }
    }

    // Filtered and Sorted Tasks list
    val filteredTasks: StateFlow<List<Task>> = combine(
        _allTasks,
        statusFilter,
        priorityFilter,
        categoryFilter,
        selectedDateKey,
        searchQuery,
        sortOption
    ) { params ->
        val tasks = params[0] as List<Task>
        val status = params[1] as String
        val priority = params[2] as String
        val category = params[3] as String
        val dateKey = params[4] as String
        val query = (params[5] as String).lowercase().trim()
        val sort = params[6] as String

        var result = tasks

        // Search filter
        if (query.isNotEmpty()) {
            result = result.filter {
                it.title.lowercase().contains(query) || it.description.lowercase().contains(query)
            }
        }

        // Status filter
        if (status != "All") {
            result = result.filter { it.status == status }
        }

        // Priority filter
        if (priority != "All") {
            result = result.filter { it.priority == priority }
        }

        // Category filter
        if (category != "All") {
            result = result.filter { it.category == category }
        }

        // Date filter
        val today = DateUtils.todayString()
        if (dateKey == "OVERDUE") {
            result = result.filter { DateUtils.isOverdue(it.dueDate) && !it.isCompleted }
        } else if (dateKey != "ALL") {
            result = result.filter { it.dueDate == dateKey }
        }

        // Sorting
        result = when (sort) {
            "Due Date" -> result.sortedBy { it.dueDate }
            "Priority: High → Low" -> result.sortedByDescending { priorityOrder(it.priority) }
            "Priority: Low → High" -> result.sortedBy { priorityOrder(it.priority) }
            "Title: A → Z" -> result.sortedBy { it.title.lowercase() }
            "Title: Z → A" -> result.sortedByDescending { it.title.lowercase() }
            "Recently Created" -> result.sortedByDescending { it.createdAt }
            else -> result
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun priorityOrder(priority: String): Int {
        return when (priority) {
            "High" -> 3
            "Medium" -> 2
            "Low" -> 1
            else -> 0
        }
    }

    // Dashboard Statistics
    val totalTasksCount: StateFlow<Int> = combine(_allTasks) { tasks ->
        tasks[0].size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingTasksCount: StateFlow<Int> = combine(_allTasks) { tasks ->
        tasks[0].count { it.status == "Pending" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inProgressTasksCount: StateFlow<Int> = combine(_allTasks) { tasks ->
        tasks[0].count { it.status == "In Progress" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedTasksCount: StateFlow<Int> = combine(_allTasks) { tasks ->
        tasks[0].count { it.status == "Completed" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Today's Progress Stats
    val todayProgressPercent: StateFlow<Int> = combine(_allTasks) { tasks ->
        val today = DateUtils.todayString()
        val todayTasks = tasks[0].filter { it.dueDate == today }
        if (todayTasks.isEmpty()) {
            0
        } else {
            val completed = todayTasks.count { it.isCompleted }
            ((completed.toFloat() / todayTasks.size.toFloat()) * 100).toInt()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isTodayAllCompleted: StateFlow<Boolean> = combine(_allTasks) { tasks ->
        val today = DateUtils.todayString()
        val todayTasks = tasks[0].filter { it.dueDate == today }
        todayTasks.isNotEmpty() && todayTasks.all { it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addTask(task: Task, onComplete: () -> Unit) {
        if (task.title.isBlank()) {
            _errorMessage.value = "Task title is required."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val taskWithUser = task.copy(userId = _currentUserId.value)
            repository.addTask(taskWithUser)
            _isLoading.value = false
            onComplete()
        }
    }

    fun updateTask(task: Task, onComplete: () -> Unit) {
        if (task.title.isBlank()) {
            _errorMessage.value = "Task title is required."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateTask(task)
            _isLoading.value = false
            onComplete()
        }
    }

    fun deleteTask(taskId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteTask(taskId)
            _isLoading.value = false
            onComplete()
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.isCompleted) "Pending" else "Completed"
            repository.markTaskStatus(task.id, newStatus)
        }
    }

    fun toggleSubtask(taskId: String, subtaskId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleSubtask(taskId, subtaskId, completed)
        }
    }

    fun getTaskById(taskId: String): Task? {
        return _allTasks.value.find { it.id == taskId }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
