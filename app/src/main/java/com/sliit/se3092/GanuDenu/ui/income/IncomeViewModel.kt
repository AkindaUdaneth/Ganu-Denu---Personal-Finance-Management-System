package com.sliit.se3092.GanuDenu.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
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

class IncomeViewModel(
    private val repository: IncomeRepository,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {
    private var followsCurrentMonth = savedStateHandle.get<Boolean>("income_follows_current_month") ?: true
    private val restoredMonth = savedStateHandle.get<String>("income_month")?.let(YearMonth::parse)
    private val _uiState = MutableStateFlow(IncomeUiState(
        month = if (followsCurrentMonth) YearMonth.now() else restoredMonth ?: YearMonth.now(),
        draft = savedStateHandle["income_draft"],
        message = savedStateHandle["income_message"]
    ))
    val uiState = _uiState.asStateFlow()
    private var observation: Job? = null

    init { retry() }

    fun retry() {
        observation?.cancel()
        _uiState.update { it.copy(loading = true, error = null) }
        val month = _uiState.value.month
        observation = viewModelScope.launch {
            try {
                repository.observeMonth(month).collect { snapshot ->
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
        if (followsCurrentMonth) showMonth(YearMonth.now())
    }

    fun selectMonth(month: YearMonth) {
        followsCurrentMonth = month == YearMonth.now()
        savedStateHandle["income_follows_current_month"] = followsCurrentMonth
        showMonth(month)
    }

    private fun showMonth(month: YearMonth) {
        if (_uiState.value.month == month) return
        savedStateHandle["income_month"] = month.toString()
        savedStateHandle["income_message"] = null
        observation?.cancel()
        val emptySummary = IncomeSummary.from(emptyList())
        _uiState.update {
            it.copy(
                month = month, loading = true, snapshot = IncomeSnapshot(),
                summary = emptySummary, cards = incomeCards(emptySummary), error = null, message = null
            )
        }
        retry()
    }

    fun openLogger(source: IncomeSource = IncomeSource.SALARY) {
        if (_uiState.value.saving) return
        savedStateHandle["income_message"] = null
        setDraft(IncomeDraft(UUID.randomUUID().toString(), source))
        _uiState.update { it.copy(message = null) }
    }

    fun dismissLogger() { if (!_uiState.value.saving) setDraft(null) }
    fun editDraft(transform: (IncomeDraft) -> IncomeDraft) {
        if (_uiState.value.saving) return
        setDraft(_uiState.value.draft?.let(transform))
    }
    private fun setDraft(draft: IncomeDraft?) {
        savedStateHandle["income_draft"] = draft
        _uiState.update { it.copy(draft = draft, formError = null) }
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
                val message = if (inMonth) "Income saved." else "Income saved for ${YearMonth.from(entry.date)}. This dashboard shows ${state.month}."
                savedStateHandle["income_draft"] = null
                savedStateHandle["income_message"] = message
                _uiState.update { it.copy(saving = false, draft = null, message = message) }
            } catch (exception: Exception) {
                if (exception is CancellationException && exception !is TimeoutCancellationException) throw exception
                _uiState.update { it.copy(saving = false, formError = if (exception is TimeoutCancellationException)
                    "Waiting for server confirmation. The entry may still sync. Keep this form and retry to avoid creating a duplicate."
                    else incomeError(exception)) }
            }
        }
    }
    fun dismissMessage() {
        savedStateHandle["income_message"] = null
        _uiState.update { it.copy(message = null) }
    }
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
