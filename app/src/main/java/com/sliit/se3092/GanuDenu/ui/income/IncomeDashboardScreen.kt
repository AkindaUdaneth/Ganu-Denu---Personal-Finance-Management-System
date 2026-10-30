package com.sliit.se3092.GanuDenu.ui.income

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sliit.se3092.ganudenu.R
import com.sliit.se3092.GanuDenu.data.income.*
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal val IncomeYellow = Color(0xFFFFEF46)
private val IncomeGray = Color(0xFF292929)

@Composable
fun IncomeDashboardScreen(viewModel: IncomeViewModel, onBack: () -> Unit, onLogIncome: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshMonth() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    IncomeDashboardContent(state, onBack, onLogIncome, viewModel::retry, viewModel::dismissMessage)
}

@Composable
internal fun IncomeDashboardContent(
    state: IncomeUiState, onBack: () -> Unit, onLog: () -> Unit,
    onRetry: () -> Unit, onDismissMessage: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to dashboard", tint = Color.White)
            }
            Text("Income Dashboard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f))
            Image(painterResource(R.drawable.ganu_denu_logo), "Ganu Denu", modifier = Modifier.size(32.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(30.dp))
            Text("INCOME STATUS", color = Color(0xFFE0E0E0), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(state.month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), color = Color(0xFFAAA68C),
                fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
            IncomeStatusCard("CONFIRMED INCOME", state.summary.confirmedMinor)
            Spacer(Modifier.height(14.dp))
            IncomeStatusCard("ESTIMATED INCOME", state.summary.estimatedMinor)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("INCOME BY SOURCE", color = Color(0xFFE0E0E0), fontSize = 17.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("4 Channels Monitored", color = Color(0xFFAAA68C), fontSize = 9.sp)
            }
            Spacer(Modifier.height(18.dp))
            state.cards.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    row.forEach { card -> IncomeSourceCard(card, Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(14.dp))
            }
            if (state.loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = IncomeYellow, strokeWidth = 2.dp)
                    Text(" Loading your income…", color = Color.LightGray, fontSize = 12.sp)
                }
            } else if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                TextButton(onClick = onRetry) { Text("Retry", color = IncomeYellow) }
            } else if (state.snapshot.entries.isEmpty()) {
                Text(if (state.snapshot.fromCache) "No cached income for this month. Connect to sync, or log income."
                    else "No income recorded this month. Tap Log Income to add your first entry.",
                    color = Color.LightGray, fontSize = 12.sp)
            }
            if (state.snapshot.pendingWrites) Text("Changes waiting to sync", color = IncomeYellow, fontSize = 11.sp)
            else if (state.snapshot.fromCache && state.snapshot.entries.isNotEmpty()) Text("Showing cached income · waiting for server", color = Color.LightGray, fontSize = 11.sp)
            if (state.snapshot.skippedRecords > 0) Text("${state.snapshot.skippedRecords} invalid records could not be displayed. Totals may be incomplete.",
                color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            state.message?.let {
                Text(it, color = IncomeYellow, modifier = Modifier.padding(top = 10.dp), fontSize = 12.sp)
                TextButton(onClick = onDismissMessage) { Text("Dismiss", color = IncomeYellow) }
            }
            Spacer(Modifier.height(12.dp))
        }
        Button(onClick = onLog, modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 20.dp).height(46.dp),
            shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = IncomeYellow, contentColor = Color.Black)) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(" Log Income", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun IncomeStatusCard(title: String, amount: Long) {
    Surface(color = IncomeGray, shape = RoundedCornerShape(9.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(IncomeYellow, CircleShape))
                Text(title, color = Color(0xFFE5E5E5), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(start = 12.dp))
                Icon(Icons.Outlined.Paid, null, tint = IncomeYellow, modifier = Modifier.size(13.dp))
            }
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Bottom) {
                Text("LKR", color = Color(0xFFBDB99C), fontSize = 12.sp, modifier = Modifier.padding(end = 9.dp, bottom = 5.dp))
                Text(formatIncomeMoney(amount), color = IncomeYellow, fontSize = 30.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun IncomeSourceCard(card: IncomeCardUi, modifier: Modifier) {
    val icon: ImageVector = when (card.source) {
        IncomeSource.SALARY -> Icons.Outlined.AccountBalance
        IncomeSource.FREELANCE -> Icons.Outlined.Code
        IncomeSource.ADSENSE -> Icons.Outlined.AttachMoney
        IncomeSource.CRYPTO -> Icons.Outlined.CurrencyBitcoin
    }
    Surface(color = Color.Black, shape = RoundedCornerShape(9.dp),
        border = BorderStroke(0.75.dp, IncomeYellow), modifier = modifier.heightIn(min = 152.dp)) {
        Column(Modifier.padding(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = IncomeYellow, modifier = Modifier.size(18.dp))
                Text(card.badge, color = Color(0xFFAAA68C), fontSize = 8.sp, modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
            Text(card.source.title, color = Color(0xFFE5E5E5), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                modifier = Modifier.padding(top = 13.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            val prefix = if (card.source == IncomeSource.CRYPTO && card.amountMinor > 0) "+" else ""
            Text("${prefix}LKR ${formatIncomeMoney(card.amountMinor)}",
                color = if (card.source == IncomeSource.SALARY || card.source == IncomeSource.CRYPTO) IncomeYellow else Color(0xFFE5E5E5),
                fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            card.highlight?.let { Text(it, color = IncomeYellow, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp), maxLines = 2) }
            Text(card.detail, color = Color(0xFFBDB99C), fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun IncomeDashboardPreview() {
    // Design-only examples; production always observes the authenticated user's Firestore data.
    val today = LocalDate.now()
    val entries = listOf(
        IncomeEntry("1", IncomeSource.SALARY, IncomeStatus.CONFIRMED, "135000", IncomeCurrency.LKR, "1", 13500000, today, "WSO2 Lanka (Pvt) Ltd"),
        IncomeEntry("2", IncomeSource.FREELANCE, IncomeStatus.CONFIRMED, "20000", IncomeCurrency.LKR, "1", 2000000, today, ""),
        IncomeEntry("3", IncomeSource.FREELANCE, IncomeStatus.ESTIMATED, "40000", IncomeCurrency.LKR, "1", 4000000, today, ""),
        IncomeEntry("4", IncomeSource.ADSENSE, IncomeStatus.CONFIRMED, "25", IncomeCurrency.USD, "300", 750000, today, ""),
        IncomeEntry("5", IncomeSource.CRYPTO, IncomeStatus.PNL, "50000", IncomeCurrency.LKR, "1", 5000000, today, "")
    )
    val summary = IncomeSummary.from(entries)
    GanuDenuTheme { IncomeDashboardContent(IncomeUiState(loading = false, snapshot = IncomeSnapshot(entries),
        summary = summary, cards = incomeCards(summary)), {}, {}, {}, {}) }
}
