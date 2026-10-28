package com.sliit.se3092.GanuDenu.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview

// Theme Colors
private val ThemeBlack = Color(0xFF000000)
private val SurfaceDark = Color(0xFF18181B)
private val ThemeYellow = Color(0xFFFFEF00)
private val BorderGray = Color(0xFF3F3F46)

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ThemeBlack
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ThemeYellow)
                }
            }
            is DashboardUiState.Success -> {
                DashboardContent(state)
            }
            is DashboardUiState.Error -> {
                // To be implemented
            }
            is DashboardUiState.Empty -> {
                // To be implemented
            }
        }
    }
}

@Composable
private fun DashboardContent(state: DashboardUiState.Success) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dashboard",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            // Logo placeholder
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(ThemeYellow, RoundedCornerShape(16.dp))
            )
        }

        CurrentBalanceCard(
            confirmed = state.confirmedBalance,
            estimated = state.estimatedIncome
        )
    }
}

@Composable
private fun CurrentBalanceCard(confirmed: Double, estimated: Double) {
    val format = NumberFormat.getNumberInstance(Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Current Balance",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Text(
                    text = "LKR ${format.format(confirmed)}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Confirmed Box
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(text = "Confirmed", color = Color.LightGray, fontSize = 12.sp)
                    Text(text = "LKR ${format.format(confirmed)}", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Estimated Box
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF141416), RoundedCornerShape(8.dp))
                        .border(1.dp, ThemeYellow, RoundedCornerShape(8.dp)) // Dashed border requires custom drawing, using solid for MVP
                        .padding(12.dp)
                ) {
                    Text(text = "Estimated", color = ThemeYellow, fontSize = 12.sp)
                    Text(text = "LKR ${format.format(estimated)}", color = ThemeYellow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DashboardPreview() {
    val mockState = DashboardUiState.Success(
        confirmedBalance = 114500.0,
        estimatedIncome = 135000.0,
        committedSpent = 31320.0,
        discretionarySpent = 22680.0,
        goalCurrentSaved = 11200.0,
        goalTargetAmount = 49000.0,
        requiredMonthlySavings = 39900.0,
        activeSubscriptions = emptyList()
    )

    DashboardContent(state = mockState)
}