package com.sliit.se3092.GanuDenu.ui.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.fragmentActivity() }
    val biometricAvailable = BiometricManager.from(context).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG
    ) == BiometricManager.BIOMETRIC_SUCCESS
    val onSuccess by rememberUpdatedState(viewModel::biometricSucceeded)
    val onMessage by rememberUpdatedState(viewModel::showMessage)
    val prompt = remember(activity) {
        activity?.let {
            BiometricPrompt(it, ContextCompat.getMainExecutor(it), object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onMessage("${errString}. You can retry or use your password.")
                }
                override fun onAuthenticationFailed() { onMessage("Biometric not recognized. Please try again.") }
            })
        }
    }
    DisposableEffect(prompt) { onDispose { prompt?.cancelAuthentication() } }
    val unlock = {
        if (biometricAvailable && prompt != null) {
            prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Ganu Denu")
                .setSubtitle("Confirm your identity to open your finances")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Cancel")
                .build())
        } else viewModel.showMessage("Set up a fingerprint or supported face unlock in device settings, or sign in with your password.")
    }

    BackHandler(enabled = state.signingUp && !state.busy && state.session == AuthSession.SIGNED_OUT) {
        viewModel.switchMode(false)
    }
    Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
        when (state.session) {
            AuthSession.CHECKING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(color = AuthYellow)
                    Text("Opening your account…", color = Color.LightGray)
                }
            }
            AuthSession.ERROR -> AuthStatusPanel(
                title = "Unable to open your account",
                message = state.error.orEmpty(),
                button = "Retry", onClick = viewModel::retrySession,
                secondary = "Use another account", onSecondary = viewModel::signOut
            )
            AuthSession.LOCKED -> AuthStatusPanel(
                title = if (state.biometricEnrollment) "Enable biometric unlock" else "Welcome back",
                message = state.message ?: if (state.biometricEnrollment)
                    "Confirm your fingerprint or face to enable unlock on this device."
                    else "Unlock your account to continue managing your finances.",
                button = if (state.biometricEnrollment) "Confirm biometric" else "Unlock", onClick = unlock,
                secondary = if (state.biometricEnrollment) "Skip for now" else "Sign in with password",
                onSecondary = if (state.biometricEnrollment) viewModel::skipBiometricEnrollment else viewModel::signOut
            )
            AuthSession.NEEDS_PROFILE -> SignUpScreen(state, viewModel, biometricAvailable, profileOnly = true)
            else -> if (state.signingUp) SignUpScreen(state, viewModel, biometricAvailable)
                else LoginScreen(state, viewModel, biometricAvailable)
        }
    }
    if (state.resetDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissResetDialog,
            containerColor = AuthSurface,
            title = { Text("Reset password", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter your account email to request a reset link.", color = Color.LightGray)
                    AuthField("Email address", state.resetEmail, viewModel::updateResetEmail,
                        error = state.emailError, enabled = !state.busy, keyboardType = KeyboardType.Email)
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::resetPassword, enabled = !state.busy) {
                    Text(if (state.busy) "Sending…" else "Send reset link", color = AuthYellow)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissResetDialog, enabled = !state.busy) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AuthStatusPanel(title: String, message: String, button: String, onClick: () -> Unit, secondary: String, onSecondary: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = AuthYellow, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(24.dp))
        Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(message, color = Color.LightGray, modifier = Modifier.padding(vertical = 20.dp))
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = AuthYellow, contentColor = Color.Black)) { Text(button) }
        TextButton(onClick = onSecondary) { Text(secondary, color = AuthYellow) }
    }
}

private tailrec fun Context.fragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.fragmentActivity()
    else -> null
}
