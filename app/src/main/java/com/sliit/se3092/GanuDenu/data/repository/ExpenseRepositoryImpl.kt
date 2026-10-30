package com.sliit.se3092.GanuDenu.data.repository

import com.sliit.se3092.GanuDenu.data.local.dao.ExpenseDao
import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(userId: String): Flow<List<ExpenseEntity>> =
        expenseDao.getAllExpenses(userId)

    override suspend fun insertExpense(expense: ExpenseEntity) =
        expenseDao.insertExpense(expense)

    override suspend fun deleteExpense(expense: ExpenseEntity) =
        expenseDao.deleteExpense(expense)

    override fun getActiveRecurringItems(userId: String): Flow<List<RecurringItemEntity>> =
        expenseDao.getActiveRecurringItems(userId)

    override suspend fun insertRecurringItem(item: RecurringItemEntity) =
        expenseDao.insertRecurringItem(item)

    override suspend fun updateRecurringItem(item: RecurringItemEntity) =
        expenseDao.updateRecurringItem(item)
}