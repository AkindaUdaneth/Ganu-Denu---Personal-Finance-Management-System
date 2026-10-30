package com.sliit.se3092.ganudenu.expenses

import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseRepositoryTest {

    @Test
    fun newExpenseEntityDefaultsToPendingSyncStatus() {
        val expense = ExpenseEntity(
            userId = "user_123",
            amount = 1500.0,
            date = System.currentTimeMillis(),
            category = "UberEats",
            paymentMethod = "Cash",
            classification = "Discretionary"
        )

        assertEquals("PENDING", expense.syncStatus)
    }

    @Test
    fun updatedExpenseEntitySyncStatusReflectsSynced() {
        val expense = ExpenseEntity(
            userId = "user_123",
            amount = 1500.0,
            date = System.currentTimeMillis(),
            category = "UberEats",
            paymentMethod = "Cash",
            classification = "Discretionary",
            syncStatus = "PENDING"
        )

        val syncedExpense = expense.copy(syncStatus = "SYNCED")

        assertEquals("SYNCED", syncedExpense.syncStatus)
    }

    @Test
    fun newRecurringItemEntityDefaultsToPendingSyncStatus() {
        val recurringItem = RecurringItemEntity(
            userId = "user_123",
            name = "Gym Membership",
            amount = 4500.0,
            category = "Health",
            nextDueDate = System.currentTimeMillis()
        )

        assertEquals("PENDING", recurringItem.syncStatus)
    }
}
