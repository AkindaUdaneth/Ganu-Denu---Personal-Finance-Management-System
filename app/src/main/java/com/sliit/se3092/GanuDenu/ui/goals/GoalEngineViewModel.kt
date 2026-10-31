package com.sliit.se3092.GanuDenu.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sliit.se3092.GanuDenu.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class GoalEngineViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<GoalEngineUiState>(GoalEngineUiState.Loading)
    val uiState: StateFlow<GoalEngineUiState> = _uiState.asStateFlow()

    init {
        loadGoalData()
    }

    private fun loadGoalData() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            repository.getAllGoals(userId)
                .catch { /* Silently handle empty DB states */ }
                .collect { goals ->
                    val activeGoal = goals.firstOrNull()

                    if (activeGoal != null) {
                        val now = Calendar.getInstance().timeInMillis
                        val msDiff = activeGoal.targetDate - now
                        val monthsRemaining = (msDiff / (1000L * 60 * 60 * 24 * 30)).toInt().coerceAtLeast(1)

                        val remainingAmount = activeGoal.targetAmount - activeGoal.currentSaved
                        val requiredMonthly = if (monthsRemaining > 0) remainingAmount / monthsRemaining else 0.0
                        val progress = if (activeGoal.targetAmount > 0) (activeGoal.currentSaved / activeGoal.targetAmount) * 100 else 0.0

                        val currentImpulse = (_uiState.value as? GoalEngineUiState.Success)?.impulseExpenseInput ?: ""
                        val currentDelay = (_uiState.value as? GoalEngineUiState.Success)?.impulseDelayDays

                        _uiState.value = GoalEngineUiState.Success(
                            goalName = activeGoal.name,
                            currentSaved = activeGoal.currentSaved,
                            targetAmount = activeGoal.targetAmount,
                            targetDateMonths = monthsRemaining,
                            requiredMonthlySavings = requiredMonthly,
                            progressPercentage = String.format(java.util.Locale.US, "%.1f", progress).toDouble(),
                            impulseExpenseInput = currentImpulse,
                            impulseDelayDays = currentDelay
                        )
                    } else {
                        // Safe fallback if the database is empty
                        _uiState.value = GoalEngineUiState.Success(
                            goalName = "No Active Goal",
                            currentSaved = 0.0,
                            targetAmount = 0.0,
                            targetDateMonths = 0,
                            requiredMonthlySavings = 0.0,
                            progressPercentage = 0.0,
                            impulseExpenseInput = "",
                            impulseDelayDays = null
                        )
                    }
                }
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