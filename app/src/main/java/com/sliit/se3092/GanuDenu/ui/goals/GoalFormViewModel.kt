package com.sliit.se3092.GanuDenu.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalFormViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GoalFormUiState())
    val uiState: StateFlow<GoalFormUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = GoalFormUiState(
            goalName = "MacBook M4 Pro",
            targetAmount = "490000",
            targetDate = "October 30, 2026",
            currentSaved = "11200"
        )
    }

    fun updateGoalName(name: String) = _uiState.update { it.copy(goalName = name) }
    fun updateTargetAmount(amount: String) = _uiState.update { it.copy(targetAmount = amount) }
    fun updateTargetDate(date: String) = _uiState.update { it.copy(targetDate = date) }
    fun updateCurrentSaved(saved: String) = _uiState.update { it.copy(currentSaved = saved) }

    fun saveGoal() {
        viewModelScope.launch {
            _uiState.update { it.copy(showSuccessSnackbar = true) }

            delay(3000)
            _uiState.update { it.copy(showSuccessSnackbar = false) }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(showSuccessSnackbar = false) }
    }
}