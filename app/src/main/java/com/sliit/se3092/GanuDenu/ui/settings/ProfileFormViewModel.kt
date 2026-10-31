package com.sliit.se3092.GanuDenu.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sliit.se3092.GanuDenu.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileFormViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileFormUiState())
    val uiState: StateFlow<ProfileFormUiState> = _uiState.asStateFlow()

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            val profile = repository.loadProfile()
            if (profile != null) {
                _uiState.update {
                    it.copy(
                        name = profile.name,
                        email = profile.email,
                        newPassword = "",
                        confirmPassword = ""
                    )
                }
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateEmail(email: String) = _uiState.update { it.copy(email = email) }
    fun updateNewPassword(pwd: String) = _uiState.update { it.copy(newPassword = pwd) }
    fun updateConfirmPassword(pwd: String) = _uiState.update { it.copy(confirmPassword = pwd) }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value

            try {
                // 1. Update Firestore Profile (passing "" for phone to prevent crashes)
                repository.createProfile(name = state.name.trim(), phone = "")

                // 2. Update Firebase Auth Password if the user typed a matching pair
                if (state.newPassword.isNotBlank() && state.newPassword == state.confirmPassword) {
                    auth.currentUser?.updatePassword(state.newPassword)
                }

                _uiState.update {
                    it.copy(
                        showSuccessSnackbar = true,
                        newPassword = "",
                        confirmPassword = ""
                    )
                }
                delay(3000)
                _uiState.update { it.copy(showSuccessSnackbar = false) }
            } catch (e: Exception) {
                // In a production app, we would update the UI state with an error message here
            }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(showSuccessSnackbar = false) }
    }

    fun updatePhotoBottomSheetVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(showPhotoBottomSheet = isVisible) }
    }
}