package com.sliit.se3092.GanuDenu.ui.settings

import android.graphics.Bitmap
data class ProfileFormUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val showSuccessSnackbar: Boolean = false,
    val errorMessage: String? = null,
    val showPhotoBottomSheet: Boolean = false,
    val profileImageBitmap: Bitmap? = null
)