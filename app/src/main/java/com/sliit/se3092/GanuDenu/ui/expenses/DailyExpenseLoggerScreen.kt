package com.sliit.se3092.GanuDenu.ui.expenses

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DailyExpenseLoggerScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    DailyExpenseLoggerContent(
        onNavigateBack = onNavigateBack,
        onSaveExpense = { amount, category, method, dateMillis ->
            viewModel.addDailyExpense(
                amount = amount,
                category = category,
                paymentMethod = method,
                date = dateMillis,
                onSuccess = onNavigateBack
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExpenseLoggerContent(
    onNavigateBack: () -> Unit,
    onSaveExpense: (amount: Double, category: String, method: String, dateMillis: Long) -> Unit
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("PickMe") }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    val categories = listOf("PickMe", "UberEats", "Coffee", "Groceries")
    val paymentMethods = listOf("Cash", "Card", "Secondary Account")

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedCalendar = newCal
        },
        selectedCalendar.get(Calendar.YEAR),
        selectedCalendar.get(Calendar.MONTH),
        selectedCalendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Daily Expense", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Amount Input
                Text("Amount", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("135,000.00", color = Color(0xFF555555)) },
                    prefix = { Text("LKR  |  ", color = CanaryYellow, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedBorderColor = CanaryYellow,
                        unfocusedBorderColor = CardBorder
                    )
                )

                // Category Selection
                Text("Type", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = (selectedCategory == cat),
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CanaryYellow,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkCardBg,
                                labelColor = Color.White
                            )
                        )
                    }
                }
                Text(
                    "Add new +",
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { /* Expand category dialog */ }
                )

                // Date Picker Field (Supports Backfill FR-EXP-008)
                Text("Date", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCardBg, RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable { datePickerDialog.show() }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(dateFormat.format(selectedCalendar.time), color = Color.White, fontSize = 14.sp)
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Pick Date",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Payment Method Selector
                Text("Payment Method", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCardBg, RoundedCornerShape(24.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    paymentMethods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) CanaryYellow else Color.Transparent, RoundedCornerShape(20.dp))
                                .clickable { selectedMethod = method }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                method,
                                color = if (isSelected) Color.Black else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Save Expense CTA raised with bottom padding
            Button(
                onClick = {
                    val parsed = amount.toDoubleOrNull() ?: 0.0
                    if (parsed > 0) {
                        onSaveExpense(parsed, selectedCategory, selectedMethod, selectedCalendar.timeInMillis)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CanaryYellow)
            ) {
                Text("Save Expense", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C0C)
@Composable
fun DailyExpenseLoggerScreenPreview() {
    DailyExpenseLoggerContent(
        onNavigateBack = {},
        onSaveExpense = { _, _, _, _ -> }
    )
}