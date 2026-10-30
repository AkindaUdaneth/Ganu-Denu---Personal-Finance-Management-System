package com.sliit.se3092.GanuDenu.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    // --- Daily Expenses ---
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpenses(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE syncStatus = 'PENDING' AND userId = :userId")
    suspend fun getPendingExpenses(userId: String): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE expenseId = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)

    // --- Recurring Expenses ---
    @Query("SELECT * FROM recurring_items WHERE userId = :userId")
    fun getActiveRecurringItems(userId: String): Flow<List<RecurringItemEntity>>

    @Query("SELECT * FROM recurring_items WHERE syncStatus = 'PENDING' AND userId = :userId")
    suspend fun getPendingRecurringItems(userId: String): List<RecurringItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringItem(item: RecurringItemEntity)

    @Update
    suspend fun updateRecurringItem(item: RecurringItemEntity)
}