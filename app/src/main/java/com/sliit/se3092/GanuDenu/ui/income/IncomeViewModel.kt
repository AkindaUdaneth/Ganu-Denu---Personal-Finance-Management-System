package com.sliit.se3092.GanuDenu.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.sliit.se3092.GanuDenu.data.income.*
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class IncomeUiState(
    val month: YearMonth = YearMonth.now(),
    val loading: Boolean = true,
    val snapshot: IncomeSnapshot = IncomeSnapshot(),
    val summary: IncomeSummary = IncomeSummary.from(emptyList()),
    val cards: List<IncomeCardUi> = incomeCards(IncomeSummary.from(emptyList())),
    val error: String? = null,
    val draft: IncomeDraft? = null,
    val formError: String? = null,
    val saving: Boolean = false,
    val message: String? = null
)

class IncomeViewModel(private val repository: IncomeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState = _uiState.asStateFlow()
    private var observation: Job? = null

    init { retry() }

    fun retry() {
        observation?.cancel()
        _uiState.update { it.copy(loading = true, error = null) }
        observation = viewModelScope.launch {
            try {
                repository.observeMonth(_uiState.value.month).collect { snapshot ->
                    val summary = IncomeSummary.from(snapshot.entries)
                    _uiState.update { it.copy(loading = false, snapshot = snapshot,
                        summary = summary, cards = incomeCards(summary), error = null) }
                }
            } catch (exception: Exception) {
                if (exception is CancellationException) throw exception
                _uiState.update { it.copy(loading = false, error = incomeError(exception)) }
            }
        }
    }

    fun refreshMonth() {
        val month = YearMonth.now()
        if (_uiState.value.month != month) {
            _uiState.update { it.copy(month = month, snapshot = IncomeSnapshot(), summary = IncomeSummary.from(emptyList()), cards = incomeCards(IncomeSummary.from(emptyList()))) }
            retry()
        }
    }

    fun openLogger(source: IncomeSource = IncomeSource.SALARY) {
        if (_uiState.value.saving) return
        _uiState.update { it.copy(draft = IncomeDraft(UUID.randomUUID().toString(), source), formError = null, message = null) }
    }

    fun dismissLogger() { if (!_uiState.value.saving) _uiState.update { it.copy(draft = null, formError = null) } }
    fun editDraft(transform: (IncomeDraft) -> IncomeDraft) {
        if (_uiState.value.saving) return
        _uiState.update { it.copy(draft = it.draft?.let(transform), formError = null) }
    }
    fun selectSource(source: IncomeSource) = editDraft { it.copy(source = source) }

    fun save() {
        val state = _uiState.value
        if (state.saving) return
        val draft = state.draft ?: return
        val entry = try { IncomeValidation.toEntry(draft) } catch (exception: IllegalArgumentException) {
            _uiState.update { it.copy(formError = exception.message) }; return
        }
        _uiState.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            try {
                withTimeout(25_000) { repository.save(entry) }
                val inMonth = YearMonth.from(entry.date) == state.month
                _uiState.update { it.copy(saving = false, draft = null,
                    message = if (inMonth) "Income saved." else "Income saved for ${YearMonth.from(entry.date)}. This dashboard shows ${state.month}.") }
            } catch (exception: Exception) {
                if (exception is CancellationException && exception !is TimeoutCancellationException) throw exception
                _uiState.update { it.copy(saving = false, formError = if (exception is TimeoutCancellationException)
                    "Waiting for server confirmation. The entry may still sync. Keep this form and retry to avoid creating a duplicate."
                    else incomeError(exception)) }
            }
        }
    }
    fun dismissMessage() = _uiState.update { it.copy(message = null) }
}

internal fun incomeError(exception: Exception): String = when (exception) {
    is IncomeSignInRequiredException -> "You are signed out. Sign in before viewing or saving income."
    is FirebaseFirestoreException -> when (exception.code) {
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> "Your session has expired. Sign in again to sync income."
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Income access was denied. Publish the latest Firestore rules and check your account permissions."
        FirebaseFirestoreException.Code.FAILED_PRECONDITION -> "Firestore is not ready for this income query. Check the database and index setup in Firebase Console."
        FirebaseFirestoreException.Code.NOT_FOUND -> "The Firebase database could not be found. Check the project configuration and create its default Firestore database."
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> "Firebase usage limits were reached. Check the project's quota and try again later."
        FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> "Unable to reach Firebase. Check your connection and retry."
        else -> "Income could not be synced (${exception.code.name}). Please retry."
    }
    else -> "Income could not be synced. Please retry."
}
