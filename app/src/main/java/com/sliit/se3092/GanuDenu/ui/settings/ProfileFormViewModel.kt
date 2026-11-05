package com.sliit.se3092.GanuDenu.ui.settings

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

            if (profile != null) {
                _uiState.update {
                    it.copy(
                        name = profile.name,
                        email = profile.email,
                        phone = profile.phone,
                        newPassword = "",
                        confirmPassword = "",
                        profileImageBase64 = fetchedBase64
                    )
                }
            }
        }
    }


    fun updateProfileImageFromUri(context: Context, uri: Uri) {
        val base64Image = ImageUtils.uriToBase64(context, uri)
        if (base64Image != null) saveImageToFirestore(base64Image)
    }

    fun updateProfileImageFromBitmap(bitmap: Bitmap) {
        val base64Image = ImageUtils.bitmapToBase64(bitmap)
        saveImageToFirestore(base64Image)
    }

    fun removeProfileImage() {
        saveImageToFirestore("")
    }

    private fun saveImageToFirestore(base64Image: String) {
        val userId = auth.currentUser?.uid ?: return

        // Update UI immediately and close the bottom sheet
        _uiState.update {
            it.copy(profileImageBase64 = base64Image, showPhotoBottomSheet = false)
        }

        viewModelScope.launch {
            try {
                val data = mapOf("profileImageBase64" to base64Image)
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(data, SetOptions.merge())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to save profile. Check connection.") }
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