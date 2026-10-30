package com.sliit.se3092.GanuDenu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
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
import com.sliit.se3092.GanuDenu.data.local.entity.RecurringItemEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecurringExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddRecurring: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    RecurringExpenseContent(
        committedOverheadMonthly = state.committedOverheadMonthly,
        recurringItems = state.recurringItems,
        onNavigateBack = onNavigateBack,
        onNavigateToAddRecurring = onNavigateToAddRecurring,
        onToggleStatus = { item -> viewModel.toggleRecurringStatus(item) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpenseContent(
    committedOverheadMonthly: Double,
    recurringItems: List<RecurringItemEntity>,
    onNavigateBack: () -> Unit,
    onNavigateToAddRecurring: () -> Unit,
    onToggleStatus: (RecurringItemEntity) -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Active Subscriptions",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddRecurring,
                containerColor = CanaryYellow,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subscription",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Committed Overhead Banner
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Committed Overhead:",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "LKR ${currencyFormatter.format(committedOverheadMonthly)} / month",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "October",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Active Subscriptions & Bill",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Subscriptions List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(recurringItems) { item ->
                    val formattedDue = remember(item.nextDueDate) {
                        try {
                            val dayFormat = SimpleDateFormat("d", Locale.getDefault())
                            val day = dayFormat.format(Date(item.nextDueDate)).toIntOrNull() ?: 1
                            val suffix = when {
                                day in 11..13 -> "th"
                                day % 10 == 1 -> "st"
                                day % 10 == 2 -> "nd"
                                day % 10 == 3 -> "rd"
                                else -> "th"
                            }
                            "Due: on $day$suffix"
                        } catch (e: Exception) {
                            "Due: on 1st"
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (item.isActive) CanaryYellow.copy(alpha = 0.45f) else CardBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Leading Icon Badge with softened yellow border
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF222222), CircleShape)
                                        .border(1.dp, CanaryYellow.copy(alpha = 0.45f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when {
                                        item.name.contains("Rent", ignoreCase = true) -> Icons.Default.Home
                                        item.name.contains("Gym", ignoreCase = true) ||
                                                item.name.contains("Fitness", ignoreCase = true) -> Icons.Default.FitnessCenter
                                        else -> Icons.Default.Refresh
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = CanaryYellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = item.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$formattedDue | ${item.category}",
                                        color = Color(0xFFAAAAAA),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "LKR ${currencyFormatter.format(item.amount)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = { onToggleStatus(item) },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (item.isActive) Color(0xFFFF3B30) else CanaryYellow.copy(alpha = 0.45f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (item.isActive) Color(0xFFFF3B30) else CanaryYellow
                                    ),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(
                                        text = if (item.isActive) "Deactivate" else "Activate",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C0C)
@Composable
fun RecurringExpenseScreenPreview() {
    val sampleSubscriptions = listOf(
        RecurringItemEntity(
            userId = "user_default",
            name = "Apartment Rent",
            amount = 34000.0,
            category = "Card",
            classification = "Committed",
            cadence = "Monthly",
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        ),
        RecurringItemEntity(
            userId = "user_default",
            name = "Fitness Gym Membership",
            amount = 8000.0,
            category = "Cash",
            classification = "Committed",
            cadence = "Monthly",
            nextDueDate = System.currentTimeMillis() + 864000000L,
            isActive = true
        )
    )

    RecurringExpenseContent(
        committedOverheadMonthly = 42000.0,
        recurringItems = sampleSubscriptions,
        onNavigateBack = {},
        onNavigateToAddRecurring = {},
        onToggleStatus = {}
    )
}