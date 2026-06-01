package com.finolize.app.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.finolize.app.R

sealed class Screen(val route: String, @StringRes val labelResourceId: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object History : Screen("history", R.string.nav_history, Icons.Default.History)
    object Stats : Screen("stats", R.string.nav_stats, Icons.Default.DateRange)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)
    object AddExpense : Screen("add_expense?expenseId={expenseId}", R.string.add_expense, Icons.Default.Add)
}