package com.sliit.se3092.ganudenu.auth

import com.sliit.se3092.GanuDenu.data.auth.*
import com.sliit.se3092.GanuDenu.ui.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var preferences: FakeBiometricPreferences

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAuthRepository()
        preferences = FakeBiometricPreferences()
    }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun invalidFormDoesNotCallFirebase() = runTest(dispatcher) {
        val vm = AuthViewModel(repository, preferences)
        runCurrent()
        vm.switchMode(true)
        vm.submit()
        assertNotNull(vm.uiState.value.nameError)
        assertNotNull(vm.uiState.value.emailError)
        assertEquals(0, repository.registrations)
    }

    @Test fun registrationSavesNormalizedProfileAndClearsPassword() = runTest(dispatcher) {
        val vm = registrationForm()
        vm.submit()
        runCurrent()
        assertEquals(AuthSession.SIGNED_IN, vm.uiState.value.session)
        assertEquals("Kasun Perera", repository.profile?.name)
        assertEquals("+94711234567", repository.profile?.phone)
        assertEquals("", vm.uiState.value.password)
        assertEquals(1, repository.registrations)
    }

    @Test fun failedProfileSaveCanRetryWithoutCreatingAnotherAccount() = runTest(dispatcher) {
        val vm = registrationForm()
        repository.failProfileWrite = true
        vm.submit()
        runCurrent()
        assertEquals(AuthSession.NEEDS_PROFILE, vm.uiState.value.session)
        assertNotNull(vm.uiState.value.error)
        repository.failProfileWrite = false
        vm.submit()
        runCurrent()
        assertEquals(1, repository.registrations)
        assertEquals(AuthSession.SIGNED_IN, vm.uiState.value.session)
    }

    @Test fun restoredSessionRequiresBiometricAndSignOutClearsUserState() = runTest(dispatcher) {
        repository.signIn("person@example.com", "password")
        repository.createProfile("Kasun", "+94711234567")
        preferences.setEnabled("uid-1", true)
        val vm = AuthViewModel(repository, preferences)
        runCurrent()
        assertEquals(AuthSession.LOCKED, vm.uiState.value.session)
        vm.biometricSucceeded()
        assertEquals(AuthSession.SIGNED_IN, vm.uiState.value.session)
        vm.lockIfEnabled()
        assertEquals(AuthSession.LOCKED, vm.uiState.value.session)
        vm.signOut()
        runCurrent()
        assertEquals(AuthSession.SIGNED_OUT, vm.uiState.value.session)
        assertNull(vm.uiState.value.profile)
    }

    @Test fun biometricOptInIsSavedOnlyAfterSuccessfulPrompt() = runTest(dispatcher) {
        val vm = registrationForm()
        vm.updateBiometric(true)
        vm.submit()
        runCurrent()
        assertEquals(AuthSession.LOCKED, vm.uiState.value.session)
        assertFalse(preferences.isEnabled("uid-1"))
        vm.biometricSucceeded()
        assertTrue(preferences.isEnabled("uid-1"))
    }

    @Test fun restoreNetworkFailureOffersRetryInsteadOfDashboard() = runTest(dispatcher) {
        repository.signIn("person@example.com", "password")
        repository.createProfile("Kasun", "+94711234567")
        repository.failProfileRead = true
        val vm = AuthViewModel(repository, preferences)
        runCurrent()
        assertEquals(AuthSession.ERROR, vm.uiState.value.session)
        repository.failProfileRead = false
        vm.retrySession()
        runCurrent()
        assertEquals(AuthSession.SIGNED_IN, vm.uiState.value.session)
    }

    @Test fun passwordResetValidatesThenSendsAndShowsGenericMessage() = runTest(dispatcher) {
        val vm = AuthViewModel(repository, preferences)
        runCurrent()
        vm.showResetDialog()
        vm.resetPassword()
        assertNotNull(vm.uiState.value.emailError)
        vm.updateResetEmail(" person@example.com ")
        vm.resetPassword()
        runCurrent()
        assertEquals("person@example.com", repository.resetRecipient)
        assertFalse(vm.uiState.value.resetDialog)
        assertNotNull(vm.uiState.value.message)
    }

    private suspend fun TestScope.registrationForm(): AuthViewModel {
        val vm = AuthViewModel(repository, preferences)
        runCurrent()
        vm.switchMode(true)
        vm.updateName(" Kasun Perera ")
        vm.updatePhone("+94 71 123 4567")
        vm.updateEmail("person@example.com")
        vm.updatePassword("StrongPass2026!")
        return vm
    }
}

private class FakeAuthRepository : AuthRepository {
    override val identities = MutableStateFlow<AuthIdentity?>(null)
    override val currentIdentity get() = identities.value
    var profile: UserProfile? = null
    var registrations = 0
    var failProfileWrite = false
    var failProfileRead = false
    var resetRecipient: String? = null
    override suspend fun signIn(email: String, password: String) { identities.value = AuthIdentity("uid-1", email) }
    override suspend fun register(email: String, password: String) { registrations++; signIn(email, password) }
    override suspend fun loadProfile(): UserProfile? {
        if (failProfileRead) error("No network")
        return profile
    }
    override suspend fun createProfile(name: String, phone: String): UserProfile {
        if (failProfileWrite) error("No network")
        val identity = requireNotNull(currentIdentity)
        return (profile ?: UserProfile(identity.uid, name, phone, identity.email)).also { profile = it }
    }
    override suspend fun sendPasswordReset(email: String) { resetRecipient = email }
    override fun signOut() { identities.value = null }
}

private class FakeBiometricPreferences : BiometricPreferences {
    private val enabled = mutableSetOf<String>()
    override fun isEnabled(uid: String) = uid in enabled
    override fun setEnabled(uid: String, enabled: Boolean) {
        if (enabled) this.enabled.add(uid) else this.enabled.remove(uid)
    }
}
