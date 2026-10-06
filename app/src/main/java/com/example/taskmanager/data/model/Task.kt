package com.example.taskmanager.data.model

data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val priority: String = "Medium",
    val category: String = "Personal",
    val status: String = "Pending",
    val subtasks: List<Subtask> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val subtasksCompletedCount: Int
        get() = subtasks.count { it.completed }

    val subtasksTotalCount: Int
        get() = subtasks.size

    val isCompleted: Boolean
        get() = status == "Completed"
}
