package com.example.floworbit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.floworbit.presentation.focus.FocusTimerScreen
import com.example.floworbit.presentation.home.HomeScreen
import com.example.floworbit.presentation.task.TaskDetailScreen
import com.example.floworbit.ui.dnd.DNDPermissionScreen
import com.example.floworbit.presentation.dnd.DNDViewModel

object Routes {
    const val HOME = "home"
    const val TASK_DETAIL = "task_detail"
    const val FOCUS = "focus"
    const val DND_PERMISSION = "dnd_permission"
}

@Composable
fun NavGraph(navController: NavHostController) {

// Start from DND permission screen
    NavHost(navController = navController, startDestination = Routes.DND_PERMISSION) {

        // DND Permission Screen
        composable(Routes.DND_PERMISSION) {
            // Create DNDViewModel
            val dndViewModel: DNDViewModel = viewModel()

            // Check permission on composition
            LaunchedEffect(Unit) {
                dndViewModel.checkPermission()
            }

            // Observe permission StateFlow
            val hasPermissionState = dndViewModel.hasPermission.collectAsState(initial = false)
            val hasPermission = hasPermissionState.value

            // Navigate to Home automatically if permission granted
            LaunchedEffect(hasPermission) {
                if (hasPermission) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DND_PERMISSION) { inclusive = true }
                    }
                }
            }

            // Show DNDPermissionScreen UI
            DNDPermissionScreen(navController = navController, dndViewModel = dndViewModel)
        }

        // Home Screen
        composable(Routes.HOME) {
            HomeScreen(
                onOpenTask = { id ->
                    navController.navigate("${Routes.TASK_DETAIL}/$id")
                },
                onStartFocus = {
                    navController.navigate(Routes.FOCUS)
                }
            )
        }

        // Task Detail Screen
        composable("${Routes.TASK_DETAIL}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        // Focus Timer Screen
        composable(Routes.FOCUS) {
            FocusTimerScreen()
        }
    }


}
