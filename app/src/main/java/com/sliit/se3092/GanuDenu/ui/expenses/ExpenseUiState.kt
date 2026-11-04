package com.sliit.se3092.GanuDenu.ui.expenses

import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity

data class ExpenseUiState(
    val dailyExpenses: List<ExpenseEntity> = emptyList(),
    val recurringItems: List<RecurringItemEntity> = emptyList(),
    val totalDailyToday: Double = 0.0,
    val totalDailyMonth: Double = 0.0,
    val committedOverheadMonthly: Double = 0.0,
    val currentMonthName: String = "",
    val selectedFilter: String = "All",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)