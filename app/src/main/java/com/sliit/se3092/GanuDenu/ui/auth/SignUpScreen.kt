package com.sliit.se3092.GanuDenu.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Registration and retryable profile completion, separate from the login form. */
@Composable
internal fun SignUpScreen(
    state: AuthUiState, viewModel: AuthViewModel, profileOnly: Boolean = false
) {
    val focus = LocalFocusManager.current
    val submit = { focus.clearFocus(); viewModel.submit() }
    AuthFormContainer {
        Text("Ganu Denu", color = AuthYellow, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        Text(if (profileOnly) "Complete your profile" else "Create Account", color = Color.White,
            fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(if (profileOnly) "Add your details to finish setup." else "Create a profile to start tracking your finances.", color = Color.LightGray,
            modifier = Modifier.padding(top = 12.dp))
        Spacer(Modifier.height(20.dp))
        AuthField("Name", state.name, viewModel::updateName, "Your full name", state.nameError, !state.busy)
        Spacer(Modifier.height(14.dp))
        AuthField("Phone number", state.phone, viewModel::updatePhone, "+94 71 123 4567", state.phoneError, !state.busy,
            keyboardType = KeyboardType.Phone, imeAction = if (profileOnly) ImeAction.Done else ImeAction.Next, onDone = submit)
        Spacer(Modifier.height(14.dp))
        if (!profileOnly) {
            AuthField("Email address", state.email, viewModel::updateEmail, "you@example.com", state.emailError, !state.busy,
                keyboardType = KeyboardType.Email, leadingIcon = Icons.Outlined.Email)
            Spacer(Modifier.height(14.dp))
            AuthField("Password", state.password, viewModel::updatePassword, "At least 6 characters", state.passwordError, !state.busy,
                password = true, leadingIcon = Icons.Outlined.Lock, imeAction = ImeAction.Done, onDone = submit)
        }
        AuthFeedback(state.error, state.message)
        Spacer(Modifier.height(18.dp))
        AuthSubmitButton(if (profileOnly) "Save Profile" else "Create Account", state.busy, submit)
        TextButton(onClick = { if (profileOnly) viewModel.signOut() else viewModel.switchMode(false) }, enabled = !state.busy) {
            Text(if (profileOnly) "Sign out" else "Already have an account? ", color = Color.White, fontSize = 12.sp)
            if (!profileOnly) Text("Sign in", color = AuthYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
    }
}
