package com.sliit.se3092.GanuDenu.ui.expenses

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
    var showAddCategorySheet by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    val categories = remember { mutableStateListOf("PickMe", "UberEats", "Coffee", "Groceries") }
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

    if (showAddCategorySheet) {
        AddCategoryBottomSheet(
            onDismissRequest = { showAddCategorySheet = false },
            onAddCategory = { newCategory, iconName ->
                val lowerCat = newCategory.lowercase(Locale.ROOT)
                val lowerIcon = iconName.lowercase(Locale.ROOT)
                val formattedCategory = if (lowerCat.contains(lowerIcon) || lowerIcon == "shopping") {
                    newCategory
                } else {
                    "$newCategory ($iconName)"
                }

                if (!categories.contains(formattedCategory)) {
                    categories.add(formattedCategory)
                }
                selectedCategory = formattedCategory
            }
        )
    }

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
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
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
                    modifier = Modifier.clickable { showAddCategorySheet = true }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryBottomSheet(
    onDismissRequest: () -> Unit,
    onAddCategory: (categoryName: String, iconName: String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedIconIndex by remember { mutableIntStateOf(0) }

    val iconOptions = listOf(
        Pair("Shopping", Icons.Default.ShoppingCart),
        Pair("Dining", Icons.Default.Fastfood),
        Pair("Coffee", Icons.Default.LocalCafe),
        Pair("Fuel", Icons.Default.LocalGasStation),
        Pair("Medical", Icons.Default.LocalHospital)
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = DarkCardBg,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFF333333), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "New Category",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Category Name Input Field
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                placeholder = { Text("Category Name", color = Color(0xFF666666), fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CanaryYellow,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground,
                    cursorColor = CanaryYellow
                )
            )

            // Icon Selection Section
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
                                color = if (isSelected) CanaryYellow else DarkBackground,
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

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons (Cancel & Add Category)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val trimmed = categoryName.trim()
                        if (trimmed.isNotBlank()) {
                            onAddCategory(trimmed, iconOptions[selectedIconIndex].first)
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CanaryYellow,
                        disabledContainerColor = CanaryYellow.copy(alpha = 0.4f)
                    ),
                    enabled = categoryName.isNotBlank()
                ) {
                    Text(
                        text = "Add Category",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
