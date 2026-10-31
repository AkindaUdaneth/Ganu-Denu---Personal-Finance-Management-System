package com.sliit.se3092.GanuDenu.data.repository

import com.sliit.se3092.GanuDenu.data.local.dao.GoalDao
import com.sliit.se3092.GanuDenu.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    fun getAllGoals(userId: String): Flow<List<GoalEntity>> {
        return goalDao.getAllGoals(userId)
    }

    suspend fun getGoalById(goalId: String): GoalEntity? {
        return goalDao.getGoalById(goalId)
    }

    suspend fun insertOrUpdateGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
    }
}