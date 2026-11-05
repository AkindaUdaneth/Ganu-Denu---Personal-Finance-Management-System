package com.sliit.se3092.ganudenu

import android.os.Bundle
import android.os.Parcel
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.FirebaseFirestoreException
import com.sliit.se3092.GanuDenu.data.income.*
import com.sliit.se3092.GanuDenu.ui.income.*
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class IncomeRecoveryUiTest {
    @get:Rule val compose = createComposeRule()
    private val repository = RecoveryIncomeRepository()

    @Test fun calendarSelectionIsSavedAsTheChosenPaymentDate() {
        val vm = IncomeViewModel(repository)
        compose.setContent { GanuDenuTheme { AddIncomeScreen(vm, {}, {}) } }
        compose.onNodeWithTag("income-amount").performTextInput("123.45")
        compose.onNodeWithTag("income-date").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Switch to text input mode").performClick()
        compose.onAllNodes(hasSetTextAction()).onLast().performTextReplacement("12312025")
        compose.onNodeWithText("Select").performClick()
        compose.onNodeWithText("31 Dec 2025").assertExists()
        compose.onNodeWithText("Save Income").performScrollTo().performClick()
        compose.waitUntil(5_000) { repository.saved != null }
        assertEquals(LocalDate.of(2025, 12, 31), repository.saved?.date)
    }

    @Test fun permissionDeniedKeepsTheDraftAndRetryUsesTheSameId() {
        repository.deny = true
        val vm = IncomeViewModel(repository)
        compose.setContent { GanuDenuTheme { AddIncomeScreen(vm, {}, {}) } }
        compose.onNodeWithTag("income-amount").performTextInput("250")
        compose.onNodeWithText("Save Income").performScrollTo().performClick()
        compose.onNodeWithText("Income access was denied.", substring = true).assertExists()
        val id = vm.uiState.value.draft!!.id
        repository.deny = false
        compose.onNodeWithText("Save Income").performScrollTo().performClick()
        compose.waitUntil(5_000) { repository.saved != null }
        assertEquals(id, repository.saved?.id)
        assertEquals(2, repository.attempts)
    }

    @Test fun processStateParcelRestoresMonthFieldsAndRetryIdentity() {
        val handle = SavedStateHandle()
        val original = IncomeViewModel(repository, handle)
        compose.runOnIdle {
            original.selectMonth(YearMonth.of(2025, 12))
            original.openLogger(IncomeSource.ADSENSE)
            original.editDraft { it.copy(amount = "875.50", date = "2025-12-31",
                status = IncomeStatus.ESTIMATED, frequency = IncomeFrequency.YEARLY) }
        }
        val id = original.uiState.value.draft!!.id
        val state = handle.savedStateProvider().saveState()
        val parcel = Parcel.obtain()
        val restoredState: Bundle
        try {
            parcel.writeBundle(state)
            parcel.setDataPosition(0)
            restoredState = requireNotNull(parcel.readBundle(IncomeDraft::class.java.classLoader))
        } finally { parcel.recycle() }
        val restored = IncomeViewModel(repository, SavedStateHandle.createHandle(restoredState, null))
        compose.setContent { GanuDenuTheme { AddIncomeScreen(restored, {}, {}) } }
        compose.onNodeWithTag("income-source-adsense").assertIsSelected()
        compose.onNodeWithTag("income-amount").assertTextContains("875.50")
        compose.onNodeWithText("31 Dec 2025").assertExists()
        compose.onNodeWithText("Yearly").assertExists()
        compose.onNodeWithText("Estimated").assertIsSelected()
        assertEquals(YearMonth.of(2025, 12), restored.uiState.value.month)
        assertEquals(id, restored.uiState.value.draft?.id)
        compose.onNodeWithText("Save Income").performScrollTo().performClick()
        compose.waitUntil(5_000) { repository.saved != null }
        assertEquals(id, repository.saved?.id)
    }
}

private class RecoveryIncomeRepository : IncomeRepository {
    val events = MutableStateFlow(IncomeSnapshot())
    var deny = false
    var attempts = 0
    @Volatile var saved: IncomeEntry? = null
    override fun observeMonth(month: YearMonth) = events
    override suspend fun save(entry: IncomeEntry) {
        attempts++
        if (deny) throw FirebaseFirestoreException("Denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
        saved = entry
        events.value = IncomeSnapshot(listOf(entry))
    }
}
