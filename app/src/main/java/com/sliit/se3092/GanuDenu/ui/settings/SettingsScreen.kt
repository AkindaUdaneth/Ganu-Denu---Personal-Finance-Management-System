package com.sliit.se3092.GanuDenu.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.sliit.se3092.GanuDenu.ui.theme.BorderGray
import com.sliit.se3092.GanuDenu.ui.theme.DangerRed
import com.sliit.se3092.GanuDenu.ui.theme.SurfaceDark
import com.sliit.se3092.GanuDenu.ui.theme.ThemeBlack
import com.sliit.se3092.GanuDenu.ui.theme.ThemeYellow

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfileImage(context)
    }

    Scaffold(
        containerColor = ThemeBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SettingsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ThemeYellow)
                    }
                }
                is SettingsUiState.Success -> {
                    SettingsContent(
                        state = state,
                        onToggleNotifications = viewModel::toggleNotifications,
                        onShowLogoutDialog = { viewModel.updateLogoutDialogVisibility(true) },
                        onShowDeleteDialog = { viewModel.updateDeleteDialogVisibility(true) },
                        onBackClick = onBackClick,
                        onEditProfileClick = onEditProfileClick
                    )

                    if (state.showLogoutDialog) {
                        LogoutDialog(
                            onDismiss = { viewModel.updateLogoutDialogVisibility(false) },
                            onConfirm = onLogout
                        )
                    }

                    if (state.showDeleteDialog) {
                        DeleteAccountDialog(
                            onDismiss = { viewModel.updateDeleteDialogVisibility(false) },
                            onConfirm = { /* Handle delete logic later */ }
                        )
                    }
                }
                is SettingsUiState.Error -> {
                    Column(Modifier.align(Alignment.Center).padding(24.dp)) {
                        Text(state.message, color = Color.White)
                        TextButton(onClick = viewModel::loadProfileData) { Text("Retry") }
                        TextButton(onClick = onLogout) { Text("Sign out") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState.Success,
    onToggleNotifications: (Boolean) -> Unit,
    onShowLogoutDialog: () -> Unit,
    onShowDeleteDialog: () -> Unit,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top App Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, BorderGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (state.profileImageBitmap == null) ThemeYellow else Color.Transparent, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.profileImageBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = state.profileImageBitmap.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = state.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = ThemeBlack,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = state.userName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = state.userEmail, color = Color.LightGray, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onEditProfileClick,
                    border = BorderStroke(1.dp, ThemeYellow),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeYellow),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Edit", fontSize = 12.sp)
                }
            }
        }

        // Settings List
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, BorderGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications", color = Color.White, fontSize = 14.sp)
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = onToggleNotifications,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ThemeBlack,
                            checkedTrackColor = ThemeYellow,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
                Divider(color = BorderGray)
                SettingsListItem("Currency", "LKR")
                Divider(color = BorderGray)
                SettingsListItem("Language", "English")
                Divider(color = BorderGray)
                SettingsListItem("Help & Support")
                Divider(color = BorderGray)
                SettingsListItem("About")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Button(
            onClick = onShowLogoutDialog,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out", color = Color.White)
        }

        OutlinedButton(
            onClick = onShowDeleteDialog,
            border = BorderStroke(1.dp, DangerRed),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Account")
        }
    }
}

@Composable
private fun SettingsListItem(title: String, value: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(value, color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next", tint = Color.LightGray)
        }
    }
}

@Composable
private fun LogoutDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Log out of Ganu Denu?", color = Color.White, fontWeight = FontWeight.Bold) },
        text = { Text("Any unsynced offline data will remain on this device until you log back in.", color = Color.LightGray) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Log Out", color = ThemeYellow, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.LightGray)
            }
        }
    )
}

@Composable
private fun DeleteAccountDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Delete Account?", color = Color.White, fontWeight = FontWeight.Bold) },
        text = { Text("This action cannot be undone. All your financial data, goals, and sync history will be permanently erased.", color = Color.LightGray) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = DangerRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.LightGray)
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 800)
@Composable
private fun SettingsPreview() {
    val mockState = SettingsUiState.Success(
        userName = "Kavindu",
        userEmail = "kavindu123@gmail.com",
        notificationsEnabled = true,
        showLogoutDialog = true,
        showDeleteDialog = true
    )

    Scaffold(containerColor = ThemeBlack) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            SettingsContent(mockState, {}, {}, {}, {}, {})
        }
    }
}