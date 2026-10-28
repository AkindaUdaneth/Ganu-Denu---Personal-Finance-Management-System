package com.sliit.se3092.GanuDenu.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    // Internal mutable state - hidden from the UI layer
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)

    // Public immutable state - the Compose screen will observe this
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Success(
                confirmedBalance = 114500.0,
                estimatedIncome = 135000.0,
                committedSpent = 31320.0,
                discretionarySpent = 22680.0,
                goalCurrentSaved = 11200.0,
                goalTargetAmount = 49000.0,
                requiredMonthlySavings = 39900.0,
                activeSubscriptions = listOf(
                    SubscriptionItem("Gym Membership", 2500.0, "GYM"),
                    SubscriptionItem("Gemini Pro", 7000.0, "SOFTWARE"),
                    SubscriptionItem("Uber One", 1500.0, "TRANSPORT")
                )
            )
        }
    }
}