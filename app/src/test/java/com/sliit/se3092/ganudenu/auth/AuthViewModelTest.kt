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

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAuthRepository()
    }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun invalidFormDoesNotCallFirebase() = runTest(dispatcher) {
        val vm = AuthViewModel(repository)
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

    @Test fun restoredSessionSignsInAndSignOutClearsUserState() = runTest(dispatcher) {
        repository.signIn("person@example.com", "password")
        repository.createProfile("Kasun", "+94711234567")
        val vm = AuthViewModel(repository)
        runCurrent()
        assertEquals(AuthSession.SIGNED_IN, vm.uiState.value.session)
        vm.signOut()
        runCurrent()
        assertEquals(AuthSession.SIGNED_OUT, vm.uiState.value.session)
        assertNull(vm.uiState.value.profile)
    }

    @Test fun restoreNetworkFailureOffersRetryInsteadOfDashboard() = runTest(dispatcher) {
        repository.signIn("person@example.com", "password")
        repository.createProfile("Kasun", "+94711234567")
        repository.failProfileRead = true
        val vm = AuthViewModel(repository)
        runCurrent()
        assertEquals(AuthSession.ERROR, vm.uiState.value.session)
        repository.failProfileRead = false
        vm.retrySession()
        runCurrent()
        assertEquals(AuthSession.SIGNED_IN, vm.uiState.value.session)
    }

    @Test fun passwordResetValidatesThenSendsAndShowsGenericMessage() = runTest(dispatcher) {
        val vm = AuthViewModel(repository)
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

    @Test fun loginProfileReadFailureOffersRetryWithoutRequestingNewDetails() = runTest(dispatcher) {
        repository.profile = UserProfile("uid-1", "Existing User", "+94711234567", "person@example.com")
        repository.failProfileRead = true
        val vm = AuthViewModel(repository)
        runCurrent()
        vm.updateEmail("person@example.com")
        vm.updatePassword("password")
        vm.submit()
        runCurrent()

        assertEquals(AuthSession.ERROR, vm.uiState.value.session)
        assertNotNull(vm.uiState.value.error)
        assertEquals("", vm.uiState.value.password)
        assertEquals(0, repository.profileWrites)

        repository.failProfileRead = false
        vm.retrySession()
        runCurrent()
        assertEquals(AuthSession.SIGNED_IN, vm.uiState.value.session)
        assertEquals("Existing User", vm.uiState.value.profile?.name)
        assertEquals(1, repository.signIns)
        assertEquals(0, repository.profileWrites)
    }

    @Test fun missingProfileAfterLoginStillOffersProfileCompletion() = runTest(dispatcher) {
        val vm = AuthViewModel(repository)
        runCurrent()
        vm.updateEmail("person@example.com")
        vm.updatePassword("password")
        vm.submit()
        runCurrent()
        assertEquals(AuthSession.NEEDS_PROFILE, vm.uiState.value.session)
        assertNull(vm.uiState.value.error)
    }

    @Test fun googleProfileReadFailureOffersRetryForBothLoginAndSignup() = runTest(dispatcher) {
        for (isSignUp in listOf(false, true)) {
            repository.profile = UserProfile("uid-1", "Google User", "+94711234567", "person@example.com")
            repository.failProfileRead = true
            val vm = AuthViewModel(repository)
            runCurrent()
            vm.signInWithGoogle("test-token", isSignUp)
            runCurrent()
            assertEquals(AuthSession.ERROR, vm.uiState.value.session)
            assertEquals(0, repository.profileWrites)

            repository.failProfileRead = false
            vm.retrySession()
            runCurrent()
            assertEquals(AuthSession.SIGNED_IN, vm.uiState.value.session)
            vm.signOut()
            runCurrent()
        }
    }

    @Test fun profileReadFailureAllowsSigningOut() = runTest(dispatcher) {
        repository.failProfileRead = true
        val vm = AuthViewModel(repository)
        runCurrent()
        vm.signInWithGoogle("test-token")
        runCurrent()
        vm.signOut()
        runCurrent()
        assertEquals(AuthSession.SIGNED_OUT, vm.uiState.value.session)
        assertNull(repository.currentIdentity)
    }

    private suspend fun TestScope.registrationForm(): AuthViewModel {
        val vm = AuthViewModel(repository)
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
    var signIns = 0
    var profileWrites = 0
    var failProfileWrite = false
    var failProfileRead = false
    var resetRecipient: String? = null
    override suspend fun signIn(email: String, password: String) { signIns++; identities.value = AuthIdentity("uid-1", email) }
    override suspend fun signInWithGoogle(idToken: String) { identities.value = AuthIdentity("uid-1", "person@example.com") }
    override suspend fun register(email: String, password: String) { registrations++; signIn(email, password) }
    override suspend fun loadProfile(): UserProfile? {
        if (failProfileRead) error("No network")
        return profile
    }
    override suspend fun createProfile(name: String, phone: String): UserProfile {
        profileWrites++
        if (failProfileWrite) error("No network")
        val identity = requireNotNull(currentIdentity)
        return (profile ?: UserProfile(identity.uid, name, phone, identity.email)).also { profile = it }
    }
    override suspend fun sendPasswordReset(email: String) { resetRecipient = email }
    override fun signOut() { identities.value = null }
}
