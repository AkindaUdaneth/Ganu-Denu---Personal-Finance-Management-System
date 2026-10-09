package com.sliit.se3092.GanuDenu.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sliit.se3092.GanuDenu.ui.theme.PureBlack
import com.sliit.se3092.GanuDenu.ui.theme.PureWhite

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            PlaceholderScreen(title = "Dashboard Screen")
        }
        composable(Screen.QuickExpense.route) {
            PlaceholderScreen(title = "Quick Expense Log")
        }
        composable(Screen.CatchUp.route) {
            PlaceholderScreen(title = "Catch-Up Mode")
        }
        composable(Screen.RecurringManager.route) {
            PlaceholderScreen(title = "Recurring & Subscriptions")
        }
        composable(Screen.IncomeHub.route) {
            PlaceholderScreen(title = "Income Hub")
        }
        composable(Screen.GoalDetail.route) {
            PlaceholderScreen(title = "MacBook Goal Detail")
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = PureWhite)
    }
}