package com.sliit.se3092.GanuDenu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val expenseId: String = UUID.randomUUID().toString(),
    val userId: String,
    val amount: Double,
    val date: Long,
    val category: String, // PickMe, UberEats, Coffee/Dining, Rent, Utilities, Gym, Subscriptions
    val paymentMethod: String, // Card, Cash, Secondary Account
    val classification: String, // Committed or Discretionary
    val isRecurringInstance: Boolean = false,
    val recurringItemId: String? = null,
    val notes: String? = null,
    val syncStatus: String = "PENDING" // SYNCED, PENDING, FAILED
)