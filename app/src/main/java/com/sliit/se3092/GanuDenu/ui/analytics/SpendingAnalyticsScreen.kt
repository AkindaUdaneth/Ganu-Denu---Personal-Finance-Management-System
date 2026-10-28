package com.sliit.se3092.GanuDenu.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.sliit.se3092.GanuDenu.ui.dashboard.GanuDenuBottomNavBar

private val ThemeBlack = Color(0xFF000000)
private val ThemeYellow = Color(0xFFFFEF00)
private val SurfaceDark = Color(0xFF18181B)

@Composable
fun SpendingAnalyticsScreen(viewModel: SpendingAnalyticsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = { GanuDenuBottomNavBar() },
        containerColor = ThemeBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SpendingAnalyticsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ThemeYellow)
                    }
                }
                is SpendingAnalyticsUiState.Success -> {
                    AnalyticsContent(state)
                }
                is SpendingAnalyticsUiState.Error -> {}
            }
        }
    }
}

@Composable
private fun AnalyticsContent(state: SpendingAnalyticsUiState.Success) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top App Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Spending Breakdown",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Time Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip("This Month", true)
            FilterChip("Last Month", false)
            FilterChip("September", false)
        }

        // Donut Chart Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            DonutChart(expenses = state.expenses, totalSpent = state.totalSpent)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val format = NumberFormat.getNumberInstance(Locale.US)
                Text("Total Spent", color = Color.LightGray, fontSize = 14.sp)
                Text(
                    text = "LKR\n${format.format(state.totalSpent)}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        ExpenseLegend(expenses = state.expenses)

        TopExpensesCard(expenses = state.expenses)
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) ThemeYellow else SurfaceDark,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) ThemeBlack else Color.LightGray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun DonutChart(expenses: List<ExpenseCategory>, totalSpent: Double) {
    Canvas(modifier = Modifier.size(200.dp)) {
        var startAngle = -90f // Start from top center
        val strokeWidth = 40.dp.toPx()

        expenses.forEach { expense ->
            val sweepAngle = ((expense.amount / totalSpent) * 360f).toFloat()

            drawArc(
                color = Color(expense.colorHex),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseLegend(expenses: List<ExpenseCategory>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        expenses.forEach { expense ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(expense.colorHex))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = expense.name.split("/")[0], // Truncates "Coffee/Dining" to "Coffee"
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TopExpensesCard(expenses: List<ExpenseCategory>) {
    val format = NumberFormat.getNumberInstance(Locale.US)
    val topExpenses = expenses.sortedByDescending { it.amount }.take(3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416)),
        border = BorderStroke(1.dp, Color(0xFF3F3F46)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Top Expenses",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            topExpenses.forEachIndexed { index, expense ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF09090B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}. ${expense.name}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "LKR ${format.format(expense.amount)}",
                        color = if (index == 0) ThemeYellow else Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, heightDp = 800)
@Composable
private fun AnalyticsPreview() {
    val mockState = SpendingAnalyticsUiState.Success(
        totalSpent = 54000.0,
        expenses = listOf(
            ExpenseCategory("Rent", 15000.0, 0xFFFFD700),
            ExpenseCategory("Coffee/Dining", 24000.0, 0xFFFFEF00),
            ExpenseCategory("PickMe", 5000.0, 0xFFD4AF37),
            ExpenseCategory("UberEats", 7500.0, 0xFF3F3F46),
            ExpenseCategory("Gym", 2500.0, 0xFF27272A)
        )
    )

    Scaffold(
        bottomBar = { GanuDenuBottomNavBar() },
        containerColor = ThemeBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnalyticsContent(state = mockState)
        }
    }
}