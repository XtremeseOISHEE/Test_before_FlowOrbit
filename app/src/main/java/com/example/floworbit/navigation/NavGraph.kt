package com.example.floworbit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.floworbit.presentation.analytics.AnalyticsScreen // ⭐ 1. Import the new AnalyticsScreen
import com.example.floworbit.presentation.focus.FocusTimerScreen
import com.example.floworbit.presentation.home.HomeScreen
import com.example.floworbit.presentation.task.TaskDetailScreen
import com.example.floworbit.ui.dnd.DNDPermissionScreen
import com.example.floworbit.presentation.dnd.DNDViewModel
import com.example.floworbit.presentation.blockedapps.BlockedAppsScreen

object Routes {
    const val HOME = "home"
    const val TASK_DETAIL = "task_detail"
    const val FOCUS = "focus"
    const val DND_PERMISSION = "dnd_permission"
    const val BLOCKED_APPS = "blocked_apps"
    const val ANALYTICS = "analytics" // ⭐ 2. Add the route for the new screen
}

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.DND_PERMISSION
    ) {

        // -----------------------------
        // DND Permission Screen
        // -----------------------------
        composable(Routes.DND_PERMISSION) {
            val dndViewModel: DNDViewModel = viewModel()

            LaunchedEffect(Unit) {
                dndViewModel.checkPermission()
            }

            val hasPermission = dndViewModel.hasPermission.collectAsState(initial = false).value

            LaunchedEffect(hasPermission) {
                if (hasPermission) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.DND_PERMISSION) { inclusive = true }
                    }
                }
            }

            DNDPermissionScreen(
                navController = navController,
                dndViewModel = dndViewModel
            )
        }

        // -----------------------------
        // Home Screen
        // -----------------------------
        composable(Routes.HOME) {
            HomeScreen(
                onOpenTask = { id ->
                    navController.navigate("${Routes.TASK_DETAIL}/$id")
                },
                onStartFocus = {
                    navController.navigate(Routes.FOCUS)
                },
                onOpenBlockedApps = {
                    navController.navigate(Routes.BLOCKED_APPS)
                },
                // ⭐ 3. Add a callback to navigate to the Analytics screen
                onOpenAnalytics = {
                    navController.navigate(Routes.ANALYTICS)
                }
            )
        }

        // -----------------------------
        // Analytics Screen (NEW)
        // -----------------------------
        composable(Routes.ANALYTICS) {
            AnalyticsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // -----------------------------
        // Blocked Apps Screen
        // -----------------------------
        composable(Routes.BLOCKED_APPS) {
            BlockedAppsScreen()
        }

        // -----------------------------
        // Task Detail Screen
        // -----------------------------
        composable("${Routes.TASK_DETAIL}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        // -----------------------------
        // Focus Timer Screen
        // -----------------------------
        composable(Routes.FOCUS) {
            FocusTimerScreen(onBack = { navController.popBackStack() })
        }
    }
}
