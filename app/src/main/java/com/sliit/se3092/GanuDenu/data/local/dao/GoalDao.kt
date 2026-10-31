package com.sliit.se3092.GanuDenu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sliit.se3092.GanuDenu.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY targetDate ASC")
    fun getAllGoals(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE goalId = :goalId LIMIT 1")
    suspend fun getGoalById(goalId: String): GoalEntity?
}