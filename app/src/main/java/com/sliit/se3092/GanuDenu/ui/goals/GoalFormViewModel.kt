package com.sliit.se3092.GanuDenu.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sliit.se3092.GanuDenu.data.local.entity.GoalEntity
import com.sliit.se3092.GanuDenu.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalFormViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalFormUiState())
    val uiState: StateFlow<GoalFormUiState> = _uiState.asStateFlow()

    init {
        loadActiveGoal()
    }

    private fun loadActiveGoal() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val activeGoal = repository.getAllGoals(userId).firstOrNull()?.firstOrNull()

            if (activeGoal != null) {
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                _uiState.update {
                    it.copy(
                        goalName = activeGoal.name,
                        targetAmount = activeGoal.targetAmount.toLong().toString(),
                        currentSaved = activeGoal.currentSaved.toLong().toString(),
                        targetDate = dateFormat.format(Date(activeGoal.targetDate))
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        goalName = "MacBook M4 Pro",
                        targetAmount = "490000",
                        targetDate = "October 30, 2026",
                        currentSaved = "0"
                    )
                }
            }
        }
    }

    fun updateGoalName(name: String) = _uiState.update { it.copy(goalName = name) }
    fun updateTargetAmount(amount: String) = _uiState.update { it.copy(targetAmount = amount) }
    fun updateTargetDate(date: String) = _uiState.update { it.copy(targetDate = date) }
    fun updateCurrentSaved(saved: String) = _uiState.update { it.copy(currentSaved = saved) }

    fun saveGoal() {
        val userId = auth.currentUser?.uid ?: return
        val state = _uiState.value

        viewModelScope.launch {
            // 1. Guarantee we fetch the real ID straight from the DB right now
            val existingGoal = repository.getAllGoals(userId).firstOrNull()?.firstOrNull()
            val goalIdToUse = existingGoal?.goalId ?: UUID.randomUUID().toString()

            // 2. Safely parse the date, falling back to the existing DB date if the user typed gibberish
            val cleanDate = state.targetDate.trim()
            val targetDateMillis = try {
                val format = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                format.isLenient = true
                format.parse(cleanDate)?.time ?: existingGoal?.targetDate ?: System.currentTimeMillis()
            } catch (e: Exception) {
                existingGoal?.targetDate ?: System.currentTimeMillis()
            }

            // 3. Build and save the entity
            val goal = GoalEntity(
                goalId = goalIdToUse, // This guarantees a REPLACE instead of an INSERT
                userId = userId,
                name = state.goalName.trim(),
                targetAmount = state.targetAmount.trim().toDoubleOrNull() ?: existingGoal?.targetAmount ?: 0.0,
                currentSaved = state.currentSaved.trim().toDoubleOrNull() ?: existingGoal?.currentSaved ?: 0.0,
                targetDate = targetDateMillis
            )

            repository.insertOrUpdateGoal(goal)
            _uiState.update { it.copy(showSuccessSnackbar = true) }
            delay(3000)
            _uiState.update { it.copy(showSuccessSnackbar = false) }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(showSuccessSnackbar = false) }
    }
}