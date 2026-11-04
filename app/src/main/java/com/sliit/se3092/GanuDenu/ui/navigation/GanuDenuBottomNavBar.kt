package com.sliit.se3092.GanuDenu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.sliit.se3092.GanuDenu.ui.theme.CanaryYellow
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun GanuDenuBottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val ThemeBlack = Color(0xFF000000)

    // Check if the current route belongs to the Expenses flow
    val isExpenseSelected = currentRoute in listOf(
        Screen.ExpenseHub.route,
        Screen.DailyExpenses.route,
        Screen.DailyExpenseLogger.route,
        Screen.RecurringExpenses.route,
        Screen.RecurringExpenseLogger.route,
        Screen.QuickExpense.route
    )

    NavigationBar(
        containerColor = Color(0xFF121214),
        contentColor = Color.LightGray
    ) {
        // Helper function to handle backstack state cleanly
        val navigateTo = { route: String ->
            if (currentRoute == Screen.AddIncome.route && route == Screen.IncomeHub.route) {
                navController.popBackStack(Screen.IncomeHub.route, false)
            } else if (isExpenseSelected && route == Screen.ExpenseHub.route) {
                navController.popBackStack(Screen.ExpenseHub.route, false)
            } else if (currentRoute != route) {
                navController.navigate(route) {
                    popUpTo(Screen.Dashboard.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { navigateTo(Screen.Dashboard.route) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ThemeBlack, selectedTextColor = CanaryYellow, indicatorColor = CanaryYellow,
                unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ArrowDownward, contentDescription = "Income") },
            label = { Text("Income") },
            selected = currentRoute == Screen.IncomeHub.route || currentRoute == Screen.AddIncome.route,
            onClick = { navigateTo(Screen.IncomeHub.route) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ThemeBlack, selectedTextColor = CanaryYellow, indicatorColor = CanaryYellow,
                unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ArrowUpward, contentDescription = "Expenses") },
            label = { Text("Expenses") },
            selected = isExpenseSelected,
            onClick = { navigateTo(Screen.ExpenseHub.route) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ThemeBlack, selectedTextColor = CanaryYellow, indicatorColor = CanaryYellow,
                unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == Screen.Settings.route,
            onClick = { navigateTo(Screen.Settings.route) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ThemeBlack, selectedTextColor = CanaryYellow, indicatorColor = CanaryYellow,
                unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray
            )
        )
    }
}
