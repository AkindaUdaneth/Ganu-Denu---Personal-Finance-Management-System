package com.sliit.se3092.GanuDenu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recurring_items")
data class RecurringItemEntity(
    @PrimaryKey
    val recurringItemId: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String, // e.g., Rent, Gym, Subscriptions
    val amount: Double,
    val category: String,
    val classification: String = "Committed",
    val cadence: String = "Monthly",
    val nextDueDate: Long,
    val isActive: Boolean = true,
    val syncStatus: String = "PENDING" // SYNCED, PENDING, FAILED
)