package com.sliit.se3092.GanuDenu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity
import com.sliit.se3092.GanuDenu.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState(isLoading = true))
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadExpenseData()
    }

    fun loadExpenseData() {
        val currentUserId = auth.currentUser?.uid.orEmpty()

        if (currentUserId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "User not authenticated"
            )
            return
        }

        viewModelScope.launch {
            repository.syncPendingExpenses(currentUserId)
            repository.fetchRemoteExpenses(currentUserId)
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            combine(
                repository.getAllExpenses(currentUserId),
                repository.getActiveRecurringItems(currentUserId)
            ) { expenses, recurringItems ->
                calculateUiState(expenses, recurringItems)
            }.catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.localizedMessage ?: "Failed to load expenses"
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun calculateUiState(
        expenses: List<ExpenseEntity>,
        recurringItems: List<RecurringItemEntity>
    ): ExpenseUiState {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH)
        val currentDay = now.get(Calendar.DAY_OF_YEAR)

        val monthFormat = java.text.SimpleDateFormat("MMMM", Locale.getDefault())
        val currentMonthName = monthFormat.format(now.time)

        var todaySum = 0.0
        var monthSum = 0.0

        val itemCal = Calendar.getInstance()

        // 1. Calculate Daily Totals
        for (expense in expenses) {
            itemCal.timeInMillis = expense.date
            if (itemCal.get(Calendar.YEAR) == currentYear) {
                if (itemCal.get(Calendar.MONTH) == currentMonth) {
                    monthSum += expense.amount
                }
                if (itemCal.get(Calendar.DAY_OF_YEAR) == currentDay) {
                    todaySum += expense.amount
                }
            }
        }

        // 2. Calculate Committed Monthly Overhead
        var overheadMonthlySum = 0.0
        for (recurring in recurringItems) {
            if (recurring.isActive) {
                overheadMonthlySum += when (recurring.cadence.lowercase(Locale.ROOT)) {
                    "daily" -> recurring.amount * 30.0
                    "weekly" -> recurring.amount * 4.33
                    "yearly", "annually" -> recurring.amount / 12.0
                    else -> recurring.amount // Default: Monthly
                }
            }
        }

        return _uiState.value.copy(
            dailyExpenses = expenses,
            recurringItems = recurringItems,
            totalDailyToday = todaySum,
            totalDailyMonth = monthSum,
            committedOverheadMonthly = overheadMonthlySum,
            currentMonthName = currentMonthName,
            isLoading = false,
            errorMessage = null
        )
    }

    fun updateFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun addDailyExpense(
        amount: Double,
        category: String,
        paymentMethod: String,
        date: Long,
        onSuccess: () -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid.orEmpty()
        viewModelScope.launch {
            val expense = ExpenseEntity(
                userId = currentUserId,
                amount = amount,
                date = date,
                category = category,
                paymentMethod = paymentMethod,
                classification = "Discretionary"
            )
            repository.insertExpense(expense)
            onSuccess()
        }
    }

    fun addRecurringExpense(
        title: String,
        amount: Double,
        dueDay: Int,
        paymentMethod: String,
        onSuccess: () -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid.orEmpty()
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val validDay = dueDay.coerceIn(1, 31)
            val maxDayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            calendar.set(Calendar.DAY_OF_MONTH, validDay.coerceAtMost(maxDayInMonth))
            val recurringItem = RecurringItemEntity(
                userId = currentUserId,
                name = title,
                amount = amount,
                category = paymentMethod,
                classification = "Committed",
                cadence = "Monthly",
                nextDueDate = calendar.timeInMillis,
                isActive = true
            )
            repository.insertRecurringItem(recurringItem)
            onSuccess()
        }
    }

    fun toggleRecurringStatus(item: RecurringItemEntity) {
        viewModelScope.launch {
            val updated = item.copy(isActive = !item.isActive)
            repository.updateRecurringItem(updated)
        }
    }
}