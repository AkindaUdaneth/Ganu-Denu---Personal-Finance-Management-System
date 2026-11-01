package com.sliit.se3092.GanuDenu.ui.dashboard

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    object Empty : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
    data class Success(
        val confirmedBalance: Double,
        val estimatedIncome: Double,
        val committedSpent: Double,
        val discretionarySpent: Double,
        val goalName: String,
        val goalCurrentSaved: Double,
        val goalTargetAmount: Double,
        val requiredMonthlySavings: Double,
        val activeSubscriptions: List<SubscriptionItem>
    ) : DashboardUiState()
}

data class SubscriptionItem(
    val name: String,
    val amount: Double,
    val category: String
)