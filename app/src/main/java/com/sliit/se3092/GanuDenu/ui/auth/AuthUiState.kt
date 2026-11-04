package com.sliit.se3092.GanuDenu.ui.auth

import com.sliit.se3092.GanuDenu.data.auth.UserProfile

enum class AuthSession { CHECKING, SIGNED_OUT, NEEDS_PROFILE, SIGNED_IN, ERROR }

data class AuthUiState(
    val session: AuthSession = AuthSession.CHECKING,
    val signingUp: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val busy: Boolean = false,
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val message: String? = null,
    val profile: UserProfile? = null,
    val resetDialog: Boolean = false,
    val resetEmail: String = ""
)
