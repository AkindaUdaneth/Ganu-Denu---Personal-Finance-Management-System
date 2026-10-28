package com.sliit.se3092.GanuDenu.ui.analytics

data class ExpenseCategory(
    val name: String,
    val amount: Double,
    val colorHex: Long
)

sealed interface SpendingAnalyticsUiState {
    data object Loading : SpendingAnalyticsUiState

    data class Success(
        val totalSpent: Double,
        val expenses: List<ExpenseCategory>
    ) : SpendingAnalyticsUiState

    data class Error(val message: String) : SpendingAnalyticsUiState
}