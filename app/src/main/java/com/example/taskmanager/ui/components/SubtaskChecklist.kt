package com.example.taskmanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskmanager.data.model.Subtask
import java.util.UUID

@Composable
fun SubtaskChecklist(
    subtasks: List<Subtask>,
    onSubtasksChange: (List<Subtask>) -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true
) {
    var newSubtaskTitle by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Checklist / Subtasks",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        subtasks.forEachIndexed { index, subtask ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = subtask.completed,
                    onCheckedChange = { checked ->
                        val updated = subtasks.toMutableList()
                        updated[index] = subtask.copy(completed = checked)
                        onSubtasksChange(updated)
                    }
                )

                Text(
                    text = subtask.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (isEditable) {
                    IconButton(onClick = {
                        val updated = subtasks.toMutableList()
                        updated.removeAt(index)
                        onSubtasksChange(updated)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove subtask",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        if (isEditable) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSubtaskTitle,
                    onValueChange = { newSubtaskTitle = it },
                    placeholder = { Text("Add subtask item...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (newSubtaskTitle.isNotBlank()) {
                            val newSubtask = Subtask(
                                id = UUID.randomUUID().toString(),
                                title = newSubtaskTitle.trim(),
                                completed = false
                            )
                            onSubtasksChange(subtasks + newSubtask)
                            newSubtaskTitle = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add subtask",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
