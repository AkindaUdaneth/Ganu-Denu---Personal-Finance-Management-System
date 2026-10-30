package com.sliit.se3092.GanuDenu.data.repository

import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(userId: String): Flow<List<ExpenseEntity>>
    suspend fun insertExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)

    fun getActiveRecurringItems(userId: String): Flow<List<RecurringItemEntity>>
    suspend fun insertRecurringItem(item: RecurringItemEntity)
    suspend fun updateRecurringItem(item: RecurringItemEntity)

    suspend fun syncPendingExpenses(userId: String)
    suspend fun fetchRemoteExpenses(userId: String)
}