package com.sliit.se3092.GanuDenu.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
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

internal val AuthYellow = Color(0xFFFFEF46)
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
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp).widthIn(max = 480.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
internal fun BiometricOption(checked: Boolean, busy: Boolean, available: Boolean, onChange: (Boolean) -> Unit) {
    Surface(color = AuthSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = AuthYellow, modifier = Modifier.size(28.dp))
            Text("Biometric Unlock", color = Color.White, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(start = 12.dp))
            Switch(checked = checked, onCheckedChange = onChange, enabled = !busy && available,
                colors = SwitchDefaults.colors(checkedTrackColor = AuthYellow, checkedThumbColor = Color.Black))
        }
    }
    if (!available) Text("Set up biometrics in device settings to enable unlock.", color = Color.Gray,
        fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
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
        else Text(label, fontWeight = FontWeight.Bold)
    }
}
