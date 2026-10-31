package com.sliit.se3092.GanuDenu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sliit.se3092.GanuDenu.data.local.dao.ExpenseDao
import com.sliit.se3092.GanuDenu.data.local.dao.GoalDao
import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.GoalEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity

@Database(
    entities = [ExpenseEntity::class, RecurringItemEntity::class, GoalEntity::class],
    version = 3, // Bumped to trigger migration and build the goals table
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao
}