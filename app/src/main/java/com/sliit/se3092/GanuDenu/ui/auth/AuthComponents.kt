package com.sliit.se3092.GanuDenu.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sliit.se3092.GanuDenu.ui.theme.CanaryYellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal val AuthYellow = CanaryYellow
internal val AuthSurface = Color(0xFF1C1C1B)

@Composable
internal fun AuthField(
    label: String, value: String, onChange: (String) -> Unit, placeholder: String = "",
    error: String? = null, enabled: Boolean = true, keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false, leadingIcon: ImageVector? = null,
    imeAction: ImeAction = ImeAction.Next, onDone: () -> Unit = {}
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange, enabled = enabled, singleLine = true, isError = error != null,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) } },
            trailingIcon = if (password) { {
                IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (visible) "Hide password" else "Show password")
                }
            } } else null,
            visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AuthYellow, unfocusedBorderColor = Color(0xFF777777),
                focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = AuthYellow),
            supportingText = error?.let { { Text(it) } }
        )
    }
}

@Composable
internal fun AuthFormContainer(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
internal fun AuthFeedback(error: String?, message: String?) {
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) }
    message?.let { Text(it, color = AuthYellow, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) }
}

@Composable
internal fun AuthSubmitButton(label: String, busy: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !busy, modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AuthYellow, contentColor = Color.Black), shape = CircleShape) {
        if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
        else Text(label, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun GoogleAuthButton(label: String, busy: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = !busy,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF555555)),
        shape = CircleShape
    ) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

internal fun launchGoogleSignIn(
    context: android.content.Context,
    scope: CoroutineScope,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val webClientId = context.getString(com.sliit.se3092.ganudenu.R.string.default_web_client_id)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(webClientId)
        .setFilterByAuthorizedAccounts(false)
        .build()

    val request = androidx.credentials.GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val credentialManager = androidx.credentials.CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                onSuccess(googleIdTokenCredential.idToken)
            } else {
                onError("Unexpected credential type")
            }
        } catch (e: androidx.credentials.exceptions.GetCredentialException) {
            onError(e.localizedMessage ?: "Google sign-in failed")
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Google sign-in failed")
        }
    }
}
