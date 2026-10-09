package com.sliit.se3092.GanuDenu.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object QuickExpense : Screen("quick_expense")
    object CatchUp : Screen("catch_up")
    object RecurringManager : Screen("recurring_manager")
    object IncomeHub : Screen("income_hub")
    object GoalDetail : Screen("goal_detail")
}