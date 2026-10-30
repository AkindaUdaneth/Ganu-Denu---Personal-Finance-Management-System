package com.sliit.se3092.GanuDenu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
val CanaryYellow = Color(0xFFFFE600)
val DarkBackground = Color(0xFF0C0C0C)
val DarkCardBg = Color(0xFF161616)
val CardBorder = Color(0xFF242424)

@Composable
fun ExpensesHubScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDailyManager: () -> Unit,
    onNavigateToRecurringManager: () -> Unit,
    onNavigateToAddRecurring: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val monthName = remember {
        java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
            .format(java.util.Calendar.getInstance().time)
    }

    ExpensesHubContent(
        totalDailyToday = state.totalDailyToday,
        totalDailyMonth = state.totalDailyMonth,
        committedOverheadMonthly = state.committedOverheadMonthly,
        onNavigateBack = onNavigateBack,
        onNavigateToDailyManager = onNavigateToDailyManager,
        onNavigateToRecurringManager = onNavigateToRecurringManager,
        onNavigateToAddRecurring = onNavigateToAddRecurring
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesHubContent(
    totalDailyToday: Double,
    totalDailyMonth: Double,
    committedOverheadMonthly: Double,
    onNavigateBack: () -> Unit,
    onNavigateToDailyManager: () -> Unit,
    onNavigateToRecurringManager: () -> Unit,
    onNavigateToAddRecurring: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Transaction",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Expenses Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .clickable { onNavigateToDailyManager() }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = CanaryYellow,
                            modifier = Modifier.size(34.dp)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Today : LKR ${totalDailyToday.toInt()}",
                                color = Color(0xFFAAAAAA),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Oct : LKR ${totalDailyMonth.toInt()}",
                                color = Color(0xFFAAAAAA),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Daily Expenses",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Log, Edit, & backfill one-off costs like food, rides and shopping",
                        color = Color(0xFF888888),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(
                            onClick = onNavigateToDailyManager,
                            modifier = Modifier
                                .size(36.dp)
                                .background(CardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Daily Expense",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Recurring Expenses Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .clickable { onNavigateToRecurringManager() }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = CanaryYellow,
                            modifier = Modifier.size(34.dp)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Overhead / Mo",
                                color = Color(0xFFAAAAAA),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "LKR ${committedOverheadMonthly.toInt()}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Recurring Expenses",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage fixed bills, gym memberships, & active subscriptions",
                        color = Color(0xFF888888),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(
                            onClick = onNavigateToAddRecurring,
                            modifier = Modifier
                                .size(36.dp)
                                .background(CardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Recurring Expense",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C0C)
@Composable
fun ExpensesHubScreenPreview() {
    ExpensesHubContent(
        totalDailyToday = 0.0,
        totalDailyMonth = 3400.0,
        committedOverheadMonthly = 42000.0,
        onNavigateBack = {},
        onNavigateToDailyManager = {},
        onNavigateToRecurringManager = {},
        onNavigateToAddRecurring = {}
    )
}