package com.sliit.se3092.GanuDenu.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val ThemeBlack = Color(0xFF000000)
private val ThemeYellow = Color(0xFFFFEF00)
private val SurfaceDark = Color(0xFF18181B)
private val BorderGray = Color(0xFF3F3F46)

@Composable
fun ProfileFormScreen(viewModel: ProfileFormViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.showSuccessSnackbar) {
        if (state.showSuccessSnackbar) {
            snackbarHostState.showSnackbar(
                message = "Profile updated successfully!",
                duration = SnackbarDuration.Short
            )
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        containerColor = ThemeBlack,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = SurfaceDark,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = ThemeYellow)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(data.visuals.message, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ProfileFormContent(
                state = state,
                onNameChange = viewModel::updateName,
                onEmailChange = viewModel::updateEmail,
                onNewPasswordChange = viewModel::updateNewPassword,
                onConfirmPasswordChange = viewModel::updateConfirmPassword,
                onSaveClick = viewModel::saveProfile
            )
        }
    }
}

@Composable
private fun ProfileFormContent(
    state: ProfileFormUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), // Matched wider padding from Figma
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top App Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Edit Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Profile Avatar & Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFF27272A), CircleShape)
            )
            OutlinedButton(
                onClick = { },
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
            ) {
                Text("Change Photo", fontSize = 14.sp)
            }
        }

        // Form Fields
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            GanuDenuExternalLabelTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Name",
                keyboardType = KeyboardType.Text
            )

            GanuDenuExternalLabelTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                keyboardType = KeyboardType.Email
            )

            GanuDenuExternalLabelTextField(
                value = state.newPassword,
                onValueChange = onNewPasswordChange,
                label = "New Password",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            GanuDenuExternalLabelTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Re-enter the New Password",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ThemeYellow),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Save Changes", color = ThemeBlack, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun GanuDenuExternalLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, color = Color.White, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ThemeBlack,
                unfocusedContainerColor = ThemeBlack,
                focusedBorderColor = BorderGray,
                unfocusedBorderColor = BorderGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = ThemeYellow
            ),
            singleLine = true
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 850)
@Composable
private fun ProfileFormPreview() {
    val mockState = ProfileFormUiState(
        name = "Kavindu",
        email = "kavindu123@gmail.com",
        newPassword = "password123",
        confirmPassword = "password123"
    )

    Scaffold(containerColor = ThemeBlack) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ProfileFormContent(
                state = mockState,
                onNameChange = {},
                onEmailChange = {},
                onNewPasswordChange = {},
                onConfirmPasswordChange = {},
                onSaveClick = {}
            )
        }
    }
}