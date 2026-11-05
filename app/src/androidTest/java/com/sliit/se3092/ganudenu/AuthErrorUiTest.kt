package com.sliit.se3092.ganudenu

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.firebase.auth.FirebaseAuthException
import com.sliit.se3092.GanuDenu.data.auth.*
import com.sliit.se3092.GanuDenu.ui.auth.*
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AuthErrorUiTest {
    @get:Rule val compose = createComposeRule()
    private val repository = ErrorUiAuthRepository()

    private fun showForm(signUp: Boolean = false): AuthViewModel {
        val vm = AuthViewModel(repository)
        compose.setContent { GanuDenuTheme { AuthScreen(vm) } }
        if (signUp) compose.onNodeWithText("Create account").performScrollTo().performClick()
        return vm
    }

    private fun fillLogin() {
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("qa@example.com")
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("WrongPassword!")
    }

    @Test fun wrongPasswordStaysOnLoginAndShowsAnError() {
        repository.authError = FirebaseAuthException("ERROR_WRONG_PASSWORD", "Wrong password")
        showForm()
        fillLogin()
        compose.onNodeWithText("Sign In").performScrollTo().performClick()
        compose.onNodeWithText("Sign-in failed.", substring = true).assertExists()
        compose.onNodeWithText("Welcome back").assertExists()
        assertNull(repository.currentIdentity)
    }

    @Test fun disabledUserGetsAnActionableError() {
        repository.authError = FirebaseAuthException("ERROR_USER_DISABLED", "Disabled")
        showForm()
        fillLogin()
        compose.onNodeWithText("Sign In").performScrollTo().performClick()
        compose.onNodeWithText("This account has been disabled.", substring = true).assertExists()
        assertNull(repository.currentIdentity)
    }

    @Test fun duplicateSignupEmailKeepsTheFormAndOffersSignIn() {
        repository.authError = FirebaseAuthException("ERROR_EMAIL_ALREADY_IN_USE", "Duplicate")
        showForm(true)
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("QA User")
        compose.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextInput("+94711234567")
        compose.onAllNodes(hasSetTextAction())[2].performScrollTo().performTextInput("qa@example.com")
        compose.onAllNodes(hasSetTextAction())[3].performScrollTo().performTextInput("StrongPassword!")
        compose.onNode(hasText("Create Account") and hasClickAction()).performScrollTo().performClick()
        compose.onNodeWithText("An account already uses this email.", substring = true).assertExists()
        compose.onNodeWithText("Sign in", substring = false).assertExists()
        assertNull(repository.currentIdentity)
    }

    @Test fun resetDialogRejectsInvalidEmailAndThenSendsToTheRequestedAddress() {
        showForm()
        compose.onNodeWithText("Forgot password?").performScrollTo().performClick()
        compose.onNodeWithText("Send reset link").performClick()
        compose.onNodeWithText("Reset password").assertIsDisplayed()
        compose.onAllNodes(hasSetTextAction()).onLast().performTextInput("reset@example.com")
        compose.onNodeWithText("Send reset link").performClick()
        compose.onNodeWithText("If an account exists", substring = true).assertExists()
        assertEquals("reset@example.com", repository.resetRecipient)
    }

    @Test fun profileReadErrorRetriesWithoutOpeningProfileCompletion() {
        repository.failRead = true
        showForm()
        fillLogin()
        compose.onNodeWithText("Sign In").performScrollTo().performClick()
        compose.onNodeWithText("Unable to open your account").assertIsDisplayed()
        compose.onNodeWithText("Complete your profile").assertDoesNotExist()
        repository.failRead = false
        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithText("Unable to open your account").assertDoesNotExist()
        // AuthScreen delegates signed-in navigation to its caller.
        assertNotNull(repository.currentIdentity)
    }
}

private class ErrorUiAuthRepository : AuthRepository {
    override val identities = MutableStateFlow<AuthIdentity?>(null)
    override val currentIdentity get() = identities.value
    var authError: Exception? = null
    var failRead = false
    var resetRecipient: String? = null
    override suspend fun signIn(email: String, password: String) {
        authError?.let { throw it }
        identities.value = AuthIdentity("qa-user", email)
    }
    override suspend fun register(email: String, password: String) = signIn(email, password)
    override suspend fun signInWithGoogle(idToken: String) = Unit
    override suspend fun loadProfile(): UserProfile? {
        check(!failRead) { "Connection failed" }
        return UserProfile("qa-user", "QA User", "+94711234567", "qa@example.com")
    }
    override suspend fun createProfile(name: String, phone: String) = requireNotNull(loadProfile())
    override suspend fun sendPasswordReset(email: String) { resetRecipient = email }
    override fun signOut() { identities.value = null }
}
