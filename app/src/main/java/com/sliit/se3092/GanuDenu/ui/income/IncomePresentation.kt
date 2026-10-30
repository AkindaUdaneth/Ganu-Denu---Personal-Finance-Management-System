package com.sliit.se3092.GanuDenu.ui.income

import com.sliit.se3092.GanuDenu.data.income.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

internal fun formatIncomeMoney(minor: Long): String = NumberFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}.format(BigDecimal.valueOf(minor, 2))

data class IncomeCardUi(
    val source: IncomeSource,
    val badge: String,
    val amountMinor: Long,
    val detail: String,
    val highlight: String? = null
)

internal fun incomeCards(summary: IncomeSummary): List<IncomeCardUi> = IncomeSource.entries.map { source ->
    val entries = summary.bySource[source].orEmpty()
    val pnl = entries.filter { it.status == IncomeStatus.PNL }
    val pnlOnly = source == IncomeSource.CRYPTO && pnl.isNotEmpty() && entries.all { it.status == IncomeStatus.PNL }
    val receipts = entries.filter { it.status == if (pnlOnly) IncomeStatus.PNL else IncomeStatus.CONFIRMED }
    val total = receipts.fold(0L) { sum, entry -> Math.addExact(sum, entry.amountLkrMinor) }
    val pending = entries.filter { it.status == IncomeStatus.ESTIMATED }.fold(0L) { sum, entry -> Math.addExact(sum, entry.amountLkrMinor) }
    val note = receipts.filter { it.note.isNotBlank() }.maxByOrNull { it.date }?.note
    val badge = when (source) {
        IncomeSource.SALARY -> "MONTHLY"
        IncomeSource.FREELANCE -> if (entries.isEmpty()) "NO ENTRIES" else "ACTIVE"
        IncomeSource.ADSENSE -> "WEB"
        IncomeSource.CRYPTO -> if (pnlOnly) "P&L ONLY" else "CRYPTO"
    }
    val detail = when {
        entries.isEmpty() -> "No entries this month"
        pnlOnly -> "Recorded P&L (non-liquid)"
        note != null -> note
        source == IncomeSource.ADSENSE && receipts.isNotEmpty() && receipts.all { it.currency == IncomeCurrency.USD } -> {
            val usd = receipts.fold(BigDecimal.ZERO) { sum, entry -> sum + BigDecimal(entry.originalAmount) }
            "USD ${usd.setScale(2).toPlainString()} equiv · Net"
        }
        receipts.isEmpty() -> "No confirmed receipts yet"
        else -> "${receipts.size} confirmed ${if (receipts.size == 1) "receipt" else "receipts"}"
    }
    val highlights = buildList {
        if (pending > 0) add("LKR ${formatIncomeMoney(pending)} pending")
        if (pnl.isNotEmpty() && !pnlOnly) {
            val pnlTotal = pnl.fold(0L) { sum, entry -> Math.addExact(sum, entry.amountLkrMinor) }
            add("P&L: LKR ${formatIncomeMoney(pnlTotal)} (non-liquid)")
        }
    }
    IncomeCardUi(source, badge, total, detail, highlights.takeIf { it.isNotEmpty() }?.joinToString("\n"))
}
