package com.sliit.se3092.ganudenu

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.sliit.se3092.GanuDenu.data.income.*
import com.sliit.se3092.GanuDenu.ui.income.*
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AddIncomeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sourceCardsRetainInputsAndSaveTheSelectedSource() {
        var saved: IncomeEntry? = null
        compose.setContent {
            var draft by remember { mutableStateOf(IncomeDraft("test")) }
            GanuDenuTheme {
                AddIncomeContent(IncomeUiState(loading = false), draft, {},
                    selectSource = { draft = draft.copy(source = it) },
                    edit = { transform -> draft = transform(draft) },
                    onSave = { saved = IncomeValidation.toEntry(draft) })
            }
        }
        compose.onNodeWithTag("income-amount").performTextInput("135,000.00")
        IncomeSource.entries.forEach { source ->
            compose.onNodeWithTag("income-source-${source.key}").performScrollTo().performClick().assertIsSelected()
            compose.onNodeWithTag("income-amount").assertTextContains("135,000.00")
            compose.onNodeWithText("Income Status").assertExists()
            compose.onNodeWithText("Payment Date").assertExists()
            compose.onNodeWithText("Frequency").assertExists()
        }
        compose.onNodeWithText("Save Income").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(IncomeSource.CRYPTO, saved?.source)
            assertEquals(13500000L, saved?.amountLkrMinor)
            assertEquals(IncomeFrequency.MONTHLY, saved?.frequency)
        }
    }

    @Test fun addIncomeMatchesCommonFormDesign() {
        compose.setContent {
            GanuDenuTheme { AddIncomeContent(IncomeUiState(loading = false),
                IncomeDraft("preview", amount = "135,000.00"), {}, {}, {}, {}) }
        }
        compose.onNodeWithText("Add Income").assertIsDisplayed()
        compose.onNodeWithTag("income-source-salary").assertIsSelected()
        compose.onNodeWithText("Amount (LKR)").assertIsDisplayed()
        compose.onNodeWithText("Save Income").performScrollTo().assertIsDisplayed()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")?.let { File(it) }
            ?: InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
        requireNotNull(output).mkdirs()
        File(output, "add-income-test.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
