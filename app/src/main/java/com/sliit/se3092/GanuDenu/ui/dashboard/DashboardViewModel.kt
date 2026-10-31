package com.sliit.se3092.GanuDenu.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)

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