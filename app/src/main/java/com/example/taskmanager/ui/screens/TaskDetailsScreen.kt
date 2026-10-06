package com.example.taskmanager.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskmanager.ui.components.PrimaryButton
import com.example.taskmanager.ui.components.SecondaryOutlinedButton
import com.example.taskmanager.ui.components.SubtaskChecklist
import com.example.taskmanager.ui.theme.PriorityHigh
import com.example.taskmanager.ui.theme.PriorityHighContainer
import com.example.taskmanager.ui.theme.PriorityLow
import com.example.taskmanager.ui.theme.PriorityLowContainer
import com.example.taskmanager.ui.theme.PriorityMedium
import com.example.taskmanager.ui.theme.PriorityMediumContainer
import com.example.taskmanager.ui.theme.StatusCompleted
import com.example.taskmanager.ui.theme.StatusInProgress
import com.example.taskmanager.ui.theme.StatusPending
import com.example.taskmanager.utils.DateUtils
import com.example.taskmanager.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onNavigateEdit: (String) -> Unit,
    taskViewModel: TaskViewModel
) {
    val context = LocalContext.current
    val task = taskViewModel.getTaskById(taskId)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (task == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Task not found.")
        }
        return
    }

    val (priorityColor, priorityBg) = when (task.priority) {
        "High" -> PriorityHigh to PriorityHighContainer
        "Medium" -> PriorityMedium to PriorityMediumContainer
        else -> PriorityLow to PriorityLowContainer
    }

    val statusColor = when (task.status) {
        "Completed" -> StatusCompleted
        "In Progress" -> StatusInProgress
        else -> StatusPending
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateEdit(task.id) }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Title and Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = priorityBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${task.priority.uppercase()} PRIORITY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusColor, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = task.status,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Info Cards Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Due Date: ",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = DateUtils.formatDateDisplay(task.dueDate),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🏷 Category: ",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Subtasks Checklist
                SubtaskChecklist(
                    subtasks = task.subtasks,
                    onSubtasksChange = { updated ->
                        taskViewModel.updateTask(task.copy(subtasks = updated)) {}
                    },
                    isEditable = false
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                if (!task.isCompleted) {
                    PrimaryButton(
                        text = "Mark as Completed ✓",
                        onClick = {
                            taskViewModel.toggleTaskComplete(task)
                        },
                        containerColor = StatusCompleted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = StatusCompleted.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = StatusCompleted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Task Completed!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatusCompleted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Set Reminder Button
                SecondaryOutlinedButton(
                    text = "🔔 Set Reminder",
                    onClick = {
                        taskViewModel.triggerReminder(context, task)
                        Toast.makeText(context, "Reminder notification sent!", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Share Button
                SecondaryOutlinedButton(
                    text = "Share Task",
                    onClick = {
                        val subtasksSummary = if (task.subtasks.isNotEmpty()) {
                            "\nChecklist:\n" + task.subtasks.joinToString("\n") {
                                if (it.completed) "✓ ${it.title}" else "☐ ${it.title}"
                            }
                        } else ""

                        val shareText = """
                            Personal Task Manager
                            
                            Task: ${task.title}
                            
                            Description:
                            ${task.description.ifBlank { "No description provided." }}
                            
                            Due Date:
                            ${DateUtils.formatDateDisplay(task.dueDate)}
                            
                            Priority:
                            ${task.priority}
                            
                            Category:
                            ${task.category}
                            
                            Status:
                            ${task.status}
                            $subtasksSummary
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Task via")
                        context.startActivity(shareIntent)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task?") },
            text = { Text("Are you sure you want to permanently delete this task?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        taskViewModel.deleteTask(task.id) {
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
