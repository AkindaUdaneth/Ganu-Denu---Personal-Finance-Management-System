package com.sliit.se3092.GanuDenu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Tv
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
import java.util.Locale

@Composable
fun AddRecurringExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    AddRecurringExpenseContent(
        onNavigateBack = onNavigateBack,
        onSaveExpense = { title, amount, dueDay, method ->
            viewModel.addRecurringExpense(title, amount, dueDay, method) {
                scheduleRecurringExpenseReminder(context, title, amount, dueDay)
                onNavigateBack()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringExpenseContent(
    onNavigateBack: () -> Unit,
    onSaveExpense: (title: String, amount: Double, dueDay: Int, method: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("25") }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var selectedIconIndex by remember { mutableIntStateOf(0) }

    val iconOptions = listOf(
        Pair("Rent", Icons.Default.Home),
        Pair("Gym", Icons.Default.FitnessCenter),
        Pair("Subscription", Icons.Default.Tv),
        Pair("Bill", Icons.Default.Receipt),
        Pair("Software", Icons.Default.AutoAwesome),
        Pair("Transport", Icons.Default.DirectionsCar)
    )

    val paymentMethods = listOf("Cash", "Card", "Secondary Account")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Committed Expense",
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
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Field
            Text(
                text = "Amount",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder = { Text("135,000.00", color = Color(0xFF555555)) },
                prefix = { Text("LKR  |  ", color = CanaryYellow, fontWeight = FontWeight.Bold) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg,
                    focusedBorderColor = CanaryYellow.copy(alpha = 0.45f),
                    unfocusedBorderColor = CardBorder
                )
            )

            // Type / Title Field
            Text(
                text = "Type",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Rent", color = Color(0xFF555555)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = CanaryYellow,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg,
                    focusedBorderColor = CanaryYellow.copy(alpha = 0.45f),
                    unfocusedBorderColor = CardBorder
                )
            )

            // Select Icon Section
            Text(
                text = "Select Icon",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                iconOptions.forEachIndexed { index, (name, icon) ->
                    val isSelected = selectedIconIndex == index
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (isSelected) CanaryYellow else DarkCardBg,
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CanaryYellow else CardBorder,
                                shape = CircleShape
                            )
                            .clickable { selectedIconIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = name,
                            tint = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Remind me on (Due Day) Field
            Text(
                text = "Remind me on (Day of Month: 1-31)",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = dueDay,
                onValueChange = { newValue ->
                    val digits = newValue.filter { it.isDigit() }
                    if (digits.isEmpty()) {
                        dueDay = ""
                    } else if (digits.length <= 2) {
                        val dayVal = digits.toIntOrNull()
                        if (dayVal != null && dayVal in 1..31) {
                            dueDay = digits
                        }
                    }
                },
                placeholder = { Text("25", color = Color(0xFF555555)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = CanaryYellow,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(18.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkCardBg,
                    unfocusedContainerColor = DarkCardBg,
                    focusedBorderColor = CanaryYellow.copy(alpha = 0.45f),
                    unfocusedBorderColor = CardBorder
                )
            )

            // Payment Method Selector
            Text(
                text = "Payment Method",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
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
                            .background(
                                if (isSelected) CanaryYellow else Color.Transparent,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedMethod = method }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = method,
                            color = if (isSelected) Color.Black else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Expense CTA
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                    val parsedDay = (dueDay.toIntOrNull() ?: 25).coerceIn(1, 31)
                    val selectedIconTag = iconOptions[selectedIconIndex].first
                    val formattedTitle = if (title.lowercase(Locale.ROOT).contains(selectedIconTag.lowercase(Locale.ROOT))) {
                        title.trim()
                    } else {
                        "${title.trim()} ($selectedIconTag)"
                    }

                    if (title.isNotBlank() && parsedAmount > 0) {
                        onSaveExpense(formattedTitle, parsedAmount, parsedDay, selectedMethod)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CanaryYellow)
            ) {
                Text(
                    text = "Save Expense",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0C0C)
@Composable
fun AddRecurringExpenseScreenPreview() {
    AddRecurringExpenseContent(
        onNavigateBack = {},
        onSaveExpense = { _, _, _, _ -> }
    )
}
