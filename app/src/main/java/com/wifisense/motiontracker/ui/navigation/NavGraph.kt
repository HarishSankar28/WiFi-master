package com.wifisense.motiontracker.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wifisense.motiontracker.ui.screen.calibration.CalibrationScreen
import com.wifisense.motiontracker.ui.screen.dashboard.DashboardScreen
import com.wifisense.motiontracker.ui.screen.history.HistoryScreen
import com.wifisense.motiontracker.ui.screen.settings.SettingsScreen
import com.wifisense.motiontracker.ui.theme.NavyCard
import com.wifisense.motiontracker.ui.theme.NavySurface
import com.wifisense.motiontracker.ui.theme.CyanPrimary
import com.wifisense.motiontracker.ui.theme.TextSecondary

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard  : Screen("dashboard",  "Monitor",  Icons.Filled.Wifi)
    data object History    : Screen("history",    "History",  Icons.Filled.History)
    data object Settings   : Screen("settings",   "Settings", Icons.Filled.Settings)
    data object Calibration: Screen("calibration","Calibrate",Icons.Filled.Wifi)
}

private val bottomNavItems = listOf(Screen.Dashboard, Screen.History, Screen.Settings)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = NavyCard,
                    tonalElevation = androidx.compose.ui.unit.Dp(0f)
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label
                                )
                            },
                            label = { Text(text = screen.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = NavySurface
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            enterTransition = {
                fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 }
            },
            exitTransition = {
                fadeOut(tween(200))
            },
            popEnterTransition = {
                fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 }
            },
            popExitTransition = {
                fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 4 }
            }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    paddingValues = innerPadding,
                    onNavigateToCalibration = { navController.navigate(Screen.Calibration.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(paddingValues = innerPadding)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    paddingValues = innerPadding,
                    onNavigateToCalibration = { navController.navigate(Screen.Calibration.route) }
                )
            }
            composable(Screen.Calibration.route) {
                CalibrationScreen(
                    onCalibrationComplete = { navController.popBackStack() }
                )
            }
        }
    }
}
