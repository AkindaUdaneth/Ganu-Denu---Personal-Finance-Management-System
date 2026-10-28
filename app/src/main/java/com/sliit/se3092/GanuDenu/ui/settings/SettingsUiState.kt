package com.sliit.se3092.GanuDenu.ui.settings

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val userName: String,
        val userEmail: String,
        val notificationsEnabled: Boolean,
        val showLogoutDialog: Boolean,
        val showDeleteDialog: Boolean
    ) : SettingsUiState

    data class Error(val message: String) : SettingsUiState
}