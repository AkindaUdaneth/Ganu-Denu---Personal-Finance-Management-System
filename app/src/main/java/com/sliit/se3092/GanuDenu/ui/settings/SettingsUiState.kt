package com.sliit.se3092.GanuDenu.ui.settings

import android.graphics.Bitmap

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val userName: String,
        val userEmail: String,
        val notificationsEnabled: Boolean,
        val showLogoutDialog: Boolean,
        val showDeleteDialog: Boolean,
        val profileImageBitmap: Bitmap? = null
    ) : SettingsUiState

    data class Error(val message: String) : SettingsUiState
}