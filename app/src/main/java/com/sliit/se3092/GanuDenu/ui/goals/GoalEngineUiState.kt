package com.sliit.se3092.GanuDenu.ui.goals

sealed interface GoalEngineUiState {
    data object Loading : GoalEngineUiState

    data class Success(
        val goalName: String,
        val currentSaved: Double,
        val targetAmount: Double,
        val targetDateMonths: Int,
        val requiredMonthlySavings: Double,
        val progressPercentage: Double,
        val impulseExpenseInput: String,
        val impulseDelayDays: Int?
    ) : GoalEngineUiState

    data class Error(val message: String) : GoalEngineUiState
}