package com.example.taskmanager.data.repository

import com.example.taskmanager.data.model.Subtask
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.utils.DateUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class TaskRepository {

    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    // Local fallback tasks cache for offline or demo mode
    private val localTasks = MutableStateFlow<List<Task>>(initialSampleTasks())

    private fun initialSampleTasks(): List<Task> {
        val calendar = Calendar.getInstance()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val today = isoFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -2)
        val overdueDate = isoFormat.format(calendar.time)

        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = isoFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, 2)
        val futureDate = isoFormat.format(calendar.time)

        return listOf(
            Task(
                id = "sample_1",
                userId = "demo_user_123",
                title = "Prepare Android Project Demo",
                description = "Finalize Jetpack Compose UI, MVVM architecture, and verify task filtering.",
                dueDate = today,
                priority = "High",
                category = "College",
                status = "In Progress",
                subtasks = listOf(
                    Subtask("s1_1", "Verify Navigation Flow", true),
                    Subtask("s1_2", "Check Dark Mode Persistence", true),
                    Subtask("s1_3", "Test Subtask Toggle Checkbox", false)
                )
            ),
            Task(
                id = "sample_2",
                userId = "demo_user_123",
                title = "Submit Database Management Assignment",
                description = "Complete ER Diagrams and SQL queries for Chapter 4 assignment.",
                dueDate = overdueDate,
                priority = "High",
                category = "College",
                status = "Pending",
                subtasks = listOf(
                    Subtask("s2_1", "Draw ER Diagram in StarUML", true),
                    Subtask("s2_2", "Write SQL Create Table queries", true),
                    Subtask("s2_3", "Export PDF and submit on Portal", false)
                )
            ),
            Task(
                id = "sample_3",
                userId = "demo_user_123",
                title = "Weekly Grocery Shopping",
                description = "Purchase fresh vegetables, organic milk, whole wheat bread, and fruits.",
                dueDate = today,
                priority = "Medium",
                category = "Shopping",
                status = "Pending",
                subtasks = listOf(
                    Subtask("s3_1", "Apples & Bananas", false),
                    Subtask("s3_2", "Almond Milk", false),
                    Subtask("s3_3", "Oats & Greek Yogurt", false)
                )
            ),
            Task(
                id = "sample_4",
                userId = "demo_user_123",
                title = "Team Sprint Planning Meeting",
                description = "Sync with project leads to review user stories and sprint backlog.",
                dueDate = tomorrow,
                priority = "High",
                category = "Work",
                status = "Pending",
                subtasks = listOf(
                    Subtask("s4_1", "Review Jira Backlog Tickets", true),
                    Subtask("s4_2", "Prepare Sprint Demo Slides", false)
                )
            ),
            Task(
                id = "sample_5",
                userId = "demo_user_123",
                title = "Evening Workout Session",
                description = "45-minute cardio exercise and resistance training at the gym.",
                dueDate = today,
                priority = "Low",
                category = "Personal",
                status = "Completed",
                subtasks = listOf(
                    Subtask("s5_1", "15 min Warmup Treadmill", true),
                    Subtask("s5_2", "Core & Upper Body Exercises", true)
                )
            ),
            Task(
                id = "sample_6",
                userId = "demo_user_123",
                title = "Order Course Textbooks",
                description = "Purchase Mobile Computing and Data Structures textbooks online.",
                dueDate = futureDate,
                priority = "Medium",
                category = "Shopping",
                status = "Pending",
                subtasks = emptyList()
            ),
            Task(
                id = "sample_7",
                userId = "demo_user_123",
                title = "Read Tech Blog Articles",
                description = "Read latest updates on Kotlin 2.0 and Jetpack Compose performance optimizations.",
                dueDate = tomorrow,
                priority = "Low",
                category = "Personal",
                status = "Completed",
                subtasks = listOf(
                    Subtask("s7_1", "Read Compose Stability Guide", true),
                    Subtask("s7_2", "Try Coroutines Flow Operators", true)
                )
            )
        )
    }

    fun getTasksFlow(userId: String): Flow<List<Task>> {
        val firestore = db
        if (firestore != null) {
            return callbackFlow {
                val registration = firestore.collection("tasks")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            // On Firestore error or missing indexes, fallback to local flow
                            val seeded = localTasks.value.map { it.copy(userId = userId) }
                            trySend(seeded)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val remoteTasks = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(Task::class.java)?.copy(id = doc.id)
                            }
                            if (remoteTasks.isEmpty()) {
                                val seeded = localTasks.value.map { it.copy(userId = userId) }
                                trySend(seeded)
                            } else {
                                trySend(remoteTasks)
                            }
                        } else {
                            val seeded = localTasks.value.map { it.copy(userId = userId) }
                            trySend(seeded)
                        }
                    }
                awaitClose { registration.remove() }
            }
        } else {
            return MutableStateFlow(localTasks.value.map { it.copy(userId = userId) })
        }
    }

    suspend fun addTask(task: Task): Result<String> {
        val firestore = db
        val taskId = if (task.id.isBlank()) UUID.randomUUID().toString() else task.id
        val taskWithId = task.copy(id = taskId)

        return try {
            if (firestore != null) {
                firestore.collection("tasks").document(taskId).set(taskWithId).await()
            }
            val current = localTasks.value.toMutableList()
            current.add(0, taskWithId)
            localTasks.value = current
            Result.success(taskId)
        } catch (e: Exception) {
            val current = localTasks.value.toMutableList()
            current.add(0, taskWithId)
            localTasks.value = current
            Result.success(taskId)
        }
    }

    suspend fun updateTask(task: Task): Result<Unit> {
        val firestore = db
        return try {
            if (firestore != null && task.id.isNotBlank()) {
                firestore.collection("tasks").document(task.id).set(task).await()
            }
            val current = localTasks.value.toMutableList()
            val index = current.indexOfFirst { it.id == task.id }
            if (index != -1) {
                current[index] = task
                localTasks.value = current
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val current = localTasks.value.toMutableList()
            val index = current.indexOfFirst { it.id == task.id }
            if (index != -1) {
                current[index] = task
                localTasks.value = current
            }
            Result.success(Unit)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        val firestore = db
        return try {
            if (firestore != null && taskId.isNotBlank()) {
                firestore.collection("tasks").document(taskId).delete().await()
            }
            val current = localTasks.value.toMutableList()
            current.removeAll { it.id == taskId }
            localTasks.value = current
            Result.success(Unit)
        } catch (e: Exception) {
            val current = localTasks.value.toMutableList()
            current.removeAll { it.id == taskId }
            localTasks.value = current
            Result.success(Unit)
        }
    }

    suspend fun toggleSubtask(taskId: String, subtaskId: String, completed: Boolean): Result<Unit> {
        val current = localTasks.value.toMutableList()
        val index = current.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val task = current[index]
            val updatedSubtasks = task.subtasks.map {
                if (it.id == subtaskId) it.copy(completed = completed) else it
            }
            val updatedTask = task.copy(subtasks = updatedSubtasks)
            return updateTask(updatedTask)
        }
        return Result.failure(Exception("Task not found"))
    }

    suspend fun markTaskStatus(taskId: String, status: String): Result<Unit> {
        val current = localTasks.value.toMutableList()
        val index = current.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val task = current[index]
            val updatedTask = task.copy(status = status)
            return updateTask(updatedTask)
        }
        return Result.failure(Exception("Task not found"))
    }
}
