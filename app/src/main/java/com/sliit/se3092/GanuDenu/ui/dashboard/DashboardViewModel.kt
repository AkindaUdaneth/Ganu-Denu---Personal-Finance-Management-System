package com.sliit.se3092.GanuDenu.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sliit.se3092.GanuDenu.data.income.IncomeRepository
import com.sliit.se3092.GanuDenu.data.income.IncomeStatus
import com.sliit.se3092.GanuDenu.data.repository.ExpenseRepository
import com.sliit.se3092.GanuDenu.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return
        val currentMonth = YearMonth.now()

        viewModelScope.launch {
            // Fetch remote data to ensure Room is up-to-date
            expenseRepository.fetchRemoteExpenses(userId)

            // Combine Goal, Expense, Recurring, and Income flows
            combine(
                goalRepository.getAllGoals(userId),
                expenseRepository.getAllExpenses(userId),
                expenseRepository.getActiveRecurringItems(userId),
                incomeRepository.observeMonth(currentMonth)
            ) { goals, expenses, recurringItems, incomeSnapshot ->

                // 1. Process Goal Math
                val activeGoal = goals.firstOrNull()
                val requiredMonthly = if (activeGoal != null) {
                    val now = Calendar.getInstance().timeInMillis
                    val msDiff = activeGoal.targetDate - now
                    val monthsRemaining = (msDiff / (1000L * 60 * 60 * 24 * 30)).toInt().coerceAtLeast(1)
                    val remainingAmount = activeGoal.targetAmount - activeGoal.currentSaved
                    if (monthsRemaining > 0) remainingAmount / monthsRemaining else 0.0
                } else {
                    0.0
                }

                // 2. Process Expenses
                // Filter for current month's expenses
                val monthStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                val currentMonthExpenses = expenses.filter { it.date >= monthStart }
                val committedSpent = currentMonthExpenses.filter { it.classification == "Committed" }.sumOf { it.amount }
                val discretionarySpent = currentMonthExpenses.filter { it.classification == "Discretionary" }.sumOf { it.amount }

                // 3. Process Income (Minor units to Major units conversion)
                val confirmedIncome = incomeSnapshot.entries
                    .filter { it.status == IncomeStatus.CONFIRMED }
                    .sumOf { it.amountLkrMinor / 100.0 }

                val estimatedIncome = incomeSnapshot.entries
                    .filter { it.status == IncomeStatus.ESTIMATED }
                    .sumOf { it.amountLkrMinor / 100.0 }

                // 4. Calculate Current Balance
                val totalSpent = committedSpent + discretionarySpent
                val currentBalance = confirmedIncome - totalSpent

                // 5. Map Recurring Items to UI Subscriptions
                val activeSubscriptions = recurringItems
                    .filter { it.isActive }
                    .map {
                        SubscriptionItem(
                            name = it.name,
                            amount = it.amount,
                            category = it.category
                        )
                    }

                // Push to UI State
                DashboardUiState.Success(
                    confirmedBalance = currentBalance,
                    estimatedIncome = estimatedIncome,
                    committedSpent = committedSpent,
                    discretionarySpent = discretionarySpent,
                    goalName = activeGoal?.name ?: "Set a Goal",
                    goalCurrentSaved = activeGoal?.currentSaved ?: 0.0,
                    goalTargetAmount = activeGoal?.targetAmount ?: 0.0,
                    requiredMonthlySavings = requiredMonthly,
                    activeSubscriptions = activeSubscriptions
                )
            }
                .catch { e ->
                    e.printStackTrace()
                    // Fail gracefully, keep UI loading or handle error
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }
}