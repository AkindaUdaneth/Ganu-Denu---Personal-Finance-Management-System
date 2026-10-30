package com.sliit.se3092.GanuDenu.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sliit.se3092.GanuDenu.data.local.dao.ExpenseDao
import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    override fun getAllExpenses(userId: String): Flow<List<ExpenseEntity>> =
        expenseDao.getAllExpenses(userId)

    override suspend fun insertExpense(expense: ExpenseEntity) {
        val pendingExpense = expense.copy(syncStatus = "PENDING")
        expenseDao.insertExpense(pendingExpense)

        if (pendingExpense.userId.isBlank()) return

        try {
            val docData = hashMapOf(
                "expenseId" to pendingExpense.expenseId,
                "userId" to pendingExpense.userId,
                "amount" to pendingExpense.amount,
                "date" to pendingExpense.date,
                "category" to pendingExpense.category,
                "paymentMethod" to pendingExpense.paymentMethod,
                "classification" to pendingExpense.classification,
                "isRecurringInstance" to pendingExpense.isRecurringInstance,
                "recurringItemId" to pendingExpense.recurringItemId,
                "notes" to pendingExpense.notes
            )
            firestore.collection("users")
                .document(pendingExpense.userId)
                .collection("expenses")
                .document(pendingExpense.expenseId)
                .set(docData)
                .await()

            expenseDao.insertExpense(pendingExpense.copy(syncStatus = "SYNCED"))
        } catch (_: Exception) {
            // Retains PENDING status in Room for offline retry
        }
    }

    override suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)

        if (expense.userId.isBlank()) return

        try {
            firestore.collection("users")
                .document(expense.userId)
                .collection("expenses")
                .document(expense.expenseId)
                .delete()
                .await()
        } catch (_: Exception) {
            // Ignore offline deletion errors
        }
    }

    override fun getActiveRecurringItems(userId: String): Flow<List<RecurringItemEntity>> =
        expenseDao.getActiveRecurringItems(userId)

    override suspend fun insertRecurringItem(item: RecurringItemEntity) {
        val pendingItem = item.copy(syncStatus = "PENDING")
        expenseDao.insertRecurringItem(pendingItem)

        if (pendingItem.userId.isBlank()) return

        try {
            val docData = hashMapOf(
                "recurringItemId" to pendingItem.recurringItemId,
                "userId" to pendingItem.userId,
                "name" to pendingItem.name,
                "amount" to pendingItem.amount,
                "category" to pendingItem.category,
                "classification" to pendingItem.classification,
                "cadence" to pendingItem.cadence,
                "nextDueDate" to pendingItem.nextDueDate,
                "isActive" to pendingItem.isActive
            )
            firestore.collection("users")
                .document(pendingItem.userId)
                .collection("recurringItems")
                .document(pendingItem.recurringItemId)
                .set(docData)
                .await()

            expenseDao.insertRecurringItem(pendingItem.copy(syncStatus = "SYNCED"))
        } catch (_: Exception) {
            // Retains PENDING status in Room
        }
    }

    override suspend fun updateRecurringItem(item: RecurringItemEntity) {
        insertRecurringItem(item)
    }

    override suspend fun syncPendingExpenses(userId: String) {
        if (userId.isBlank()) return

        val pendingExpenses = expenseDao.getPendingExpenses(userId)
        for (pending in pendingExpenses) {
            try {
                val docData = hashMapOf(
                    "expenseId" to pending.expenseId,
                    "userId" to pending.userId,
                    "amount" to pending.amount,
                    "date" to pending.date,
                    "category" to pending.category,
                    "paymentMethod" to pending.paymentMethod,
                    "classification" to pending.classification,
                    "isRecurringInstance" to pending.isRecurringInstance,
                    "recurringItemId" to pending.recurringItemId,
                    "notes" to pending.notes
                )
                firestore.collection("users")
                    .document(pending.userId)
                    .collection("expenses")
                    .document(pending.expenseId)
                    .set(docData)
                    .await()

                expenseDao.insertExpense(pending.copy(syncStatus = "SYNCED"))
            } catch (_: Exception) {
                // Ignore, will retry next sync
            }
        }

        val pendingRecurring = expenseDao.getPendingRecurringItems(userId)
        for (pending in pendingRecurring) {
            try {
                val docData = hashMapOf(
                    "recurringItemId" to pending.recurringItemId,
                    "userId" to pending.userId,
                    "name" to pending.name,
                    "amount" to pending.amount,
                    "category" to pending.category,
                    "classification" to pending.classification,
                    "cadence" to pending.cadence,
                    "nextDueDate" to pending.nextDueDate,
                    "isActive" to pending.isActive
                )
                firestore.collection("users")
                    .document(pending.userId)
                    .collection("recurringItems")
                    .document(pending.recurringItemId)
                    .set(docData)
                    .await()

                expenseDao.insertRecurringItem(pending.copy(syncStatus = "SYNCED"))
            } catch (_: Exception) {
                // Ignore, will retry next sync
            }
        }
    }

    override suspend fun fetchRemoteExpenses(userId: String) {
        if (userId.isBlank()) return

        try {
            val expensesSnapshot = firestore.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .await()

            for (doc in expensesSnapshot.documents) {
                val expenseId = doc.getString("expenseId") ?: doc.id
                val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
                val date = when (val rawDate = doc.get("date")) {
                    is Number -> rawDate.toLong()
                    is com.google.firebase.Timestamp -> rawDate.toDate().time
                    else -> System.currentTimeMillis()
                }
                val category = doc.getString("category") ?: "Other"
                val paymentMethod = doc.getString("paymentMethod") ?: "Cash"
                val classification = doc.getString("classification") ?: "Discretionary"
                val isRecurringInstance = doc.getBoolean("isRecurringInstance") ?: false
                val recurringItemId = doc.getString("recurringItemId")
                val notes = doc.getString("notes")

                val entity = ExpenseEntity(
                    expenseId = expenseId,
                    userId = userId,
                    amount = amount,
                    date = date,
                    category = category,
                    paymentMethod = paymentMethod,
                    classification = classification,
                    isRecurringInstance = isRecurringInstance,
                    recurringItemId = recurringItemId,
                    notes = notes,
                    syncStatus = "SYNCED"
                )
                expenseDao.insertExpense(entity)
            }

            val recurringSnapshot = firestore.collection("users")
                .document(userId)
                .collection("recurringItems")
                .get()
                .await()

            for (doc in recurringSnapshot.documents) {
                val itemId = doc.getString("recurringItemId") ?: doc.id
                val name = doc.getString("name") ?: ""
                val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
                val category = doc.getString("category") ?: ""
                val classification = doc.getString("classification") ?: "Committed"
                val cadence = doc.getString("cadence") ?: "Monthly"
                val nextDueDate = when (val rawDate = doc.get("nextDueDate")) {
                    is Number -> rawDate.toLong()
                    is com.google.firebase.Timestamp -> rawDate.toDate().time
                    else -> System.currentTimeMillis()
                }
                val isActive = doc.getBoolean("isActive") ?: true

                val entity = RecurringItemEntity(
                    recurringItemId = itemId,
                    userId = userId,
                    name = name,
                    amount = amount,
                    category = category,
                    classification = classification,
                    cadence = cadence,
                    nextDueDate = nextDueDate,
                    isActive = isActive,
                    syncStatus = "SYNCED"
                )
                expenseDao.insertRecurringItem(entity)
            }
        } catch (_: Exception) {
            // Offline mode: proceed with local Room cached data
        }
    }
}
