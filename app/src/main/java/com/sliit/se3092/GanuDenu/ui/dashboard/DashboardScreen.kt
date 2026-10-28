package com.sliit.se3092.GanuDenu.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.sliit.se3092.ganudenu.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Receipt

// Theme Colors
private val ThemeBlack = Color(0xFF000000)
private val SurfaceDark = Color(0xFF18181B)
private val ThemeYellow = Color(0xFFFFEF00)
private val BorderGray = Color(0xFF3F3F46)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onGoalClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
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
                is DashboardUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ThemeYellow)
                    }
                }
                is DashboardUiState.Success -> {
                    DashboardContent(
                        state = state,
                        onGoalClick = onGoalClick,
                        onAnalyticsClick = onAnalyticsClick,
                        onSettingsClick = onSettingsClick
                    )
                }
                is DashboardUiState.Error -> {}
                is DashboardUiState.Empty -> {}
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    onGoalClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
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
            Image(
                painter = painterResource(id = R.drawable.ganu_denu_logo),
                contentDescription = "Settings",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onSettingsClick() }
            )
        }

        CurrentBalanceCard(
            confirmed = state.confirmedBalance,
            estimated = state.estimatedIncome
        )

        BehavioralSplitCard(
            committed = state.committedSpent,
            discretionary = state.discretionarySpent,
            onAnalyticsClick = onAnalyticsClick
        )

        GoalProgressCard(
            currentSaved = state.goalCurrentSaved,
            targetAmount = state.goalTargetAmount,
            requiredMonthly = state.requiredMonthlySavings,
            onGoalClick = onGoalClick
        )

        ActiveSubscriptionsCard(
            subscriptions = state.activeSubscriptions
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
                Text("Current Balance", color = Color.LightGray, fontSize = 14.sp)
                Text("LKR ${format.format(confirmed)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Confirmed", color = Color.LightGray, fontSize = 12.sp)
                    Text("LKR ${format.format(confirmed)}", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF141416), RoundedCornerShape(8.dp))
                        .border(1.dp, ThemeYellow, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Estimated", color = ThemeYellow, fontSize = 12.sp)
                    Text("LKR ${format.format(estimated)}", color = ThemeYellow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BehavioralSplitCard(committed: Double, discretionary: Double, onAnalyticsClick: () -> Unit) {
    val total = committed + discretionary
    val committedWeight = if (total > 0) (committed / total).toFloat() else 0.5f
    val discretionaryWeight = if (total > 0) (discretionary / total).toFloat() else 0.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Where it went this month", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(0xFF27272A), RoundedCornerShape(20.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(committedWeight)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Committed", color = Color.White, fontSize = 14.sp)
                }

                Box(
                    modifier = Modifier
                        .weight(discretionaryWeight)
                        .fillMaxHeight()
                        .background(ThemeYellow, RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                        .clickable { onAnalyticsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Discretionary", color = ThemeBlack, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun GoalProgressCard(currentSaved: Double, targetAmount: Double, requiredMonthly: Double, onGoalClick: () -> Unit) {
    val format = NumberFormat.getNumberInstance(Locale.US)
    val progress = if (targetAmount > 0) (currentSaved / targetAmount).toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGoalClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416)),
        border = BorderStroke(1.dp, ThemeYellow),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("MacBook Pro M4 Goal", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("LKR ${format.format(currentSaved)} / LKR ${format.format(targetAmount)}", color = Color.LightGray, fontSize = 14.sp)

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = ThemeYellow,
                trackColor = BorderGray,
            )

            Row {
                Text("Required monthly savings: ", color = Color.LightGray, fontSize = 12.sp)
                Text("LKR ${format.format(requiredMonthly)}", color = ThemeYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionsCard(subscriptions: List<SubscriptionItem>) {
    val format = NumberFormat.getNumberInstance(Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Active Subscriptions", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            subscriptions.forEach { sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141416), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(ThemeYellow, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (sub.iconType) {
                            "GYM" -> Icons.Default.FitnessCenter
                            "SOFTWARE" -> Icons.Default.AutoAwesome
                            "TRANSPORT" -> Icons.Default.DirectionsCar
                            else -> Icons.Default.Receipt
                        }
                        Icon(icon, contentDescription = sub.name, tint = ThemeBlack, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(sub.name, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("LKR ${format.format(sub.costPerMonth)}/mo", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 800)
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
        activeSubscriptions = listOf(
            SubscriptionItem("Gym Membership", 2500.0, "GYM"),
            SubscriptionItem("Gemini Pro", 7000.0, "SOFTWARE"),
            SubscriptionItem("Uber One", 1500.0, "TRANSPORT")
        )
    )

    Scaffold(containerColor = ThemeBlack) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            DashboardContent(mockState, {}, {}, {})
        }
    }
}