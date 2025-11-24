package com.example.floworbit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.floworbit.presentation.focus.FocusTimerScreen
import com.example.floworbit.presentation.home.HomeScreen
import com.example.floworbit.presentation.task.TaskDetailScreen

object Routes {
    const val HOME = "home"
    const val TASK_DETAIL = "task_detail"
    const val FOCUS = "focus"
}

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(navController = navController, startDestination = Routes.HOME) {

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

        composable(Routes.FOCUS) {
            FocusTimerScreen(navController = navController)
        }
    }
}
