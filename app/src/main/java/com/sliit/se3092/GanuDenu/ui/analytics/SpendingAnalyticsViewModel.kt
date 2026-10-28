package com.sliit.se3092.ganudenu.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpendingAnalyticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SpendingAnalyticsUiState>(SpendingAnalyticsUiState.Loading)
    val uiState: StateFlow<SpendingAnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            _uiState.value = SpendingAnalyticsUiState.Success(
                totalSpent = 54000.0,
                expenses = listOf(
                    ExpenseCategory("Coffee/Dining", 24000.0, 0xFFFFEF00), // Canary Yellow
                    ExpenseCategory("Rent", 15000.0, 0xFF27272A),          // Dark Gray
                    ExpenseCategory("Rides", 12500.0, 0xFF3F3F46),         // Medium Gray
                    ExpenseCategory("Gym", 2500.0, 0xFF71717A)             // Light Gray
                )
            )
        }
    }
}