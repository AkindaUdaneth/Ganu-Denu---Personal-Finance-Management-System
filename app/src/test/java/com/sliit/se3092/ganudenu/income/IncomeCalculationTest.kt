package com.sliit.se3092.ganudenu.income

import com.sliit.se3092.GanuDenu.data.income.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class IncomeCalculationTest {
    private fun draft(source: IncomeSource = IncomeSource.SALARY, status: IncomeStatus = IncomeStatus.CONFIRMED,
        amount: String = "135000", currency: IncomeCurrency = IncomeCurrency.LKR, rate: String = "1") =
        IncomeDraft("id", source, status, amount, currency, rate, LocalDate.now().toString())

    @Test fun matchesReferenceTotalsWithoutCountingEstimatesOrCryptoAsCash() {
        val entries = listOf(
            IncomeValidation.toEntry(draft()),
            IncomeValidation.toEntry(draft(IncomeSource.FREELANCE, amount = "20000")),
            IncomeValidation.toEntry(draft(IncomeSource.ADSENSE, amount = "25", currency = IncomeCurrency.USD, rate = "300")),
            IncomeValidation.toEntry(draft(IncomeSource.FREELANCE, IncomeStatus.ESTIMATED, "40000")),
            IncomeValidation.toEntry(draft(IncomeSource.CRYPTO, IncomeStatus.PNL, "50000"))
        )
        val totals = IncomeSummary.from(entries)
        assertEquals(16250000L, totals.confirmedMinor)
        assertEquals(4000000L, totals.estimatedMinor)
        assertEquals(4, totals.bySource.size)
    }

    @Test fun originalCurrencyAndReceiptRateArePreservedAndRoundedOnce() {
        val result = IncomeValidation.toEntry(draft(amount = "0.01", currency = IncomeCurrency.USD, rate = "1.5"))
        assertEquals(2L, result.amountLkrMinor)
        assertEquals("0.01", result.originalAmount)
        assertEquals("1.5", result.exchangeRateToLkr)
    }

    @Test fun cryptoLossIsSignedAndDoesNotReduceCashReceipts() {
        val entry = IncomeValidation.toEntry(draft(IncomeSource.CRYPTO, IncomeStatus.PNL, "-0.125", IncomeCurrency.ETH, "1000000"))
        assertEquals(-12500000L, entry.amountLkrMinor)
        assertEquals(0L, IncomeSummary.from(listOf(entry)).confirmedMinor)
    }

    @Test fun rejectsInvalidMoneyAndMismatchedStatuses() {
        listOf(
            draft(amount = "0"), draft(amount = "abc"), draft(amount = "-1"), draft(amount = "1.001"),
            draft(currency = IncomeCurrency.USD, rate = "0"), draft(currency = IncomeCurrency.USD, rate = "-300"),
            draft(currency = IncomeCurrency.USD, rate = "1e100"), draft(status = IncomeStatus.PNL),
            draft(source = IncomeSource.CRYPTO, amount = "-1"), draft(currency = IncomeCurrency.ETH),
            draft(amount = "1,2,3"),
            draft().copy(date = "2026-02-30"), draft().copy(date = LocalDate.now().plusDays(1).toString()),
            draft(amount = "9999999999999999999999999999999")
        ).forEach { invalid -> assertThrows(IllegalArgumentException::class.java) { IncomeValidation.toEntry(invalid) } }
    }

    @Test fun futureExpectedReceiptsAreAllowedButNotConfirmedIncome() {
        val future = IncomeValidation.toEntry(draft(status = IncomeStatus.ESTIMATED).copy(date = LocalDate.now().plusDays(5).toString()))
        assertEquals(0L, IncomeSummary.from(listOf(future)).confirmedMinor)
    }

    @Test fun emptyMonthHasZeroTotals() {
        val totals = IncomeSummary.from(emptyList())
        assertEquals(0L, totals.confirmedMinor)
        assertEquals(0L, totals.estimatedMinor)
    }

    @Test fun commonFormAllowsConfirmedAndEstimatedLkrForAllSources() {
        IncomeSource.entries.forEach { source ->
            listOf(IncomeStatus.CONFIRMED, IncomeStatus.ESTIMATED).forEach { status ->
                val result = IncomeValidation.toEntry(draft(source, status, "135,000.00").copy(frequency = IncomeFrequency.MONTHLY))
                assertEquals(source, result.source)
                assertEquals(status, result.status)
                assertEquals(13500000L, result.amountLkrMinor)
                assertEquals(IncomeFrequency.MONTHLY, result.frequency)
            }
        }
    }
}
