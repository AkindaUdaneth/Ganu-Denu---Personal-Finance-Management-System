package com.sliit.se3092.GanuDenu.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileFormViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileFormUiState())
    val uiState: StateFlow<ProfileFormUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = ProfileFormUiState(
            name = "Kavindu",
            email = "kavindu123@gmail.com",
            newPassword = "password123",
            confirmPassword = "password123"
        )
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateEmail(email: String) = _uiState.update { it.copy(email = email) }
    fun updateNewPassword(pwd: String) = _uiState.update { it.copy(newPassword = pwd) }
    fun updateConfirmPassword(pwd: String) = _uiState.update { it.copy(confirmPassword = pwd) }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(showSuccessSnackbar = true) }
            delay(3000)
            _uiState.update { it.copy(showSuccessSnackbar = false) }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(showSuccessSnackbar = false) }
    }
}