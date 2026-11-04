package com.sliit.se3092.ganudenu

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.sliit.se3092.GanuDenu.data.income.*
import com.sliit.se3092.GanuDenu.ui.income.*
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import androidx.compose.runtime.mutableStateOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IncomeDashboardTest {
    @get:Rule val compose = createComposeRule()

    @Test fun referenceDashboardRendersTotalsAndAllSourceCards() {
        val today = LocalDate.now()
        fun entry(id: String, source: IncomeSource, status: IncomeStatus, amount: String, minor: Long, note: String = "") =
            IncomeEntry(id, source, status, amount, IncomeCurrency.LKR, "1", minor, today, note)
        val entries = listOf(
            entry("1", IncomeSource.SALARY, IncomeStatus.CONFIRMED, "135000", 13500000, "WSO2 Lanka (Pvt) Ltd"),
            entry("2", IncomeSource.FREELANCE, IncomeStatus.CONFIRMED, "20000", 2000000),
            entry("3", IncomeSource.FREELANCE, IncomeStatus.ESTIMATED, "40000", 4000000),
            entry("4", IncomeSource.ADSENSE, IncomeStatus.CONFIRMED, "25", 750000).copy(
                currency = IncomeCurrency.USD, exchangeRateToLkr = "300"),
            entry("5", IncomeSource.CRYPTO, IncomeStatus.PNL, "50000", 5000000)
        )
        val summary = IncomeSummary.from(entries)
        var opened = false
        compose.setContent {
            GanuDenuTheme { IncomeDashboardContent(IncomeUiState(loading = false,
                snapshot = IncomeSnapshot(entries), summary = summary, cards = incomeCards(summary)),
                {}, { opened = true }, {}, {}) }
        }
        compose.onNodeWithText("Income Dashboard").assertIsDisplayed()
        compose.onNodeWithText("162,500.00").assertIsDisplayed()
        compose.onNodeWithText("40,000.00").assertIsDisplayed()
        IncomeSource.entries.forEach { compose.onNodeWithText(it.title).assertIsDisplayed() }
        compose.onNodeWithText("USD 25.00 equiv · Net").assertIsDisplayed()
        compose.onNodeWithText(" Log Income").assertIsDisplayed()
        val screenshot = compose.onRoot().captureToImage().asAndroidBitmap()
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")?.let { File(it) }
            ?: InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
        requireNotNull(output).mkdirs()
        val file = File(output, "income-dashboard-test.png")
        file.outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
        IncomeSource.entries.forEach { compose.onNodeWithText(it.title).assertHasNoClickAction() }
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        compose.onNodeWithText(" Log Income").performClick()
        compose.runOnIdle { assertEquals(true, opened) }
    }

    @Test fun monthPickerShowsOctoberWhenSelected() {
        val month = mutableStateOf(YearMonth.of(2026, 11))
        compose.setContent {
            GanuDenuTheme {
                IncomeDashboardContent(
                    IncomeUiState(month = month.value, loading = false),
                    {}, {}, {}, {}, onSelectMonth = { month.value = it }
                )
            }
        }
        compose.onNodeWithContentDescription("Choose income month").performClick()
        val october = java.time.Month.OCTOBER.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
        compose.onNodeWithText(october).performClick()
        compose.onNodeWithText("October 2026").assertIsDisplayed()
    }

    @Test fun newAccountShowsZeroAndLogIncomeRemainsAvailable() {
        var opened = false
        compose.setContent {
            GanuDenuTheme { IncomeDashboardContent(IncomeUiState(loading = false), {}, { opened = true }, {}, {}) }
        }
        compose.onAllNodesWithText("0.00").assertCountEquals(2)
        compose.onNodeWithText("No income recorded this month.", substring = true).assertExists()
        compose.onNodeWithText(" Log Income").performClick()
        compose.runOnIdle { assertEquals(true, opened) }
    }
}
