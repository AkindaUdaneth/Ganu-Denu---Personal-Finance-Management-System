package com.sliit.se3092.GanuDenu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val goalId: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val targetAmount: Double,
    val currentSaved: Double = 0.0,
    val targetDate: Long,
    val syncStatus: String = "PENDING"
)