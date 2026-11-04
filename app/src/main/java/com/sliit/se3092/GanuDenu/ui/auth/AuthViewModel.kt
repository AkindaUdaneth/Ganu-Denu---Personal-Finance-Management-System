package com.sliit.se3092.GanuDenu.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.sliit.se3092.GanuDenu.data.auth.AuthRepository
import com.sliit.se3092.GanuDenu.data.auth.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()
    private var handledUid: String? = null

    init {
        viewModelScope.launch {
            repository.identities.collect { identity ->
                // Registration emits an identity before its Firestore profile is saved.
                if (!_uiState.value.busy) {
                    if (identity == null) {
                        handledUid = null
                        _uiState.update { current ->
                            AuthUiState(session = AuthSession.SIGNED_OUT, error = current.error)
                        }
                    } else if (identity.uid != handledUid) restoreSession()
                }
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value, nameError = null, error = null) }
    fun updatePhone(value: String) = _uiState.update { it.copy(phone = value, phoneError = null, error = null) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, emailError = null, error = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, passwordError = null, error = null) }

    fun switchMode(signingUp: Boolean) {
        if (_uiState.value.busy) return
        _uiState.update { AuthUiState(session = AuthSession.SIGNED_OUT, signingUp = signingUp, email = it.email) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.busy) return
        val completingProfile = state.session == AuthSession.NEEDS_PROFILE
        val needsDetails = state.signingUp || completingProfile
        val validated = state.copy(
            nameError = if (needsDetails) AuthValidation.name(state.name) else null,
            phoneError = if (needsDetails) AuthValidation.phone(state.phone) else null,
            emailError = if (!completingProfile) AuthValidation.email(state.email) else null,
            passwordError = if (!completingProfile) AuthValidation.password(state.password, state.signingUp) else null,
            error = null, message = null
        )
        _uiState.value = validated
        if (listOf(validated.nameError, validated.phoneError, validated.emailError, validated.passwordError).any { it != null }) return
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                withTimeout(25_000) {
                    if (!completingProfile) {
                        if (state.signingUp) repository.register(state.email.trim(), state.password)
                        else repository.signIn(state.email.trim(), state.password)
                    }
                    // The SDK callback may arrive after this coroutine finishes. Do not
                    // let that duplicate event replace profile errors.
                    handledUid = repository.currentIdentity?.uid
                    val profile = if (needsDetails) repository.createProfile(state.name.trim(), AuthValidation.normalizePhone(state.phone))
                        else repository.loadProfile()
                    if (profile == null) {
                        _uiState.update { it.copy(session = AuthSession.NEEDS_PROFILE, busy = false, password = "", error = null) }
                    } else finishSession(profile)
                }
            } catch (exception: Exception) {
                if (exception is CancellationException && exception !is TimeoutCancellationException) throw exception
                val hasAccount = repository.currentIdentity != null
                handledUid = repository.currentIdentity?.uid
                _uiState.update { it.copy(
                    busy = false,
                    session = if (hasAccount) AuthSession.NEEDS_PROFILE else AuthSession.SIGNED_OUT,
                    password = if (hasAccount) "" else it.password,
                    error = if (hasAccount) "Your account is signed in, but we could not load or save your profile. Check your connection and retry. ${friendlyError(exception)}" else friendlyError(exception)
                ) }
            }
        }
    }

    fun retrySession() {
        if (_uiState.value.busy) return
        viewModelScope.launch { restoreSession() }
    }

    fun signInWithGoogle(idToken: String, isSignUp: Boolean = false) {
        val state = _uiState.value
        if (state.busy) return
        _uiState.update { it.copy(busy = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                withTimeout(25_000) {
                    repository.signInWithGoogle(idToken)
                    handledUid = repository.currentIdentity?.uid
                    val profile = repository.loadProfile()
                    if (profile == null) {
                        if (!isSignUp) {
                            repository.signOut()
                            _uiState.update { it.copy(
                                busy = false,
                                session = AuthSession.SIGNED_OUT,
                                error = "No account found with this Google email. Please create an account first."
                            ) }
                        } else {
                            val currentUser = repository.currentIdentity
                            _uiState.update { it.copy(
                                session = AuthSession.NEEDS_PROFILE,
                                busy = false,
                                email = currentUser?.email.orEmpty(),
                                name = currentUser?.email?.substringBefore('@')?.replaceFirstChar { c -> c.uppercase() } ?: ""
                            ) }
                        }
                    } else {
                        finishSession(profile)
                    }
                }
            } catch (exception: Exception) {
                if (exception is CancellationException && exception !is TimeoutCancellationException) throw exception
                val hasAccount = repository.currentIdentity != null
                handledUid = repository.currentIdentity?.uid
                _uiState.update { it.copy(
                    busy = false,
                    session = if (hasAccount && isSignUp) AuthSession.NEEDS_PROFILE else AuthSession.SIGNED_OUT,
                    error = friendlyError(exception)
                ) }
            }
        }
    }

    private suspend fun restoreSession() {
        val identity = repository.currentIdentity ?: return
        handledUid = identity.uid
        _uiState.update { it.copy(session = AuthSession.CHECKING, busy = true, error = null) }
        try {
            val profile = withTimeout(25_000) { repository.loadProfile() }
            if (profile == null) _uiState.update { it.copy(session = AuthSession.NEEDS_PROFILE, email = identity.email, busy = false) }
            else finishSession(profile)
        } catch (exception: Exception) {
            if (exception is CancellationException && exception !is TimeoutCancellationException) throw exception
            _uiState.update { it.copy(session = AuthSession.ERROR, busy = false, error = friendlyError(exception)) }
        }
    }

    private fun finishSession(profile: UserProfile) {
        _uiState.value = AuthUiState(session = AuthSession.SIGNED_IN, profile = profile, email = profile.email)
    }

    fun signOut() {
        if (_uiState.value.busy) return
        repository.signOut()
        handledUid = null
        _uiState.value = AuthUiState(session = AuthSession.SIGNED_OUT)
    }

    fun showResetDialog() = _uiState.update { it.copy(resetDialog = true, resetEmail = it.email, error = null, message = null) }
    fun updateResetEmail(value: String) = _uiState.update { it.copy(resetEmail = value, emailError = null, error = null) }
    fun dismissResetDialog() { if (!_uiState.value.busy) _uiState.update { it.copy(resetDialog = false, emailError = null, error = null) } }

    fun resetPassword() {
        val state = _uiState.value
        if (state.busy) return
        val error = AuthValidation.email(state.resetEmail)
        if (error != null) { _uiState.update { it.copy(emailError = error) }; return }
        _uiState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                withTimeout(25_000) { repository.sendPasswordReset(state.resetEmail.trim()) }
                _uiState.update { it.copy(busy = false, resetDialog = false, emailError = null, message = "If an account exists for that email, a password reset link has been sent. Check your inbox and spam folder.") }
            } catch (exception: Exception) {
                if (exception is CancellationException && exception !is TimeoutCancellationException) throw exception
                _uiState.update { it.copy(busy = false, error = friendlyError(exception)) }
            }
        }
    }
}

internal fun friendlyError(exception: Exception): String = when (exception) {
    is TimeoutCancellationException -> "The request took too long. Check your connection and retry."
    is FirebaseNetworkException -> "Unable to connect. Check your internet connection."
    is FirebaseTooManyRequestsException -> "Too many attempts. Please try again later."
    is FirebaseAuthException -> when (exception.errorCode) {
        "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already uses this email. Sign in or reset your password."
        "ERROR_WEAK_PASSWORD" -> "Choose a stronger password that meets the account password policy."
        "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
        "ERROR_USER_DISABLED" -> "This account has been disabled. Contact support."
        "ERROR_OPERATION_NOT_ALLOWED" -> "Email/password sign-in is not enabled for this Firebase project."
        else -> "Sign-in failed. Check your email and password, or reset your password."
    }
    is FirebaseFirestoreException -> if (exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
        "Profile access was denied. Check that the latest Firestore rules are published."
        else "Unable to sync your profile. Check your connection and retry."
    else -> "Something went wrong. Please try again."
}
