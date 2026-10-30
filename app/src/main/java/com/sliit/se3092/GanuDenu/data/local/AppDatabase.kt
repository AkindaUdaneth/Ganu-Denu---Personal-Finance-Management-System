package com.sliit.se3092.GanuDenu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sliit.se3092.GanuDenu.data.local.dao.ExpenseDao
import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity

@Database(
    entities = [ExpenseEntity::class, RecurringItemEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}