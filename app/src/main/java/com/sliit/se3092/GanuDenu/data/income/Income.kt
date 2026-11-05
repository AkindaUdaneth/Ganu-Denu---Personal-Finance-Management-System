package com.sliit.se3092.GanuDenu.data.income

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

enum class IncomeSource(val key: String, val title: String) {
    SALARY("salary", "Salary"), FREELANCE("freelance", "Freelance"),
    ADSENSE("adsense", "Google AdSense"), CRYPTO("cryptocurrency", "Cryptocurrency")
}

enum class IncomeStatus(val key: String) { CONFIRMED("confirmed"), ESTIMATED("estimated"), PNL("pnl") }
enum class IncomeCurrency { LKR, USD, USDT, ETH }
enum class IncomeFrequency(val key: String, val title: String) {
    ONE_OFF("one_off", "One-off"), DAILY("daily", "Daily"), WEEKLY("weekly", "Weekly"),
    MONTHLY("monthly", "Monthly"), YEARLY("yearly", "Yearly")
}

data class IncomeEntry(
    val id: String,
    val source: IncomeSource,
    val status: IncomeStatus,
    val originalAmount: String,
    val currency: IncomeCurrency,
    val exchangeRateToLkr: String,
    val amountLkrMinor: Long,
    val date: LocalDate,
    val note: String,
    val frequency: IncomeFrequency = IncomeFrequency.ONE_OFF
)

data class IncomeSnapshot(
    val entries: List<IncomeEntry> = emptyList(),
    val fromCache: Boolean = false,
    val pendingWrites: Boolean = false,
    val skippedRecords: Int = 0
)

data class IncomeDraft(
    val id: String,
    val source: IncomeSource = IncomeSource.SALARY,
    val status: IncomeStatus = IncomeStatus.CONFIRMED,
    val amount: String = "",
    val currency: IncomeCurrency = IncomeCurrency.LKR,
    val exchangeRate: String = "1",
    val date: String = LocalDate.now().toString(),
    val note: String = "",
    val frequency: IncomeFrequency = IncomeFrequency.MONTHLY
) : java.io.Serializable

object IncomeValidation {
    fun toEntry(draft: IncomeDraft): IncomeEntry {
        require(draft.id.isNotBlank()) { "Open the income form again and retry." }
        val input = draft.amount.trim()
        require(!input.contains(',') || input.matches(Regex("^-?[0-9]{1,3}(,[0-9]{3})+([.][0-9]{1,8})?$"))) {
            "Enter a valid amount, e.g. 135,000.00."
        }
        val amount = input.replace(",", "").toBigDecimalOrNull()
        require(amount != null && amount.signum() != 0 && amount.precision() <= 24 && amount.scale() in 0..8) {
            "Enter a non-zero amount with up to 8 decimal places."
        }
        require(draft.status == IncomeStatus.PNL || amount.signum() > 0) { "Cash income must be positive." }
        require(draft.source == IncomeSource.CRYPTO || draft.status != IncomeStatus.PNL) { "P&L is only available for cryptocurrency." }
        require(draft.currency in listOf(IncomeCurrency.LKR, IncomeCurrency.USD) || draft.status == IncomeStatus.PNL) {
            "Use LKR or USD for cash income."
        }
        if (draft.currency == IncomeCurrency.LKR || draft.currency == IncomeCurrency.USD) {
            require(amount.scale() <= 2) { "Use no more than 2 decimal places for LKR or USD." }
        }
        val rate = if (draft.currency == IncomeCurrency.LKR) BigDecimal.ONE else draft.exchangeRate.trim().toBigDecimalOrNull()
        require(rate != null && rate.signum() > 0 && rate <= BigDecimal("1000000000") && rate.scale() in 0..8) {
            "Enter a positive LKR conversion rate (up to 8 decimal places)."
        }
        val converted = amount.multiply(rate).movePointRight(2).setScale(0, RoundingMode.HALF_UP)
        require(converted.abs() >= BigDecimal.ONE && converted.abs() <= BigDecimal("99999999999999")) {
            "The converted amount must be between LKR 0.01 and LKR 999,999,999,999.99."
        }
        val date = runCatching { LocalDate.parse(draft.date.trim()) }.getOrNull()
        require(date != null && date.year in 2000..2100) { "Enter a date as YYYY-MM-DD between 2000 and 2100." }
        require(draft.status == IncomeStatus.ESTIMATED || !date.isAfter(LocalDate.now())) { "Confirmed receipts and trading P&L cannot have a future date." }
        require(draft.note.trim().length <= 120) { "Keep the note within 120 characters." }
        return IncomeEntry(draft.id, draft.source, draft.status, amount.toPlainString(), draft.currency,
            rate.toPlainString(), converted.longValueExact(), date, draft.note.trim(), draft.frequency)
    }
}

data class IncomeSummary(
    val confirmedMinor: Long,
    val estimatedMinor: Long,
    val bySource: Map<IncomeSource, List<IncomeEntry>>
) {
    companion object {
        fun from(entries: List<IncomeEntry>): IncomeSummary {
            fun total(status: IncomeStatus) = entries.filter { it.status == status }.fold(0L) { sum, entry ->
                Math.addExact(sum, entry.amountLkrMinor)
            }
            return IncomeSummary(total(IncomeStatus.CONFIRMED), total(IncomeStatus.ESTIMATED), entries.groupBy { it.source })
        }
    }
}
