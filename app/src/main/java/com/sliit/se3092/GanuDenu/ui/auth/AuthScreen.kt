package com.sliit.se3092.GanuDenu.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sliit.se3092.GanuDenu.ui.theme.CanaryAccent
import com.sliit.se3092.ganudenu.R

@Composable
fun AuthScreen(viewModel: AuthViewModel) = CanaryAccent {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(enabled = state.signingUp && !state.busy && state.session == AuthSession.SIGNED_OUT) {
        viewModel.switchMode(false)
    }
    Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
        when (state.session) {
            AuthSession.CHECKING -> Box(
                Modifier.fillMaxSize().safeDrawingPadding(),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(148.dp).semantics {
                            contentDescription = "Loading your account"
                        },
                        color = AuthYellow,
                        strokeWidth = 3.dp
                    )
                    Image(
                        painter = painterResource(R.drawable.ganu_denu_logo),
                        contentDescription = "Ganu Denu logo",
                        modifier = Modifier.size(112.dp)
                    )
                }
            }
            AuthSession.ERROR -> AuthStatusPanel(
                title = "Unable to open your account",
                message = state.error.orEmpty(),
                button = "Retry", onClick = viewModel::retrySession,
                secondary = "Use another account", onSecondary = viewModel::signOut
            )
            AuthSession.NEEDS_PROFILE -> SignUpScreen(state, viewModel, profileOnly = true)
            else -> if (state.signingUp) SignUpScreen(state, viewModel)
                else LoginScreen(state, viewModel)
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
            dismissButton = { TextButton(onClick = viewModel::dismissResetDialog, enabled = !state.busy) { Text("Cancel", color = AuthYellow) } }
        )
    }
}

@Composable
private fun AuthStatusPanel(title: String, message: String, button: String, onClick: () -> Unit, secondary: String, onSecondary: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = AuthYellow, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(24.dp))
        Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(message, color = Color.LightGray, modifier = Modifier.padding(vertical = 20.dp))
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = AuthYellow, contentColor = Color.Black)) { Text(button) }
        TextButton(onClick = onSecondary) { Text(secondary, color = AuthYellow) }
    }
}
