package com.sliit.se3092.GanuDenu.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sliit.se3092.GanuDenu.data.local.dao.GoalDao
import com.sliit.se3092.GanuDenu.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val firestore: FirebaseFirestore
) {
    fun getAllGoals(userId: String): Flow<List<GoalEntity>> {
        return goalDao.getAllGoals(userId)
    }

    suspend fun getGoalById(goalId: String): GoalEntity? {
        return goalDao.getGoalById(goalId)
    }

    suspend fun insertOrUpdateGoal(goal: GoalEntity) {
        // 1. Save locally to Room
        goalDao.insertGoal(goal)

        // 2. Sync to Firestore in the background
        if (goal.userId.isBlank()) return
            val goalData = hashMapOf(
                "goalId" to goal.goalId,
                "userId" to goal.userId,
                "name" to goal.name,
                "targetAmount" to goal.targetAmount,
                "currentSaved" to goal.currentSaved,
                "targetDate" to goal.targetDate
            )
            firestore.collection("users")
                .document(goal.userId)
                .collection("goals")
                .document(goal.goalId)
                .set(goalData, SetOptions.merge())
                .await()
    }

    suspend fun fetchRemoteGoals(userId: String) {
        if (userId.isBlank()) return
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("goals")
                .get()
                .await()

            for (doc in snapshot.documents) {
                val entity = GoalEntity(
                    goalId = doc.getString("goalId") ?: doc.id,
                    userId = userId,
                    name = doc.getString("name") ?: "",
                    targetAmount = (doc.get("targetAmount") as? Number)?.toDouble() ?: 0.0,
                    currentSaved = (doc.get("currentSaved") as? Number)?.toDouble() ?: 0.0,
                    targetDate = (doc.get("targetDate") as? Number)?.toLong() ?: System.currentTimeMillis()
                )
                goalDao.insertGoal(entity)
            }
        } catch (e: Exception) {
        }
    }
}