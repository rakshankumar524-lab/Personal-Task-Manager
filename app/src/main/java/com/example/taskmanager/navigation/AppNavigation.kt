package com.example.taskmanager.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskmanager.ui.screens.AddTaskScreen
import com.example.taskmanager.ui.screens.EditTaskScreen
import com.example.taskmanager.ui.screens.HomeScreen
import com.example.taskmanager.ui.screens.LoginScreen
import com.example.taskmanager.ui.screens.ProfileScreen
import com.example.taskmanager.ui.screens.RegisterScreen
import com.example.taskmanager.ui.screens.SplashScreen
import com.example.taskmanager.ui.screens.TaskDetailsScreen
import com.example.taskmanager.ui.screens.TasksScreen
import com.example.taskmanager.viewmodel.AuthViewModel
import com.example.taskmanager.viewmodel.TaskViewModel
import com.example.taskmanager.viewmodel.ThemeViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    themeViewModel: ThemeViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentUser by authViewModel.currentUser.collectAsState()

    // Pass current user ID to task viewmodel if logged in
    currentUser?.uid?.let { uid ->
        taskViewModel.setUserId(uid)
    }

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Tasks,
        Screen.Profile
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                item.icon?.let {
                                    Icon(imageVector = it, contentDescription = item.title)
                                }
                            },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    authViewModel = authViewModel,
                    taskViewModel = taskViewModel,
                    onNavigateProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateAddTask = { navController.navigate(Screen.AddTask.route) },
                    onNavigateTaskDetails = { taskId ->
                        navController.navigate(Screen.TaskDetails.createRoute(taskId))
                    }
                )
            }

            composable(Screen.Tasks.route) {
                TasksScreen(
                    taskViewModel = taskViewModel,
                    onNavigateAddTask = { navController.navigate(Screen.AddTask.route) },
                    onNavigateTaskDetails = { taskId ->
                        navController.navigate(Screen.TaskDetails.createRoute(taskId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    taskViewModel = taskViewModel,
                    themeViewModel = themeViewModel,
                    onNavigateLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AddTask.route) {
                AddTaskScreen(
                    onNavigateBack = { navController.popBackStack() },
                    taskViewModel = taskViewModel
                )
            }

            composable(
                route = Screen.TaskDetails.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                TaskDetailsScreen(
                    taskId = taskId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateEdit = { id ->
                        navController.navigate(Screen.EditTask.createRoute(id))
                    },
                    taskViewModel = taskViewModel
                )
            }

            composable(
                route = Screen.EditTask.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                EditTaskScreen(
                    taskId = taskId,
                    onNavigateBack = { navController.popBackStack() },
                    taskViewModel = taskViewModel
                )
            }
        }
    }
}
