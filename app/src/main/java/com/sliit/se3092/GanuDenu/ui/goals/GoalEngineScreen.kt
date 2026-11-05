package com.sliit.se3092.GanuDenu.ui.goals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.sliit.se3092.GanuDenu.ui.theme.BorderGray
import com.sliit.se3092.GanuDenu.ui.theme.DangerRed
import com.sliit.se3092.GanuDenu.ui.theme.SurfaceDark
import com.sliit.se3092.GanuDenu.ui.theme.ThemeBlack
import com.sliit.se3092.GanuDenu.ui.theme.ThemeYellow

@Composable
fun GoalEngineScreen(
    viewModel: GoalEngineViewModel,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ThemeBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is GoalEngineUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ThemeYellow)
                    }
                }
                is GoalEngineUiState.Success -> {
                    GoalEngineContent(
                        state = state,
                        onImpulseChanged = viewModel::onImpulseExpenseChanged,
                        onBackClick = onBackClick,
                        onEditClick = onEditClick
                    )
                }
                is GoalEngineUiState.Error -> {}
            }
        }
    }
}

@Composable
private fun GoalEngineContent(
    state: GoalEngineUiState.Success,
    onImpulseChanged: (String) -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val format = NumberFormat.getNumberInstance(Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = state.goalName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onEditClick,
                border = BorderStroke(1.dp, ThemeYellow),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeYellow),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Circular Progress Ring
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(220.dp)) {
                drawArc(
                    color = BorderGray,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 32.dp.toPx(), cap = StrokeCap.Butt)
                )
                drawArc(
                    color = ThemeYellow,
                    startAngle = -90f,
                    sweepAngle = (state.progressPercentage / 100 * 360).toFloat(),
                    useCenter = false,
                    style = Stroke(width = 32.dp.toPx(), cap = StrokeCap.Butt)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${state.progressPercentage}%", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Text(text = "Complete", color = Color.LightGray, fontSize = 16.sp)
            }
        }

        Text(
            text = "LKR ${format.format(state.currentSaved)} / LKR ${format.format(state.targetAmount)}",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Target Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, Color(0xFF27272A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Target Date", color = Color.LightGray, fontSize = 12.sp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeBlack, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("${state.targetDateMonths} Months", color = Color.White, fontSize = 16.sp)
                }

                Text(
                    text = "Required Monthly Savings: LKR ${format.format(state.requiredMonthlySavings)}",
                    color = ThemeYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Impulse Checker
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, Color(0xFF27272A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text("Impulse Checker", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("What if I spend this?", color = Color.LightGray, fontSize = 14.sp)
                }

                OutlinedTextField(
                    value = state.impulseExpenseInput,
                    onValueChange = onImpulseChanged,
                    label = { Text("Hypothetical Expense") },
                    prefix = { Text("LKR ", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ThemeBlack,
                        unfocusedContainerColor = ThemeBlack,
                        focusedBorderColor = ThemeYellow,
                        unfocusedBorderColor = ThemeYellow,
                        focusedLabelColor = ThemeYellow,
                        unfocusedLabelColor = ThemeYellow,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (state.impulseDelayDays != null && state.impulseDelayDays > 0) {
                    Text(
                        text = "This pushes your MacBook deadline back by ${state.impulseDelayDays} days",
                        color = DangerRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 850)
@Composable
private fun GoalEnginePreview() {
    val mockState = GoalEngineUiState.Success(
        goalName = "MacBook Pro M4",
        currentSaved = 11200.0,
        targetAmount = 490000.0,
        targetDateMonths = 12,
        requiredMonthlySavings = 39900.0,
        progressPercentage = 2.3,
        impulseExpenseInput = "24000",
        impulseDelayDays = 18
    )

    Scaffold(containerColor = ThemeBlack) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            GoalEngineContent(mockState, {}, {}, {})
        }
    }
}