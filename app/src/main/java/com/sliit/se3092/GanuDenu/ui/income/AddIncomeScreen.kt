package com.sliit.se3092.GanuDenu.ui.income

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sliit.se3092.GanuDenu.data.income.*
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.sliit.se3092.GanuDenu.ui.theme.CanaryAccent

private val InputGray = Color(0xFF1D1D1D)
private val CardGray = Color(0xFF292929)
private val MutedText = Color.LightGray

@Composable
fun AddIncomeScreen(viewModel: IncomeViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        // Rebuild a new draft after process recreation; ordinary source changes retain inputs.
        if (viewModel.uiState.value.draft == null && viewModel.uiState.value.message == null) viewModel.openLogger()
    }
    LaunchedEffect(state.draft == null, state.message) {
        if (state.draft == null && state.message != null) onSaved()
    }
    val goBack = {
        if (!state.saving) { viewModel.dismissLogger(); onBack() }
    }
    BackHandler { goBack() }
    state.draft?.let { draft ->
        AddIncomeContent(state, draft, goBack, viewModel::selectSource, viewModel::editDraft, viewModel::save)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddIncomeContent(
    state: IncomeUiState, draft: IncomeDraft, onBack: () -> Unit,
    selectSource: (IncomeSource) -> Unit, edit: ((IncomeDraft) -> IncomeDraft) -> Unit, onSave: () -> Unit
) = CanaryAccent {
    var datePickerOpen by remember { mutableStateOf(false) }
    var frequencyOpen by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    Column(Modifier.fillMaxSize().background(Color.Black).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !state.saving) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to income overview", tint = Color.White)
            }
            Text("Add Income", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("INCOME BY SOURCE", color = Color(0xFFE0E0E0), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text("4 Channels Monitored", color = MutedText, fontSize = 9.sp)
            }
            Spacer(Modifier.height(16.dp))
            state.cards.chunked(2).forEach { cards ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    cards.forEach { card ->
                        SourceSelector(card, draft.source == card.source, !state.saving,
                            { focus.clearFocus(); selectSource(card.source) }, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(14.dp))
            FieldLabel("Amount (LKR)")
            TextField(
                value = draft.amount, onValueChange = { value -> edit { it.copy(amount = value) } }, enabled = !state.saving,
                modifier = Modifier.fillMaxWidth().testTag("income-amount"), singleLine = true,
                prefix = { Text("LKR  │  ", color = IncomeYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                placeholder = { Text("135,000.00", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = InputGray, unfocusedContainerColor = InputGray,
                    disabledContainerColor = InputGray, focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = IncomeYellow)
            )
            Helper("Enter the amount received or expected in LKR.")
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    FieldLabel("Payment Date")
                    OutlinedButton(onClick = { focus.clearFocus(); datePickerOpen = true }, enabled = !state.saving,
                        shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, CardGray),
                        contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.fillMaxWidth().height(54.dp).testTag("income-date")) {
                        Text(LocalDate.parse(draft.date).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)),
                            color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.CalendarMonth, null, tint = IncomeYellow, modifier = Modifier.size(18.dp))
                    }
                    Helper("Actual or expected date")
                }
                Column(Modifier.weight(1f)) {
                    FieldLabel("Frequency")
                    Box {
                        OutlinedButton(onClick = { focus.clearFocus(); frequencyOpen = true }, enabled = !state.saving,
                            shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, CardGray),
                            contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.fillMaxWidth().height(54.dp).testTag("income-frequency")) {
                            Text(draft.frequency.title, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Outlined.KeyboardArrowDown, null, tint = MutedText, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = frequencyOpen, onDismissRequest = { frequencyOpen = false }) {
                            IncomeFrequency.entries.forEach { frequency -> DropdownMenuItem(text = { Text(frequency.title) },
                                onClick = { frequencyOpen = false; edit { it.copy(frequency = frequency) } }) }
                        }
                    }
                    Helper("Payment schedule")
                }
            }
            Spacer(Modifier.height(18.dp))
            FieldLabel("Income Status")
            Surface(color = InputGray, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(IncomeStatus.CONFIRMED to "Confirmed", IncomeStatus.ESTIMATED to "Estimated").forEach { (status, title) ->
                        TextButton(onClick = { edit { it.copy(status = status) } }, enabled = !state.saving,
                            shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f).semantics { selected = draft.status == status; role = Role.RadioButton },
                            colors = ButtonDefaults.textButtonColors(containerColor = if (draft.status == status) IncomeYellow else Color.Transparent)) {
                            Text(title, color = if (draft.status == status) Color.Black else MutedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.VerifiedUser, null, tint = IncomeYellow, modifier = Modifier.size(13.dp))
                Text(if (draft.status == IncomeStatus.CONFIRMED) " Money has actually been credited to your account."
                    else " Expected income — not yet received.", color = MutedText, fontSize = 11.sp)
            }
            state.formError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp)) }
            Button(onClick = { focus.clearFocus(); onSave() }, enabled = !state.saving,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 28.dp).height(48.dp), shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = IncomeYellow, contentColor = Color.Black)) {
                if (state.saving) CircularProgressIndicator(Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                else Text("Save Income", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
    if (datePickerOpen) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = LocalDate.parse(draft.date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), yearRange = 2000..2100)
        DatePickerDialog(onDismissRequest = { datePickerOpen = false },
            confirmButton = { TextButton(onClick = {
                picker.selectedDateMillis?.let { millis -> edit { it.copy(date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()) } }
                datePickerOpen = false
            }, enabled = picker.selectedDateMillis != null) { Text("Select", color = IncomeYellow) } },
            dismissButton = { TextButton(onClick = { datePickerOpen = false }) { Text("Cancel", color = IncomeYellow) } }) {
            DatePicker(
                state = picker,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = IncomeYellow,
                    selectedDayContentColor = Color.Black,
                    todayDateBorderColor = IncomeYellow,
                    todayContentColor = IncomeYellow,
                    selectedYearContainerColor = IncomeYellow,
                    selectedYearContentColor = Color.Black,
                    currentYearContentColor = IncomeYellow
                )
            )
        }
    }
}

