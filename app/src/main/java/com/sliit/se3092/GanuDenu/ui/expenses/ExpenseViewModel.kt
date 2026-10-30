package com.sliit.se3092.GanuDenu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.google.firebase.auth.FirebaseAuth
import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity
import com.sliit.se3092.GanuDenu.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState(isLoading = true))
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val currentUserId: String = "user_default"

    init {
        observeExpenseData()
    }

    private fun observeExpenseData() {
        viewModelScope.launch {
            combine(
                repository.getAllExpenses(currentUserId),
                repository.getActiveRecurringItems(currentUserId)
            ) { expenses, recurring ->
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentDay = calendar.get(Calendar.DAY_OF_YEAR)

                var todaySum = 0.0
                var monthSum = 0.0

                expenses.forEach { item ->
                    val itemCal = Calendar.getInstance().apply { timeInMillis = item.date }
                    if (itemCal.get(Calendar.YEAR) == currentYear && itemCal.get(Calendar.MONTH) == currentMonth) {
                        monthSum += item.amount
                        if (itemCal.get(Calendar.DAY_OF_YEAR) == currentDay) {
                            todaySum += item.amount
                        }
                    }
                }

                val overhead = recurring.filter { it.isActive }.sumOf { it.amount }

                ExpenseUiState(
                    dailyExpenses = expenses.sortedByDescending { it.date },
                    recurringItems = recurring,
                    totalDailyToday = todaySum,
                    totalDailyMonth = monthSum,
                    committedOverheadMonthly = overhead,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addDailyExpense(
        amount: Double,
        category: String,
        paymentMethod: String,
        date: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = ExpenseEntity(
                userId = currentUserId,
                amount = amount,
                category = category,
                paymentMethod = paymentMethod,
                classification = "Discretionary", // Pass "Discretionary" string instead of isDiscretionary boolean
                date = date
            )
            repository.insertExpense(entity)
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
        viewModelScope.launch {
            val item = RecurringItemEntity(
                userId = currentUserId,
                name = title,
                amount = amount,
                category = "Subscription",
                classification = "Committed",
                cadence = "Monthly",
                nextDueDate = System.currentTimeMillis(), // or dueDay.toLong() depending on whether it's Long or String
                isActive = true                          // uses 'isActive' instead of 'active'
            )
            repository.insertRecurringItem(item)
            onSuccess()
        }
    }

    fun toggleRecurringStatus(item: RecurringItemEntity) {
        viewModelScope.launch {
            repository.updateRecurringItem(item.copy(isActive = !item.isActive))
        }
    }
}