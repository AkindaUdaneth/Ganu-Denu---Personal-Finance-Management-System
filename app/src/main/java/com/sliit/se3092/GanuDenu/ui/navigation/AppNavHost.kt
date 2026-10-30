package com.sliit.se3092.GanuDenu.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import org.koin.androidx.compose.koinViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.sliit.se3092.GanuDenu.ui.analytics.SpendingAnalyticsScreen
import com.sliit.se3092.GanuDenu.ui.analytics.SpendingAnalyticsViewModel
import com.sliit.se3092.GanuDenu.ui.dashboard.DashboardScreen
import com.sliit.se3092.GanuDenu.ui.dashboard.DashboardViewModel
import com.sliit.se3092.GanuDenu.ui.expenses.AddRecurringExpenseScreen
import com.sliit.se3092.GanuDenu.ui.expenses.DailyExpenseLoggerScreen
import com.sliit.se3092.GanuDenu.ui.expenses.DailyExpensesManagerScreen
import com.sliit.se3092.GanuDenu.ui.goals.GoalEngineScreen
import com.sliit.se3092.GanuDenu.ui.goals.GoalEngineViewModel
import com.sliit.se3092.GanuDenu.ui.goals.GoalFormScreen
import com.sliit.se3092.GanuDenu.ui.goals.GoalFormViewModel
import com.sliit.se3092.GanuDenu.ui.income.AddIncomeScreen
import com.sliit.se3092.GanuDenu.ui.income.IncomeDashboardScreen
import com.sliit.se3092.GanuDenu.ui.income.IncomeViewModel
import com.sliit.se3092.GanuDenu.ui.settings.ProfileFormScreen
import com.sliit.se3092.GanuDenu.ui.settings.ProfileFormViewModel
import com.sliit.se3092.GanuDenu.ui.settings.SettingsScreen
import com.sliit.se3092.GanuDenu.ui.settings.SettingsViewModel
import com.sliit.se3092.GanuDenu.ui.expenses.ExpensesHubScreen
import com.sliit.se3092.GanuDenu.ui.expenses.ExpenseViewModel
import com.sliit.se3092.GanuDenu.ui.expenses.RecurringExpenseScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Display the Bottom Navigation on main anchor screens
    val showBottomNav = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.GoalEngine.route,
        Screen.Analytics.route,
        Screen.IncomeHub.route,
        Screen.ExpenseHub.route,
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
            // --- Dashboard & Analytics ---
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

            // --- Goal Engine ---
            composable(Screen.GoalEngine.route) {
                val viewModel: GoalEngineViewModel = viewModel()
                GoalEngineScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate(Screen.GoalForm.route) }
                )
            }
            composable(Screen.GoalForm.route) {
                val viewModel: GoalFormViewModel = viewModel()
                GoalFormScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.GoalDetail.route) { PlaceholderScreen(title = "MacBook Goal Detail") }

            // --- Settings & Profile ---
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = koinViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditProfileClick = { navController.navigate(Screen.ProfileForm.route) },
                    onLogout = onLogout
                )
            }
            composable(Screen.ProfileForm.route) {
                val viewModel: ProfileFormViewModel = viewModel()
                ProfileFormScreen(
                    viewModel = viewModel,
                    onCloseClick = { navController.popBackStack() }
                )
            }

            // --- Income Flow ---
            composable(Screen.IncomeHub.route) {
                val incomeViewModel: IncomeViewModel = koinViewModel()

                IncomeDashboardScreen(
                    incomeViewModel,
                    onBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Screen.Dashboard.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onLogIncome = {
                        incomeViewModel.openLogger()
                        navController.navigate(Screen.AddIncome.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.AddIncome.route) { entry ->
                val incomeEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.IncomeHub.route)
                }

                val incomeViewModel: IncomeViewModel = koinViewModel(
                    viewModelStoreOwner = incomeEntry
                )

                AddIncomeScreen(
                    incomeViewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            // --- Expense Flow ---
            composable(Screen.ExpenseHub.route) {
                val expenseViewModel: ExpenseViewModel = hiltViewModel()
                ExpensesHubScreen(
                    viewModel = expenseViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDailyManager = { navController.navigate(Screen.DailyExpenses.route) },
                    onNavigateToRecurringManager = { navController.navigate(Screen.RecurringExpenses.route) }
                )
            }

            // 1. Daily Expenses Manager Screen
            composable(Screen.DailyExpenses.route) {
                val expenseViewModel: ExpenseViewModel = hiltViewModel()
                DailyExpensesManagerScreen(
                    viewModel = expenseViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogger = { navController.navigate(Screen.DailyExpenseLogger.route) }
                )
            }

            // 2. Daily Expense Logger Screen
            composable(Screen.DailyExpenseLogger.route) {
                val expenseViewModel: ExpenseViewModel = hiltViewModel()
                DailyExpenseLoggerScreen(
                    viewModel = expenseViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 3. Recurring Expenses Manager Screen
            composable(Screen.RecurringExpenses.route) {
                val expenseViewModel: ExpenseViewModel = hiltViewModel()
                RecurringExpenseScreen(
                    viewModel = expenseViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddRecurring = { navController.navigate(Screen.RecurringExpenseLogger.route) }
                )
            }

            // 4. Add Recurring Expense Screen
            composable(Screen.RecurringExpenseLogger.route) {
                val expenseViewModel: ExpenseViewModel = hiltViewModel()
                AddRecurringExpenseScreen(
                    viewModel = expenseViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- Legacy Placeholders ---
            composable(Screen.QuickExpense.route) { PlaceholderScreen(title = "Quick Expense Log") }
            composable(Screen.CatchUp.route) { PlaceholderScreen(title = "Catch-Up Mode") }
            composable(Screen.RecurringManager.route) { PlaceholderScreen(title = "Recurring & Subscriptions") }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = Color(0xFFFFFFFF))
    }
}