package com.sliit.se3092.GanuDenu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

val CanaryYellow = Color(0xFFFFE600)
val DarkBackground = Color(0xFF000000)
val DarkCardBg = Color(0xFF181818)
val CardBorder = Color(0xFF2C2C2C)
val MutedText = Color(0xFF9E9E9E)
val SubtitleText = Color(0xFFB0B0B0)

@Composable
fun ExpensesHubScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDailyManager: () -> Unit,
    onNavigateToRecurringManager: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val monthName = remember {
        java.text.SimpleDateFormat("MMM", Locale.getDefault())
            .format(java.util.Calendar.getInstance().time)
    }

    ExpensesHubContent(
        totalDailyToday = state.totalDailyToday,
        totalDailyMonth = state.totalDailyMonth,
        committedOverheadToday = 34000.0,
        committedOverheadMonthly = state.committedOverheadMonthly,
        monthName = monthName,
        onNavigateBack = onNavigateBack,
        onNavigateToDailyManager = onNavigateToDailyManager,
        onNavigateToRecurringManager = onNavigateToRecurringManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesHubContent(
    totalDailyToday: Double,
    totalDailyMonth: Double,
    committedOverheadToday: Double = 0.0,
    committedOverheadMonthly: Double,
    monthName: String,
    onNavigateBack: () -> Unit,
    onNavigateToDailyManager: () -> Unit,
    onNavigateToRecurringManager: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val formatter = remember {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ' '
        }
        DecimalFormat("#,###", symbols)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Expenses Hub",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        bottomBar = bottomBar,
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Daily Expenses Card (Explicit height: 235.dp)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .clickable { onNavigateToDailyManager() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 52.dp)
                    ) {
                        Text(
                            text = "Daily Expenses",
                            color = MutedText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Today : LKR ${formatter.format(totalDailyToday.toLong())}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$monthName : LKR ${formatter.format(totalDailyMonth.toLong())}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Log, Edit, & backfill one-off costs like food, rides and shopping",
                            color = SubtitleText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    // Calendar icon top right
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = CanaryYellow,
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.TopEnd)
                    )

                    // Plus button bottom right
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(CanaryYellow, CircleShape)
                            .align(Alignment.BottomEnd)
                            .clickable { onNavigateToDailyManager() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Daily Expense",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Recurring Expenses Card (Explicit height: 235.dp)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .clickable { onNavigateToRecurringManager() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 52.dp)
                    ) {
                        Text(
                            text = "Recurring Expenses",
                            color = MutedText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Today : LKR ${formatter.format(committedOverheadToday.toLong())}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$monthName : LKR ${formatter.format(committedOverheadMonthly.toLong())}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Manage fixed bills, gym memberships, & active subscriptions",
                            color = SubtitleText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    // Refresh icon top right
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = CanaryYellow,
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.TopEnd)
                    )

                    // Plus button bottom right
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(CanaryYellow, CircleShape)
                            .align(Alignment.BottomEnd)
                            .clickable { onNavigateToRecurringManager() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Recurring Expense",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ExpensesHubScreenPreview() {
    ExpensesHubContent(
        totalDailyToday = 0.0,
        totalDailyMonth = 3400.0,
        committedOverheadToday = 34000.0,
        committedOverheadMonthly = 42500.0,
        monthName = "Oct",
        onNavigateBack = {},
        onNavigateToDailyManager = {},
        onNavigateToRecurringManager = {}
    )
}