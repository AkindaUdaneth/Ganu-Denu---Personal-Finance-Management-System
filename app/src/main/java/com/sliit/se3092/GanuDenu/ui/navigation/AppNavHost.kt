package com.sliit.se3092.GanuDenu.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sliit.se3092.GanuDenu.ui.analytics.SpendingAnalyticsScreen
import com.sliit.se3092.GanuDenu.ui.analytics.SpendingAnalyticsViewModel
import com.sliit.se3092.GanuDenu.ui.dashboard.DashboardScreen
import com.sliit.se3092.GanuDenu.ui.dashboard.DashboardViewModel
import com.sliit.se3092.GanuDenu.ui.goals.GoalEngineScreen
import com.sliit.se3092.GanuDenu.ui.goals.GoalEngineViewModel
import com.sliit.se3092.GanuDenu.ui.goals.GoalFormScreen
import com.sliit.se3092.GanuDenu.ui.goals.GoalFormViewModel
import com.sliit.se3092.GanuDenu.ui.settings.ProfileFormScreen
import com.sliit.se3092.GanuDenu.ui.settings.ProfileFormViewModel
import com.sliit.se3092.GanuDenu.ui.settings.SettingsScreen
import com.sliit.se3092.GanuDenu.ui.settings.SettingsViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import org.koin.androidx.compose.koinViewModel
import com.sliit.se3092.GanuDenu.ui.income.IncomeDashboardScreen
import com.sliit.se3092.GanuDenu.ui.income.IncomeViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only display the Bottom Navigation on main anchor screens
    val showBottomNav = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.GoalEngine.route,
        Screen.Analytics.route,
        Screen.IncomeHub.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                GanuDenuBottomNavBar(navController = navController)
            }
        },
        containerColor = Color(0xFF000000),
        modifier = modifier
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // --- Completed UI Screens ---
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onGoalClick = { navController.navigate(Screen.GoalEngine.route) },
                    onAnalyticsClick = { navController.navigate(Screen.Analytics.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Analytics.route) {
                val viewModel: SpendingAnalyticsViewModel = viewModel()
                SpendingAnalyticsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.GoalEngine.route) {
                val viewModel: GoalEngineViewModel = viewModel()
                GoalEngineScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate(Screen.GoalForm.route) }
                )
            }
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = koinViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditProfileClick = { navController.navigate(Screen.ProfileForm.route) },
                    onLogout = onLogout
                )
            }
            composable(Screen.GoalForm.route) {
                val viewModel: GoalFormViewModel = viewModel()
                GoalFormScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.ProfileForm.route) {
                val viewModel: ProfileFormViewModel = viewModel()
                ProfileFormScreen(
                    viewModel = viewModel,
                    onCloseClick = { navController.popBackStack() }
                )
            }

            // --- Original Placeholder Screens ---
            composable(Screen.QuickExpense.route) { PlaceholderScreen(title = "Quick Expense Log") }
            composable(Screen.CatchUp.route) { PlaceholderScreen(title = "Catch-Up Mode") }
            composable(Screen.RecurringManager.route) { PlaceholderScreen(title = "Recurring & Subscriptions") }
            composable(Screen.IncomeHub.route) {
                val viewModel: IncomeViewModel = koinViewModel()
                IncomeDashboardScreen(viewModel, onBack = {
                    if (!navController.popBackStack()) navController.navigate(Screen.Dashboard.route) { launchSingleTop = true }
                })
            }
            composable(Screen.GoalDetail.route) { PlaceholderScreen(title = "MacBook Goal Detail") }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)), // Replaced PureBlack with inline hex to avoid missing import
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = Color(0xFFFFFFFF)) // Replaced PureWhite with inline hex
    }
}
