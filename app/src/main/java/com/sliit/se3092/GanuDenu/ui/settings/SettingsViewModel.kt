package com.sliit.se3092.GanuDenu.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sliit.se3092.GanuDenu.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            try {
                val profile = withTimeout(25_000) { repository.loadProfile() }

                val userId = auth.currentUser?.uid
                var fetchedBase64 = ""
                if (userId != null) {
                    try {
                        val doc = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
                        fetchedBase64 = doc.getString("profileImageBase64") ?: ""
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                _uiState.value = if (profile == null) SettingsUiState.Error("Your profile could not be found.")
                else SettingsUiState.Success(profile.name, profile.email, true, false, false, fetchedBase64)
            } catch (exception: Exception) {
                if (exception is CancellationException && exception !is TimeoutCancellationException) throw exception
                _uiState.value = SettingsUiState.Error("Unable to load your profile. Check your connection and retry.")
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Success) {
            _uiState.update { currentState.copy(notificationsEnabled = enabled) }
        }
    }

    fun updateLogoutDialogVisibility(isVisible: Boolean) {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Success) {
            _uiState.update { currentState.copy(showLogoutDialog = isVisible) }
        }
    }

    fun updateDeleteDialogVisibility(isVisible: Boolean) {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Success) {
            _uiState.update { currentState.copy(showDeleteDialog = isVisible) }
        }
    }
}