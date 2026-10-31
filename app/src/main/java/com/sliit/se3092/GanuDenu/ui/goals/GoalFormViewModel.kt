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

    // Hold onto the ID so we update the correct row!
    private var currentGoalId: String = UUID.randomUUID().toString()

    init {
        loadActiveGoal()
    }

    private fun loadActiveGoal() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Fetch the existing goal from Room to populate the form
            val activeGoalList = repository.getAllGoals(userId).firstOrNull()
            val activeGoal = activeGoalList?.firstOrNull()

            if (activeGoal != null) {
                currentGoalId = activeGoal.goalId // Lock in the existing ID
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
                // Fallback initial state if the database is completely empty
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

        val targetDateMillis = try {
            SimpleDateFormat("MMMM dd, yyyy", Locale.US).parse(state.targetDate)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        // By passing the currentGoalId, Room knows to REPLACE the existing row
        val goal = GoalEntity(
            goalId = currentGoalId,
            userId = userId,
            name = state.goalName,
            targetAmount = state.targetAmount.toDoubleOrNull() ?: 0.0,
            currentSaved = state.currentSaved.toDoubleOrNull() ?: 0.0,
            targetDate = targetDateMillis
        )

        viewModelScope.launch {
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