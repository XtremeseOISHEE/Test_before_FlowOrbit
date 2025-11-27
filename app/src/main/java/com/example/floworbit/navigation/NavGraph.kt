package com.example.floworbit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.floworbit.presentation.focus.FocusTimerScreen
import com.example.floworbit.presentation.home.HomeScreen
import com.example.floworbit.presentation.task.TaskDetailScreen
import com.example.floworbit.ui.dnd.DNDPermissionScreen

object Routes {
    const val HOME = "home"
    const val TASK_DETAIL = "task_detail"
    const val FOCUS = "focus"
    const val DND_PERMISSION = "dnd_permission"
}

@Composable
fun NavGraph(navController: NavHostController) {

    // 🟣 NEW: Start from DND permission screen
    NavHost(navController = navController, startDestination = Routes.DND_PERMISSION) {

        // 🟣 Check DND permission first
        composable(Routes.DND_PERMISSION) {
            DNDPermissionScreen(navController)
        }

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

        composable("${Routes.TASK_DETAIL}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        // 🔵 Focus Screen is UNCHANGED (your MVVM logic safe)
        composable(Routes.FOCUS) {
            FocusTimerScreen()
        }
    }
}
