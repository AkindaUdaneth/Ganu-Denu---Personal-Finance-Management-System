package com.sliit.se3092.ganudenu.income

import com.sliit.se3092.GanuDenu.data.income.*
import com.sliit.se3092.GanuDenu.ui.income.IncomeViewModel
import com.sliit.se3092.GanuDenu.ui.income.incomeError
import com.google.firebase.firestore.FirebaseFirestoreException
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IncomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeIncomeRepository
    @Before fun setup() { Dispatchers.setMain(dispatcher); repository = FakeIncomeRepository() }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun realTimeEventsUpdateTotalsAndCacheIndicators() = runTest(dispatcher) {
        val vm = IncomeViewModel(repository)
        runCurrent()
        repository.events.value = IncomeSnapshot(listOf(entry(25000)), fromCache = true, pendingWrites = true)
        runCurrent()
        assertFalse(vm.uiState.value.loading)
        assertEquals(25000L, vm.uiState.value.summary.confirmedMinor)
        assertTrue(vm.uiState.value.snapshot.pendingWrites)
        assertTrue(vm.uiState.value.snapshot.fromCache)
        assertEquals(4, vm.uiState.value.cards.size)
    }

    @Test fun invalidFormDoesNotWriteAndValidFormClosesAfterSave() = runTest(dispatcher) {
        val vm = IncomeViewModel(repository)
        runCurrent()
        vm.openLogger()
        vm.save()
        assertNotNull(vm.uiState.value.formError)
        assertEquals(0, repository.attempts)
        vm.editDraft { it.copy(amount = "1000") }
        vm.save()
        runCurrent()
        assertNull(vm.uiState.value.draft)
        assertEquals(100000L, repository.saved.values.single().amountLkrMinor)
        assertNotNull(vm.uiState.value.message)
    }

    @Test fun failedSaveRetainsDraftIdForSafeRetry() = runTest(dispatcher) {
        val vm = IncomeViewModel(repository)
        runCurrent()
        vm.openLogger()
        vm.editDraft { it.copy(amount = "1000") }
        val id = vm.uiState.value.draft!!.id
        repository.fail = true
        vm.save()
        runCurrent()
        assertEquals(id, vm.uiState.value.draft?.id)
        assertNotNull(vm.uiState.value.formError)
        repository.fail = false
        vm.save()
        runCurrent()
        assertEquals(setOf(id), repository.saved.keys)
    }

    @Test fun sourceSelectionPreservesTheCommonFormFields() = runTest(dispatcher) {
        val vm = IncomeViewModel(repository)
        runCurrent()
        vm.openLogger()
        vm.editDraft { it.copy(amount = "135,000.00", status = IncomeStatus.ESTIMATED, frequency = IncomeFrequency.WEEKLY) }
        val initial = vm.uiState.value.draft!!
        IncomeSource.entries.forEach { source ->
            vm.selectSource(source)
            assertEquals(initial.copy(source = source), vm.uiState.value.draft)
        }
    }

    @Test fun eachSelectedSourceSavesItsOwnRecordWithFrequency() = runTest(dispatcher) {
        val vm = IncomeViewModel(repository)
        runCurrent()
        IncomeSource.entries.forEach { source ->
            vm.openLogger()
            vm.selectSource(source)
            vm.editDraft { it.copy(amount = "1,000.00", frequency = IncomeFrequency.WEEKLY) }
            vm.save()
            runCurrent()
        }
        assertEquals(4, repository.saved.size)
        assertEquals(IncomeSource.entries.toSet(), repository.saved.values.map { it.source }.toSet())
        assertTrue(repository.saved.values.all { it.frequency == IncomeFrequency.WEEKLY && it.amountLkrMinor == 100000L && it.status == IncomeStatus.CONFIRMED })
    }

    @Test fun signedOutSaveReportsSignInAndKeepsTheDraft() = runTest(dispatcher) {
        val vm = IncomeViewModel(repository)
        runCurrent()
        vm.openLogger()
        vm.editDraft { it.copy(amount = "1000") }
        val draft = vm.uiState.value.draft
        repository.signInRequired = true
        vm.save()
        runCurrent()
        assertEquals(draft, vm.uiState.value.draft)
        assertTrue(vm.uiState.value.formError!!.contains("Sign in"))
        assertFalse(vm.uiState.value.formError!!.contains("connection"))
        assertTrue(repository.saved.isEmpty())
    }

    private fun entry(minor: Long) = IncomeEntry("1", IncomeSource.SALARY, IncomeStatus.CONFIRMED,
        "250", IncomeCurrency.LKR, "1", minor, LocalDate.now(), "")
}

private class FakeIncomeRepository : IncomeRepository {
    val events = MutableStateFlow(IncomeSnapshot())
    var fail = false
    var attempts = 0
    var signInRequired = false
    val saved = mutableMapOf<String, IncomeEntry>()
    override fun observeMonth(month: YearMonth) = events
    override suspend fun save(entry: IncomeEntry) {
        attempts++
        if (signInRequired) throw IncomeSignInRequiredException()
        if (fail) error("Network unavailable")
        saved[entry.id] = entry
    }
}
