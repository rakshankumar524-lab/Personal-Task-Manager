package com.example.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.taskmanager.navigation.AppNavigation
import com.example.taskmanager.ui.theme.PersonalTaskManagerTheme
import com.example.taskmanager.viewmodel.AuthViewModel
import com.example.taskmanager.viewmodel.TaskViewModel
import com.example.taskmanager.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            PersonalTaskManagerTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    authViewModel = authViewModel,
                    taskViewModel = taskViewModel,
                    themeViewModel = themeViewModel
                )
            }
        }
    }
}
