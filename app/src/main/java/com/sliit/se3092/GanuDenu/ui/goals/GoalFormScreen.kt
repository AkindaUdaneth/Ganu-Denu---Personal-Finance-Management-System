package com.sliit.se3092.GanuDenu.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.sliit.se3092.GanuDenu.ui.theme.BorderGray
import com.sliit.se3092.GanuDenu.ui.theme.SurfaceDark
import com.sliit.se3092.GanuDenu.ui.theme.ThemeBlack
import com.sliit.se3092.GanuDenu.ui.theme.ThemeYellow

@Composable
fun GoalFormScreen(viewModel: GoalFormViewModel, onBackClick: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listens to the ViewModel state and triggers the Snackbar animation
    LaunchedEffect(state.showSuccessSnackbar) {
        if (state.showSuccessSnackbar) {
            snackbarHostState.showSnackbar(
                message = "Goal saved successfully!",
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
            GoalFormContent(
                state = state,
                onNameChange = viewModel::updateGoalName,
                onTargetAmountChange = viewModel::updateTargetAmount,
                onTargetDateChange = viewModel::updateTargetDate,
                onCurrentSavedChange = viewModel::updateCurrentSaved,
                onSaveClick = viewModel::saveGoal,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun GoalFormContent(
    state: GoalFormUiState,
    onNameChange: (String) -> Unit,
    onTargetAmountChange: (String) -> Unit,
    onTargetDateChange: (String) -> Unit,
    onCurrentSavedChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
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
            Text(text = "Edit Goal", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            GanuDenuTextField(
                value = state.goalName,
                onValueChange = onNameChange,
                label = "Goal Name",
                keyboardType = KeyboardType.Text
            )

            GanuDenuTextField(
                value = state.targetAmount,
                onValueChange = onTargetAmountChange,
                label = "Target Amount (LKR)",
                keyboardType = KeyboardType.Number
            )

            GanuDenuTextField(
                value = state.targetDate,
                onValueChange = onTargetDateChange,
                label = "Target Date",
                keyboardType = KeyboardType.Text
            )

            GanuDenuTextField(
                value = state.currentSaved,
                onValueChange = onCurrentSavedChange,
                label = "Current Saved (LKR)",
                keyboardType = KeyboardType.Number
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ThemeYellow),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Goal", color = ThemeBlack, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Reusable TextField styled for the Ganu Denu dark theme
@Composable
private fun GanuDenuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ThemeBlack,
            unfocusedContainerColor = ThemeBlack,
            focusedBorderColor = ThemeYellow,
            unfocusedBorderColor = BorderGray,
            focusedLabelColor = ThemeYellow,
            unfocusedLabelColor = Color.LightGray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = ThemeYellow
        ),
        singleLine = true
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 800)
@Composable
private fun GoalFormPreview() {
    val mockState = GoalFormUiState(
        goalName = "MacBook M4 Pro",
        targetAmount = "490000",
        targetDate = "October 30, 2026",
        currentSaved = "11200"
    )

    Scaffold(containerColor = ThemeBlack) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            GoalFormContent(mockState, {}, {}, {}, {}, {}, {})
        }
    }
}