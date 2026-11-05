package com.sliit.se3092.ganudenu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sliit.se3092.GanuDenu.data.local.AppDatabase
import com.sliit.se3092.GanuDenu.data.repository.ExpenseRepositoryImpl
import com.sliit.se3092.GanuDenu.data.repository.GoalRepository
import com.sliit.se3092.GanuDenu.ui.expenses.DailyExpenseLoggerScreen
import com.sliit.se3092.GanuDenu.ui.goals.GoalFormScreen
import com.sliit.se3092.GanuDenu.ui.goals.GoalFormViewModel
import com.sliit.se3092.GanuDenu.ui.goals.GoalEngineScreen
import com.sliit.se3092.GanuDenu.ui.goals.GoalEngineViewModel
import com.sliit.se3092.GanuDenu.data.local.entity.GoalEntity
import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity
import com.sliit.se3092.GanuDenu.ui.expenses.RecurringExpenseScreen
import com.sliit.se3092.GanuDenu.ui.expenses.ExpenseViewModel
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import org.junit.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import java.util.UUID

/** Actual form -> ViewModel -> Room -> emulator Firestore. No production account/data. */
class FinanceWorkflowIntegrationTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var firebase: FirebaseApp
    private lateinit var auth: FirebaseAuth
    private lateinit var cloud: FirebaseFirestore
    private lateinit var local: AppDatabase

    @Before fun setup(): Unit = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("firebaseEmulators") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        firebase = requireNotNull(FirebaseApp.initializeApp(context,
            FirebaseOptions.Builder().setProjectId("demo-ganudenu-qa")
                .setApplicationId("1:123456789:android:0123456789abcdef")
                .setApiKey("fake-api-key").build(), "finance-" + UUID.randomUUID()))
        auth = FirebaseAuth.getInstance(firebase).apply { useEmulator("10.0.2.2", 9098) }
        cloud = FirebaseFirestore.getInstance(firebase).apply { useEmulator("10.0.2.2", 8085) }
        local = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        withTimeout(30_000) {
            auth.createUserWithEmailAndPassword("finance-${UUID.randomUUID()}@example.com", "TestPassword2026!").await()
        }
        Unit
    }

    @After fun cleanup(): Unit = runBlocking {
        if (::local.isInitialized) local.close()
        if (::cloud.isInitialized) cloud.terminate().await()
        if (::firebase.isInitialized) firebase.delete()
    }

    @Test fun expenseFormPersistsSelectedCategoryAndPaymentToRoomAndCloud(): Unit = runBlocking {
        val repository = ExpenseRepositoryImpl(local.expenseDao(), cloud)
        lateinit var vm: ExpenseViewModel
        compose.runOnIdle { vm = ExpenseViewModel(repository, auth) }
        compose.setContent { GanuDenuTheme { DailyExpenseLoggerScreen(vm, {}) } }
        compose.onNode(hasSetTextAction()).performTextInput("1500")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("UberEats").performClick()
        compose.onNodeWithText("Card", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Save Expense").performClick()
        val uid = requireNotNull(auth.currentUser).uid
        val expense = withTimeout(15_000) {
            local.expenseDao().getAllExpenses(uid).first { rows -> rows.any { it.syncStatus == "SYNCED" } }.single()
        }
        assertEquals(1500.0, expense.amount, 0.001)
        assertEquals("UberEats", expense.category)
        assertEquals("Card", expense.paymentMethod)
        assertEquals("Discretionary", expense.classification)
        val remote = withTimeout(15_000) {
            cloud.collection("users").document(uid).collection("expenses").document(expense.expenseId).get().await()
        }
        assertEquals(1500.0, remote.getDouble("amount")!!, 0.001)
        assertEquals("Card", remote.getString("paymentMethod"))
    }

    @Test fun goalFormPersistsBaselineToRoomAndCloud(): Unit = runBlocking {
        val repository = GoalRepository(local.goalDao(), cloud)
        lateinit var vm: GoalFormViewModel
        compose.runOnIdle { vm = GoalFormViewModel(repository, auth) }
        compose.setContent { GanuDenuTheme { GoalFormScreen(vm) } }
        compose.waitUntil(5_000) { vm.uiState.value.goalName.isNotBlank() }
        compose.onNodeWithText("Goal Name").performScrollTo().performTextReplacement("MacBook Pro M4")
        compose.onNodeWithText("Target Amount (LKR)").performScrollTo().performTextReplacement("490000")
        compose.onNodeWithText("Target Date").performScrollTo().performTextReplacement("November 05, 2027")
        compose.onNodeWithText("Current Saved (LKR)").performScrollTo().performTextReplacement("11200")
        compose.onNodeWithText("Save Goal").performScrollTo().performClick()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Goal saved successfully!").fetchSemanticsNodes().isNotEmpty()
        }
        val uid = requireNotNull(auth.currentUser).uid
        val goal = withTimeout(15_000) { local.goalDao().getAllGoals(uid).first { it.isNotEmpty() }.single() }
        assertEquals("MacBook Pro M4", goal.name)
        assertEquals(490000.0, goal.targetAmount, 0.001)
        assertEquals(11200.0, goal.currentSaved, 0.001)
        val remote = withTimeout(15_000) {
            cloud.collection("users").document(uid).collection("goals").document(goal.goalId).get().await()
        }
        assertEquals(goal.name, remote.getString("name"))
        assertEquals(goal.currentSaved, remote.getDouble("currentSaved")!!, 0.001)
    }

    @Test fun offlineExpenseRemainsPendingThenSyncsOnReconnect(): Unit = runBlocking {
        val repository = ExpenseRepositoryImpl(local.expenseDao(), cloud)
        lateinit var vm: ExpenseViewModel
        compose.runOnIdle { vm = ExpenseViewModel(repository, auth) }
        cloud.disableNetwork().await()
        compose.setContent { GanuDenuTheme { DailyExpenseLoggerScreen(vm, {}) } }
        compose.onNode(hasSetTextAction()).performTextInput("900")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Save Expense").performClick()
        val uid = requireNotNull(auth.currentUser).uid
        val pending = withTimeout(10_000) { local.expenseDao().getAllExpenses(uid).first { it.isNotEmpty() }.single() }
        assertEquals("PENDING", pending.syncStatus)
        cloud.enableNetwork().await()
        val synced = withTimeout(20_000) {
            local.expenseDao().getAllExpenses(uid).first { rows -> rows.any { it.syncStatus == "SYNCED" } }.single()
        }
        assertEquals(pending.expenseId, synced.expenseId)
        assertEquals(900.0, synced.amount, 0.001)
        val remote = cloud.collection("users").document(uid).collection("expenses").get().await()
        assertEquals(1, remote.size())
    }

    @Test fun recurringCancellationUpdatesCloudWithoutDeletingExpenseHistory(): Unit = runBlocking {
        val uid = requireNotNull(auth.currentUser).uid
        val repository = ExpenseRepositoryImpl(local.expenseDao(), cloud)
        val item = RecurringItemEntity(userId = uid, name = "QA Gym", amount = 2500.0,
            category = "Gym", classification = "Committed", cadence = "Monthly",
            nextDueDate = System.currentTimeMillis(), isActive = true)
        repository.insertRecurringItem(item)
        val historical = ExpenseEntity(userId = uid, amount = 2500.0, category = "Gym", paymentMethod = "Card",
            classification = "Committed", date = System.currentTimeMillis(), isRecurringInstance = true,
            recurringItemId = item.recurringItemId)
        repository.insertExpense(historical)
        lateinit var vm: ExpenseViewModel
        compose.runOnIdle { vm = ExpenseViewModel(repository, auth) }
        compose.setContent { GanuDenuTheme { RecurringExpenseScreen(vm, {}, {}) } }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("QA Gym").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Deactivate").performClick()
        val inactive = withTimeout(15_000) {
            local.expenseDao().getActiveRecurringItems(uid).first { rows -> rows.any { !it.isActive && it.syncStatus == "SYNCED" } }.single()
        }
        assertFalse(inactive.isActive)
        val remote = cloud.collection("users").document(uid).collection("recurringItems").document(item.recurringItemId).get().await()
        assertEquals(false, remote.getBoolean("isActive"))
        assertEquals(historical.expenseId, local.expenseDao().getAllExpenses(uid).first().single().expenseId)
        assertTrue(cloud.collection("users").document(uid).collection("expenses").document(historical.expenseId).get().await().exists())
    }

    @Test fun savedGoalDisplaysProgressAndImpulseDelay(): Unit = runBlocking {
        val uid = requireNotNull(auth.currentUser).uid
        val repository = GoalRepository(local.goalDao(), cloud)
        repository.insertOrUpdateGoal(GoalEntity(userId = uid, name = "MacBook Pro M4", targetAmount = 490000.0,
            currentSaved = 11200.0, targetDate = System.currentTimeMillis() + 365L * 86400000L))
        lateinit var vm: GoalEngineViewModel
        compose.runOnIdle { vm = GoalEngineViewModel(repository, auth) }
        compose.setContent { GanuDenuTheme { GoalEngineScreen(vm) } }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("MacBook Pro M4").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("2.3%").assertIsDisplayed()
        compose.onNodeWithText("Hypothetical Expense").performScrollTo().performTextInput("15000")
        compose.onNodeWithText("This pushes your MacBook deadline back by 11 days").performScrollTo().assertIsDisplayed()
    }
}
