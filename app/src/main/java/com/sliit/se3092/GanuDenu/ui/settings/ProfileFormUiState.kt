package com.sliit.se3092.GanuDenu.ui.settings

data class ProfileFormUiState(
    val name: String = "",
    val email: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val showSuccessSnackbar: Boolean = false,
    val showPhotoBottomSheet: Boolean = false
)