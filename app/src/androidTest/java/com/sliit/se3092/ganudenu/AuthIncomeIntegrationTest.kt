package com.sliit.se3092.ganudenu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.sliit.se3092.GanuDenu.GanuDenuApp
import org.koin.core.context.unloadKoinModules
import com.sliit.se3092.GanuDenu.data.auth.*
import com.sliit.se3092.GanuDenu.data.income.*
import com.sliit.se3092.GanuDenu.ui.auth.AuthViewModel
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

class AuthIncomeIntegrationTest {
    @get:Rule val compose = createComposeRule()
    private val auth = IntegrationAuthRepository()
    private val income = IntegrationIncomeRepository(auth)
    private val preferences = object : BiometricPreferences {
        override fun isEnabled(uid: String) = false
        override fun setEnabled(uid: String, enabled: Boolean) = Unit
    }


    private val testModule = module {
        single<AuthRepository> { auth }
        single<IncomeRepository> { income }
    }

    @Before
    fun overrideRepositories() {
        loadKoinModules(testModule)
    }

    @After
    fun restoreDependencies() {
        unloadKoinModules(testModule)
    }


    @Test fun signupIncomeAndLogoutUseTheSameUserSession() {
        val vm = AuthViewModel(auth, preferences)
        compose.setContent { GanuDenuTheme { GanuDenuApp(vm) } }
        compose.onNodeWithText("Welcome back").assertIsDisplayed()
        compose.onNodeWithText("Create account").performScrollTo().performClick()
        compose.onNode(hasText("Create Account") and hasClickAction()).assertExists()
        val fields = compose.onAllNodes(hasSetTextAction())
        fields[0].performScrollTo().performTextInput("Income Tester")
        fields[1].performScrollTo().performTextInput("+94 71 123 4567")
        fields[2].performScrollTo().performTextInput("income.tester@example.com")
        fields[3].performScrollTo().performTextInput("TestPassword2026!")
        compose.onNode(hasText("Create Account") and hasClickAction()).performScrollTo().performClick()
        compose.onNodeWithText("Income").performClick()
        compose.onNodeWithText("Income Dashboard").assertIsDisplayed()
        compose.onNodeWithText(" Log Income").performClick()
        compose.onNodeWithTag("income-source-adsense").performClick()
        compose.onNodeWithTag("income-amount").performTextInput("7,500.00")
        compose.onNodeWithText("Save Income").performScrollTo().performClick()
        compose.waitUntil(5_000) { income.saved != null }
        compose.onNodeWithText("Income Dashboard").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals("integration-uid", income.savedUid)
            assertEquals(IncomeSource.ADSENSE, income.saved?.source)
            assertEquals(750000L, income.saved?.amountLkrMinor)
            assertEquals("Income Tester", auth.profile?.name)
        }
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Income Tester").assertIsDisplayed()
        compose.onNodeWithText("income.tester@example.com").assertIsDisplayed()
        compose.onNodeWithText("Log Out").performClick()
        compose.onAllNodesWithText("Log Out").filter(hasClickAction()).onLast().performClick()
        compose.onNodeWithText("Welcome back").assertIsDisplayed()
        compose.onNodeWithText("Income Dashboard").assertDoesNotExist()
        compose.runOnIdle { assertNull(auth.currentIdentity) }
    }

    @Test fun existingAccountLoginOpensFinanceNavigation() {
        auth.profile = UserProfile("integration-uid", "Returning User", "+94711234567", "returning@example.com")
        val vm = AuthViewModel(auth, preferences)
        compose.setContent { GanuDenuTheme { GanuDenuApp(vm) } }
        compose.onNodeWithText("Welcome back").assertIsDisplayed()
        val fields = compose.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("returning@example.com")
        fields[1].performTextInput("TestPassword2026!")
        compose.onNodeWithText("Sign In").performScrollTo().performClick()
        compose.onNodeWithText("Income").assertIsDisplayed()
        compose.runOnIdle { assertEquals("integration-uid", auth.currentIdentity?.uid) }
    }
}

private class IntegrationAuthRepository : AuthRepository {
    override val identities = MutableStateFlow<AuthIdentity?>(null)
    override val currentIdentity get() = identities.value
    var profile: UserProfile? = null
    override suspend fun signIn(email: String, password: String) { identities.value = AuthIdentity("integration-uid", email) }
    override suspend fun register(email: String, password: String) = signIn(email, password)
    override suspend fun loadProfile() = profile
    override suspend fun createProfile(name: String, phone: String): UserProfile {
        val identity = requireNotNull(currentIdentity)
        return UserProfile(identity.uid, name, phone, identity.email).also { profile = it }
    }
    override suspend fun sendPasswordReset(email: String) = Unit
    override fun signOut() { identities.value = null }
}

private class IntegrationIncomeRepository(private val auth: AuthRepository) : IncomeRepository {
    private val events = MutableStateFlow(IncomeSnapshot())
    @Volatile var saved: IncomeEntry? = null
    var savedUid: String? = null
    override fun observeMonth(month: YearMonth) = events
    override suspend fun save(entry: IncomeEntry) {
        savedUid = requireNotNull(auth.currentIdentity).uid
        saved = entry
        events.value = IncomeSnapshot(listOf(entry))
    }
}
