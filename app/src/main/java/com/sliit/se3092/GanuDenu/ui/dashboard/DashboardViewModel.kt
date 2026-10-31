package com.sliit.se3092.GanuDenu.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sliit.se3092.GanuDenu.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Continuously listen to the Room database
            goalRepository.getAllGoals(userId).collect { goals ->
                val activeGoal = goals.firstOrNull()

                // Dynamically calculate required monthly savings based on target date
                val requiredMonthly = if (activeGoal != null) {
                    val now = Calendar.getInstance().timeInMillis
                    val msDiff = activeGoal.targetDate - now
                    val monthsRemaining = (msDiff / (1000L * 60 * 60 * 24 * 30)).toInt().coerceAtLeast(1)
                    val remainingAmount = activeGoal.targetAmount - activeGoal.currentSaved
                    if (monthsRemaining > 0) remainingAmount / monthsRemaining else 0.0
                } else {
                    0.0
                }

                _uiState.value = DashboardUiState.Success(
                    // Keep dummy data for expenses until we wire up the ExpenseRepository
                    confirmedBalance = 114500.0,
                    estimatedIncome = 135000.0,
                    committedSpent = 31320.0,
                    discretionarySpent = 22680.0,

                    // Dynamically inject the live Room data for the Goal Card
                    goalCurrentSaved = activeGoal?.currentSaved ?: 0.0,
                    goalTargetAmount = activeGoal?.targetAmount ?: 0.0,
                    requiredMonthlySavings = requiredMonthly,

                    activeSubscriptions = listOf(
                        SubscriptionItem("Gym Membership", 2500.0, "GYM"),
                        SubscriptionItem("Gemini Pro", 7000.0, "SOFTWARE"),
                        SubscriptionItem("Uber One", 1500.0, "TRANSPORT")
                    )
                )
            }
        }
    }
}