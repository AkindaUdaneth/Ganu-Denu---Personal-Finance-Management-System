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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sliit.se3092.GanuDenu.data.local.entity.ExpenseEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyExpensesManagerScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogger: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    DailyExpensesManagerContent(
        totalMonthAmount = state.totalDailyMonth,
        currentMonthName = state.currentMonthName.ifBlank { "November" },
        expenses = state.dailyExpenses,
        onNavigateBack = onNavigateBack,
        onNavigateToLogger = onNavigateToLogger
    )
}

sealed class ExpenseUiItem {
    data class Header(val monthTitle: String) : ExpenseUiItem()
    data class Expense(val expense: ExpenseEntity) : ExpenseUiItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExpensesManagerContent(
    totalMonthAmount: Double,
    currentMonthName: String,
    expenses: List<ExpenseEntity>,
    onNavigateBack: () -> Unit,
    onNavigateToLogger: () -> Unit
) {
    var activeFilter by remember { mutableStateOf("All") }
    val itemDateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val monthFormat = remember { SimpleDateFormat("MMMM", Locale.getDefault()) }
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
                        text = "Daily Expenses Manager",
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
                onClick = onNavigateToLogger,
                containerColor = CanaryYellow,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
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
            // Header summary card matching Figma
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
                        text = "Daily Expenses ($currentMonthName)",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "LKR ${currencyFormatter.format(totalMonthAmount)}",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Discretionary",
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "UberEats", "PickMe").forEach { filterTag ->
                    val isSelected = activeFilter == filterTag
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeFilter = filterTag },
                        shape = RoundedCornerShape(20.dp),
                        label = {
                            Text(
                                text = filterTag,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CanaryYellow else CardBorder,
                            selectedBorderColor = CanaryYellow
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CanaryYellow,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkCardBg,
                            labelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filtered and Month-Grouped Transactions List
            val filteredList = if (activeFilter == "All") {
                expenses
            } else {
                expenses.filter { it.category.contains(activeFilter, ignoreCase = true) }
            }.sortedByDescending { it.date }

            val uiItems: List<ExpenseUiItem> = remember(filteredList, monthFormat) {
                val result = mutableListOf<ExpenseUiItem>()
                var lastMonth: String? = null
                for (item in filteredList) {
                    val monthName = monthFormat.format(Date(item.date))
                    if (monthName != lastMonth) {
                        result.add(ExpenseUiItem.Header(monthName))
                        lastMonth = monthName
                    }
                    result.add(ExpenseUiItem.Expense(item))
                }
                result
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = uiItems,
                    key = { uiItem ->
                        when (uiItem) {
                            is ExpenseUiItem.Header -> "header_${uiItem.monthTitle}"
                            is ExpenseUiItem.Expense -> "expense_${uiItem.expense.expenseId}"
                        }
                    },
                    itemContent = { uiItem ->
                        when (uiItem) {
                            is ExpenseUiItem.Header -> {
                                Text(
                                    text = uiItem.monthTitle,
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                )
                            }
                            is ExpenseUiItem.Expense -> {
                                val item = uiItem.expense
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = CanaryYellow.copy(alpha = 0.45f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Category Icon Badge with matching softened border
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color(0xFF222222), CircleShape)
                                                    .border(1.dp, CanaryYellow.copy(alpha = 0.45f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val catLower = item.category.lowercase(Locale.ROOT)
                                                val icon = when {
                                                    catLower.contains("pickme") || catLower.contains("ride") || catLower.contains("car") || catLower.contains("transport") -> Icons.Default.DirectionsCar
                                                    catLower.contains("uber") || catLower.contains("food") || catLower.contains("dining") || catLower.contains("eat") -> Icons.Default.Fastfood
                                                    catLower.contains("coffee") || catLower.contains("cafe") || catLower.contains("tea") -> Icons.Default.LocalCafe
                                                    catLower.contains("fuel") || catLower.contains("gas") || catLower.contains("petrol") -> Icons.Default.LocalGasStation
                                                    catLower.contains("medical") || catLower.contains("doctor") || catLower.contains("hospital") || catLower.contains("health") || catLower.contains("pharmacy") -> Icons.Default.LocalHospital
                                                    else -> Icons.Default.ShoppingCart
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
                                                    text = item.category,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${itemDateFormat.format(Date(item.date))} | ${item.paymentMethod}",
                                                    color = Color(0xFFAAAAAA),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Text(
                                            text = "LKR ${currencyFormatter.format(item.amount)}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C0C)
@Composable
fun DailyExpensesManagerScreenPreview() {
    val sampleExpenses = listOf(
        ExpenseEntity(
            userId = "user_default",
            amount = 2400.0,
            category = "UberEats",
            paymentMethod = "Cash",
            classification = "Discretionary",
            date = System.currentTimeMillis() - 86400000L
        ),
        ExpenseEntity(
            userId = "user_default",
            amount = 500.0,
            category = "Pickme Ride",
            paymentMethod = "Card",
            classification = "Discretionary",
            date = System.currentTimeMillis() - 172800000L
        ),
        ExpenseEntity(
            userId = "user_default",
            amount = 500.0,
            category = "Pickme Ride",
            paymentMethod = "Card",
            classification = "Discretionary",
            date = System.currentTimeMillis() - 172800000L
        )
    )

    DailyExpensesManagerContent(
        totalMonthAmount = 3400.0,
        currentMonthName = "November",
        expenses = sampleExpenses,
        onNavigateBack = {},
        onNavigateToLogger = {}
    )
}
