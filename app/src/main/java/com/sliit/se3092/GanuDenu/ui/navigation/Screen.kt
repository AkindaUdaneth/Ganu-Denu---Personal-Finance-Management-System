package com.sliit.se3092.GanuDenu.ui.navigation

sealed class Screen(val route: String) {
    // The real screens
    data object Dashboard : Screen("dashboard")
    data object Analytics : Screen("analytics")
    data object GoalEngine : Screen("goal_engine")
    data object Settings : Screen("settings")
    data object GoalForm : Screen("goal_form")
    data object ProfileForm : Screen("profile_form")

    // Expense Hub & Sub-routes
    data object ExpenseHub : Screen("expense_hub")
    data object DailyExpenses : Screen("daily_expenses")
    data object DailyExpenseLogger : Screen("daily_expense_logger")
    data object RecurringExpenses : Screen("recurring_expenses")
    data object RecurringExpenseLogger : Screen("recurring_expense_logger")

    // The original placeholders to prevent broken links
    data object QuickExpense : Screen("quick_expense")
    data object CatchUp : Screen("catch_up")
    data object RecurringManager : Screen("recurring_manager")
    data object IncomeHub : Screen("income_hub")
    data object AddIncome : Screen("add_income")
    data object GoalDetail : Screen("goal_detail")
}