@Composable
private fun SourceSelector(card: IncomeCardUi, isSelected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val icon = when (card.source) {
        IncomeSource.SALARY -> Icons.Outlined.AccountBalance
        IncomeSource.FREELANCE -> Icons.Outlined.Code
        IncomeSource.ADSENSE -> Icons.Outlined.AttachMoney
        IncomeSource.CRYPTO -> Icons.Outlined.CurrencyBitcoin
    }
    Surface(onClick = onClick, enabled = enabled, color = CardGray, shape = RoundedCornerShape(8.dp),
        border = if (isSelected) BorderStroke(2.dp, IncomeYellow) else BorderStroke(1.dp, Color.DarkGray),
        modifier = modifier.heightIn(min = 108.dp).testTag("income-source-${card.source.key}").semantics { selected = isSelected; role = Role.RadioButton }) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = IncomeYellow, modifier = Modifier.size(15.dp))
                Text(card.badge, color = MutedText, fontSize = 7.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
            Text(card.source.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("LKR ${formatIncomeMoney(card.amountMinor)}", color = if (isSelected) IncomeYellow else Color.White,
                fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(card.highlight ?: card.detail, color = MutedText, fontSize = 9.sp,
                modifier = Modifier.padding(top = 4.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun FieldLabel(text: String) { Text(text, color = Color(0xFFDEDEDE), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 7.dp)) }

@Composable
private fun Helper(text: String) { Text(text, color = MutedText, fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp)) }

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun AddIncomePreview() {
    GanuDenuTheme { AddIncomeContent(IncomeUiState(loading = false), IncomeDraft("preview", amount = "135,000.00"), {}, {}, {}, {}) }
}
