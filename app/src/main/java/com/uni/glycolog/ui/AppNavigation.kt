package com.uni.glycolog.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToAddEntry = dropUnlessResumed {
                    navController.navigate("add_entry") { launchSingleTop = true }
                },
                onNavigateToHistory = dropUnlessResumed {
                    navController.navigate("history") { launchSingleTop = true }
                },
                onNavigateToReport = dropUnlessResumed {
                    navController.navigate("report") { launchSingleTop = true }
                },
                onNavigateToReminders = dropUnlessResumed {
                    navController.navigate("reminders") { launchSingleTop = true }
                }
            )
        }

        composable("add_entry") {
            AddEntryScreen(
                viewModel = viewModel,
                onNavigateBack = dropUnlessResumed { navController.popBackStack() }
            )
        }

        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = dropUnlessResumed { navController.popBackStack() }
            )
        }

        composable("report") {
            ReportScreen(
                viewModel = viewModel,
                onNavigateBack = dropUnlessResumed { navController.popBackStack() }
            )
        }

        composable("reminders") {
            ReminderScreen(
                onNavigateBack = dropUnlessResumed { navController.popBackStack() }
            )
        }
    }
}
