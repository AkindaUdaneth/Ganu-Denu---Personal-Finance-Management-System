package com.sliit.se3092.GanuDenu.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Email/password sign-in form; account and session operations stay in AuthViewModel. */
@Composable
internal fun LoginScreen(state: AuthUiState, viewModel: AuthViewModel, biometricAvailable: Boolean) {
    val focus = LocalFocusManager.current
    val submit = { focus.clearFocus(); viewModel.submit() }
    AuthFormContainer {
        Spacer(Modifier.height(136.dp))
        Text("Your money, clearly managed.", color = Color(0xFF999999), fontSize = 11.sp)
        Spacer(Modifier.height(28.dp))
        Text("Welcome back", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth())
        Text("Sign in to continue managing your finances.", color = Color(0xFF999999), fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        AuthField("Email address", state.email, viewModel::updateEmail, "you@example.com", state.emailError, !state.busy,
            keyboardType = KeyboardType.Email, leadingIcon = Icons.Outlined.Email)
        Spacer(Modifier.height(14.dp))
        AuthField("Password", state.password, viewModel::updatePassword, "Enter your password", state.passwordError, !state.busy,
            password = true, leadingIcon = Icons.Outlined.Lock, imeAction = ImeAction.Done, onDone = submit)
        TextButton(onClick = viewModel::showResetDialog, enabled = !state.busy, modifier = Modifier.align(Alignment.End)) {
            Text("Forgot password?", color = AuthYellow, fontSize = 12.sp)
        }
        BiometricOption(state.biometricEnabled, state.busy, biometricAvailable, viewModel::updateBiometric)
        AuthFeedback(state.error, state.message)
        Spacer(Modifier.height(18.dp))
        AuthSubmitButton("Sign In", state.busy, submit)
        TextButton(onClick = { viewModel.switchMode(true) }, enabled = !state.busy) {
            Text("New to Ganudenu? ", color = Color.White, fontSize = 12.sp)
            Text("Create account", color = AuthYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
    }
}
