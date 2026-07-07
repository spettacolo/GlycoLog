package com.uni.glycolog.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        composable("dashboard") {
            DashboardScreen(
                onNavigateToAddEntry = { navController.navigate("add_entry") }
            )
        }

        composable("add_entry") {
            AddEntryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}