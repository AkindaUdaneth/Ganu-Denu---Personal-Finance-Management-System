package com.sliit.se3092.ganudenu.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalEngineViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<GoalEngineUiState>(GoalEngineUiState.Loading)
    val uiState: StateFlow<GoalEngineUiState> = _uiState.asStateFlow()

    init {
        loadGoalData()
    }

    private fun loadGoalData() {
        viewModelScope.launch {
            _uiState.value = GoalEngineUiState.Success(
                goalName = "MacBook Pro M4",
                currentSaved = 11200.0,
                targetAmount = 490000.0,
                targetDateMonths = 12,
                requiredMonthlySavings = 39900.0,
                progressPercentage = 2.3,
                impulseExpenseInput = "24000",
                impulseDelayDays = 18
            )
        }
    }

    fun onImpulseExpenseChanged(expense: String) {
        val currentState = _uiState.value
        if (currentState is GoalEngineUiState.Success) {
            val expenseValue = expense.toDoubleOrNull()

            val delayDays = if (expenseValue != null && expenseValue > 0 && currentState.requiredMonthlySavings > 0) {
                ((expenseValue / currentState.requiredMonthlySavings) * 30).toInt()
            } else {
                null
            }

            _uiState.update {
                currentState.copy(
                    impulseExpenseInput = expense,
                    impulseDelayDays = delayDays
                )
            }
        }
    }
}