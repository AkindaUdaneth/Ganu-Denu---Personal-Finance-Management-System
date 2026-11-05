package com.sliit.se3092.GanuDenu.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sliit.se3092.GanuDenu.ui.theme.BorderGray
import com.sliit.se3092.GanuDenu.ui.theme.DangerRed
import com.sliit.se3092.GanuDenu.ui.theme.SurfaceDark
import com.sliit.se3092.GanuDenu.ui.theme.ThemeBlack
import com.sliit.se3092.GanuDenu.ui.theme.ThemeYellow
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFormScreen(viewModel: ProfileFormViewModel, onCloseClick: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProfileImage(context)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.updateProfileImageFromUri(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            cameraUri?.let { viewModel.updateProfileImageFromUri(context, it) }
        }
    }

    fun createTempImageUri(): Uri {
        val tempFile = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(tempFile, "temp_profile.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    LaunchedEffect(state.showSuccessSnackbar, state.errorMessage) {
        if (state.showSuccessSnackbar) {
            snackbarHostState.showSnackbar(
                message = "Profile updated successfully!",
                duration = SnackbarDuration.Short
            )
            viewModel.dismissSnackbar()
        } else if (state.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = "Error: ${state.errorMessage}",
                duration = SnackbarDuration.Long
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
                onPhoneChange = viewModel::updatePhone,
                onEmailChange = viewModel::updateEmail,
                onNewPasswordChange = viewModel::updateNewPassword,
                onConfirmPasswordChange = viewModel::updateConfirmPassword,
                onSaveClick = viewModel::saveProfile,
                onChangePhotoClick = { viewModel.updatePhotoBottomSheetVisibility(true) },
                onCloseClick = onCloseClick
            )

            if (state.showPhotoBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.updatePhotoBottomSheetVisibility(false) },
                    sheetState = sheetState,
                    containerColor = SurfaceDark,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGray) }
                ) {
                    PhotoBottomSheetContent(
                        onCameraClick = {
                            val uri = createTempImageUri()
                            cameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        onGalleryClick = { galleryLauncher.launch("image/*") },
                        onRemoveClick = { viewModel.removeProfileImage(context) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileFormContent(
    state: ProfileFormUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onChangePhotoClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Edit Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFF27272A), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (state.profileImageBitmap != null) {
                    Image(
                        bitmap = state.profileImageBitmap.asImageBitmap(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                }
            }
            OutlinedButton(
                onClick = onChangePhotoClick,
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
            ) {
                Text("Change Photo", fontSize = 14.sp)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            GanuDenuExternalLabelTextField(value = state.name, onValueChange = onNameChange, label = "Name", keyboardType = KeyboardType.Text)

            OutlinedTextField(
                value = state.phone,
                onValueChange = onPhoneChange,
                label = { Text("Phone Number", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ThemeBlack, unfocusedContainerColor = ThemeBlack,
                    focusedBorderColor = BorderGray, unfocusedBorderColor = BorderGray,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    cursorColor = ThemeYellow
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            GanuDenuExternalLabelTextField(value = state.email, onValueChange = onEmailChange, label = "Email", keyboardType = KeyboardType.Email)
            GanuDenuExternalLabelTextField(value = state.newPassword, onValueChange = onNewPasswordChange, label = "New Password", keyboardType = KeyboardType.Password, isPassword = true)
            GanuDenuExternalLabelTextField(value = state.confirmPassword, onValueChange = onConfirmPasswordChange, label = "Re-enter the New Password", keyboardType = KeyboardType.Password, isPassword = true)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ThemeYellow),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Save Changes", color = ThemeBlack, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PhotoBottomSheetContent(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
    ) {
        BottomSheetRow(icon = Icons.Default.CameraAlt, text = "Take a photo", textColor = Color.White, iconColor = Color.LightGray, onClick = onCameraClick)
        BottomSheetRow(icon = Icons.Default.PhotoLibrary, text = "Choose from gallery", textColor = Color.White, iconColor = Color.LightGray, onClick = onGalleryClick)
        BottomSheetRow(icon = Icons.Default.Delete, text = "Remove photo", textColor = DangerRed, iconColor = DangerRed, onClick = onRemoveClick)
    }
}

@Composable
private fun BottomSheetRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    textColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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