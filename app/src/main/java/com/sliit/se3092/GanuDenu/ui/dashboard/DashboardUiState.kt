package com.sliit.se3092.GanuDenu.ui.dashboard
data class SubscriptionItem(
    val name: String,
    val costPerMonth: Double,
    val iconType: String
)

// The sealed interface guarantees unidirectional data flow (NFR-MAINT-002)
sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val confirmedBalance: Double,
        val estimatedIncome: Double,
        val committedSpent: Double,
        val discretionarySpent: Double,
        val goalCurrentSaved: Double,
        val goalTargetAmount: Double,
        val requiredMonthlySavings: Double,
        val activeSubscriptions: List<SubscriptionItem>
    ) : DashboardUiState

    data class Error(val message: String) : DashboardUiState

    data object Empty : DashboardUiState // Handles FR-VAL-006 (Empty Dataset Handling)
}