package com.sliit.se3092.GanuDenu.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        // Mock data matching the Figma Settings screen
        _uiState.value = SettingsUiState.Success(
            userName = "Kavindu",
            userEmail = "kavindu123@gmail.com",
            notificationsEnabled = true,
            showLogoutDialog = false,
            showDeleteDialog = false
        )
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