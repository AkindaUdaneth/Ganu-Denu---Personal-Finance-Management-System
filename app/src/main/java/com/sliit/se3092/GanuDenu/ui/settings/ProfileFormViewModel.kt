package com.sliit.se3092.GanuDenu.ui.settings

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sliit.se3092.GanuDenu.data.auth.AuthRepository
import com.sliit.se3092.GanuDenu.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
                        phone = profile.phone,
                        newPassword = "",
                        confirmPassword = ""
                    )
                }
            }
        }
    }

    // --- Local Image Handling ---

    fun loadProfileImage(context: Context) {
        val userId = auth.currentUser?.uid ?: return
        val bitmap = ImageUtils.loadLocalProfileImage(context, userId)
        _uiState.update { it.copy(profileImageBitmap = bitmap) }
    }

    fun updateProfileImageFromUri(context: Context, uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val bitmap = ImageUtils.saveUriToLocal(context, uri, userId)
        _uiState.update { it.copy(profileImageBitmap = bitmap, showPhotoBottomSheet = false) }
    }

    fun updateProfileImageFromBitmap(context: Context, bitmap: Bitmap) {
        val userId = auth.currentUser?.uid ?: return
        val savedBitmap = ImageUtils.saveBitmapToLocal(context, bitmap, userId)
        _uiState.update { it.copy(profileImageBitmap = savedBitmap, showPhotoBottomSheet = false) }
    }

    fun removeProfileImage(context: Context) {
        val userId = auth.currentUser?.uid ?: return
        ImageUtils.removeLocalProfileImage(context, userId)
        _uiState.update { it.copy(profileImageBitmap = null, showPhotoBottomSheet = false) }
    }

    // --- Profile Editing ---

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updatePhone(phone: String) = _uiState.update { it.copy(phone = phone) }
    fun updateEmail(email: String) = _uiState.update { it.copy(email = email) }
    fun updateNewPassword(pwd: String) = _uiState.update { it.copy(newPassword = pwd) }
    fun updateConfirmPassword(pwd: String) = _uiState.update { it.copy(confirmPassword = pwd) }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            try {
                repository.createProfile(name = state.name.trim(), phone = state.phone.trim())

                if (state.newPassword.isNotBlank() && state.newPassword == state.confirmPassword) {
                    auth.currentUser?.updatePassword(state.newPassword)?.await()
                }

                _uiState.update {
                    it.copy(
                        showSuccessSnackbar = true,
                        errorMessage = null,
                        newPassword = "",
                        confirmPassword = ""
                    )
                }
                delay(3000)
                _uiState.update { it.copy(showSuccessSnackbar = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to save profile.") }
                delay(4000)
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(showSuccessSnackbar = false, errorMessage = null) }
    }

    fun updatePhotoBottomSheetVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(showPhotoBottomSheet = isVisible) }
    }
}