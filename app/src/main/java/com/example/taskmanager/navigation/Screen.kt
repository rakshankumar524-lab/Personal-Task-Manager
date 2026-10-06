package com.example.taskmanager.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Tasks : Screen("tasks", "Tasks", Icons.Filled.CheckCircle)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)

    object AddTask : Screen("addTask")
    object TaskDetails : Screen("taskDetails/{taskId}") {
        fun createRoute(taskId: String) = "taskDetails/$taskId"
    }
    object EditTask : Screen("editTask/{taskId}") {
        fun createRoute(taskId: String) = "editTask/$taskId"
    }
}
