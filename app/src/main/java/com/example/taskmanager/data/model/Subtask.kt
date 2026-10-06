package com.example.taskmanager.data.model

import java.util.UUID

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val completed: Boolean = false
)
