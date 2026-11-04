package com.sliit.se3092.ganudenu

import androidx.compose.ui.test.*
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.sliit.se3092.GanuDenu.MainActivity
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sliit.se3092.GanuDenu.data.income.*
import com.sliit.se3092.GanuDenu.ui.navigation.AppNavHost
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class IncomeNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val repository = NavigationIncomeRepository()

    @Before fun installFakeRepository() {
        loadKoinModules(module { single<IncomeRepository> { repository } })
    }

    @After fun restoreRepository() {
        loadKoinModules(module {
            single<IncomeRepository> { FirestoreIncomeRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()) }
        })
    }

    @Test fun logIncomeOpensSeparateRouteAndSaveReturnsToOverview() {
        compose.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                GanuDenuTheme {
                    val navigation = rememberNavController()
                    AppNavHost(navigation)
                }
            }
        }
        compose.onNodeWithText("Income").performClick()
        compose.onNodeWithText("Income Dashboard").assertIsDisplayed()
        compose.onNodeWithText(" Log Income").performClick()
        compose.onNodeWithText("Add Income").assertIsDisplayed()
        compose.onNodeWithText("Income Dashboard").assertDoesNotExist()
        compose.onNodeWithText("Income").assertIsDisplayed().assertIsSelected()
        compose.onNodeWithTag("income-source-freelance").performClick().assertIsSelected()
        compose.onNodeWithTag("income-amount").performTextInput("2,500.00")
        compose.onNodeWithTag("income-frequency").performScrollTo().performClick()
        compose.onNodeWithText("Weekly").performClick()
        compose.onNodeWithText("Estimated").performScrollTo().performClick()
        compose.onNodeWithText("Save Income").performScrollTo().performClick()
        compose.waitUntil(5_000) { repository.saved != null }
        compose.onNodeWithText("Income Dashboard").assertIsDisplayed()
        compose.onNodeWithText("Add Income").assertDoesNotExist()
        compose.onNodeWithText("2,500.00").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(IncomeSource.FREELANCE, repository.saved?.source)
            assertEquals(IncomeStatus.ESTIMATED, repository.saved?.status)
            assertEquals(IncomeFrequency.WEEKLY, repository.saved?.frequency)
        }
    }
}

private class NavigationIncomeRepository : IncomeRepository {
    private val snapshots = MutableStateFlow(IncomeSnapshot())
    @Volatile var saved: IncomeEntry? = null
    override fun observeMonth(month: YearMonth) = snapshots
    override suspend fun save(entry: IncomeEntry) {
        saved = entry
        snapshots.value = IncomeSnapshot(listOf(entry))
    }
}
