package com.finolize.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.finolize.app.core.utils.BiometricHelper
import com.finolize.app.data.local.prefs.PreferenceManager
import com.finolize.app.domain.repository.ExpenseRepository
import com.finolize.app.presentation.components.FinolizeBottomBar
import com.finolize.app.presentation.components.Screen
import com.finolize.app.presentation.screen.add_expense.AddExpenseScreen
import com.finolize.app.presentation.screen.categories.AddCategoryScreen
import com.finolize.app.presentation.screen.categories.ManageCategoriesScreen
import com.finolize.app.presentation.screen.history.HistoryScreen
import com.finolize.app.presentation.screen.home.HomeScreen
import com.finolize.app.presentation.screen.onboarding.OnboardingScreen
import com.finolize.app.presentation.screen.settings.SettingsScreen
import com.finolize.app.presentation.screen.stats.StatsScreen
import com.finolize.app.ui.theme.FinolizeTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var repository: ExpenseRepository
    @Inject lateinit var preferenceManager: PreferenceManager

    private var isUnlocked by mutableStateOf(false)
    private var showOnboarding by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showOnboarding = preferenceManager.isFirstRun()

        val biometricEnabledInApp = preferenceManager.isBiometricEnabled()
        val canAuthenticate = BiometricHelper.canAuthenticate(this)
        val shouldShowPrompt = biometricEnabledInApp && canAuthenticate
        isUnlocked = !shouldShowPrompt

        if (shouldShowPrompt) {
            BiometricHelper.showBiometricPrompt(
                activity = this,
                onSuccess = { isUnlocked = true },
                onError = { finish() }
            )
        }

        val savedLang = preferenceManager.getLanguage()
        val appLocale = LocaleListCompat.forLanguageTags(savedLang)
        AppCompatDelegate.setApplicationLocales(appLocale)

        lifecycleScope.launch {
            repository.prefillCategories(applicationContext)
        }
        enableEdgeToEdge()
        setContent {
            FinolizeTheme {
                if (showOnboarding) {
                    OnboardingScreen(onFinished = {
                        preferenceManager.setFirstRun(false)
                        showOnboarding = false
                    })
                } else if (isUnlocked) {
                    val navController = rememberNavController()
                    FinolizeAppContent(navController)
                }
            }
        }
    }

    @Composable
    fun FinolizeAppContent(navController: NavHostController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
        val isFullScreen = currentRoute.startsWith("add_expense") ||
                currentRoute.startsWith("add_category") ||
                currentRoute == "manage_categories"

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = !isFullScreen,
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
                AnimatedVisibility(visible = !isFullScreen && currentRoute == Screen.Home.route, enter = scaleIn(), exit = scaleOut()) {
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
                composable(Screen.History.route) { HistoryScreen(innerPadding, navController) }
                composable(Screen.Stats.route) { StatsScreen(innerPadding) }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        paddingValues = innerPadding,
                        onNavigateToManageCategories = { navController.navigate("manage_categories") }
                    )
                }
                composable(
                    route = Screen.AddExpense.route,
                    arguments = listOf(navArgument("expenseId") {
                        type = NavType.LongType; defaultValue = -1L
                    })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("expenseId") ?: -1L
                    AddExpenseScreen(
                        expenseId = id,
                        onNavigateBack = { navController.popBackStack() },
                        navController = navController
                    )
                }
                composable(
                    route = "add_category?categoryId={categoryId}",
                    arguments = listOf(navArgument("categoryId") { type = NavType.LongType; defaultValue = -1L })
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: -1L
                    AddCategoryScreen(
                        categoryId = categoryId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("manage_categories") {
                    ManageCategoriesScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAddCategory = { navController.navigate("add_category")},
                        onNavigateToEditCategory = { id ->
                            navController.navigate("add_category?categoryId=$id")
                        }
                    )
                }
            }
        }
    }
}