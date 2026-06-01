package com.finolize.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.ui.Modifier
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.finolize.app.presentation.components.FinolizeBottomBar
import com.finolize.app.presentation.components.Screen
import com.finolize.app.presentation.screen.add_expense.AddExpenseScreen
import com.finolize.app.presentation.screen.history.HistoryScreen
import com.finolize.app.presentation.screen.home.HomeScreen
import com.finolize.app.presentation.screen.settings.SettingsScreen
import com.finolize.app.presentation.screen.stats.StatsScreen
import com.finolize.app.ui.theme.FinolizeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinolizeTheme {
                val navController = rememberNavController()
                FinolizeAppContent(navController)
            }
        }
    }

    @Composable
    fun FinolizeAppContent(navController: NavHostController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isAddOrEditScreen = currentRoute?.startsWith("add_expense") == true

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = !isAddOrEditScreen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    FinolizeBottomBar(currentRoute, { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
                }
            },
            floatingActionButton = {
                AnimatedVisibility(visible = !isAddOrEditScreen, enter = scaleIn(), exit = scaleOut()) {
                    FloatingActionButton(
                        onClick = { navController.navigate("add_expense") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) { Icon(Icons.Default.Add, "Add") }
                }
            }
        ) { innerPadding ->
            NavHost(navController, Screen.Home.route, Modifier.fillMaxSize()) {
                composable(Screen.Home.route) { HomeScreen(innerPadding, navController) }
                composable(Screen.History.route) { HistoryScreen(innerPadding) }
                composable(Screen.Stats.route) { StatsScreen(innerPadding) }
                composable(Screen.Settings.route) { SettingsScreen(innerPadding) }
                composable(
                    route = Screen.AddExpense.route,
                    arguments = listOf(navArgument("expenseId") { type = NavType.LongType; defaultValue = -1L })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("expenseId") ?: -1L
                    AddExpenseScreen(id, { navController.popBackStack() })
                }
            }
        }
    }
}