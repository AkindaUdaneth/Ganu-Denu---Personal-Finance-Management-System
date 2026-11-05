package com.sliit.se3092.ganudenu

import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.*
import com.sliit.se3092.GanuDenu.data.auth.FirebaseAuthRepository
import com.sliit.se3092.GanuDenu.data.income.*
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/** Isolated demo project with the checked-in rules. Opt in with scripts/test-firebase.ps1. */
class FirebaseRepositoryIntegrationTest {
    private lateinit var app: FirebaseApp
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profiles: FirebaseAuthRepository
    private lateinit var incomes: FirestoreIncomeRepository
    private val email = "qa-" + UUID.randomUUID() + "@example.com"
    private val password = "TestPassword2026!"
    private val project = "demo-ganudenu-qa"
    private val host = "10.0.2.2"

    @Before fun connectToLocalEmulators() {
        assumeTrue("Run scripts/test-firebase.ps1 to enable backend integration tests.",
            InstrumentationRegistry.getArguments().getString("firebaseEmulators") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        app = requireNotNull(FirebaseApp.initializeApp(context,
            FirebaseOptions.Builder().setProjectId(project)
                .setApplicationId("1:123456789:android:0123456789abcdef")
                .setApiKey("fake-api-key").build(), "qa-" + UUID.randomUUID()))
        auth = FirebaseAuth.getInstance(app)
        auth.useEmulator(host, 9098)
        db = FirebaseFirestore.getInstance(app)
        db.useEmulator(host, 8085)
        db.firestoreSettings = FirebaseFirestoreSettings.Builder(db.firestoreSettings)
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build()
        profiles = FirebaseAuthRepository(auth, db)
        incomes = FirestoreIncomeRepository(auth, db)
    }

    @After fun closeClients() = runBlocking {
        if (::db.isInitialized) db.terminate().await()
        if (::app.isInitialized) app.delete()
    }

    @Test fun signupAndProfileEditPersistValidTimestampsAndPreserveCreatedAt() = runBlocking {
        withTimeout(30_000) {
            profiles.register(email, password)
            val profile = profiles.createProfile("QA User", "+94711234567")
            val document = db.collection("users").document(profile.uid)
            val original = document.get(Source.SERVER).await()
            assertEquals(email, original.getString("email"))
            assertNotNull(original.getTimestamp("createdAt"))
            assertNotNull(original.getTimestamp("updatedAt"))
            profiles.createProfile("Edited User", "+94777654321")
            val updated = document.get(Source.SERVER).await()
            assertEquals(original.getTimestamp("createdAt"), updated.getTimestamp("createdAt"))
            assertEquals("Edited User", profiles.loadProfile()?.name)
            assertEquals("+94777654321", profiles.loadProfile()?.phone)
        }
    }

    @Test fun duplicateEmailWrongPasswordAndDisabledAccountAreRejectedByAuth() = runBlocking {
        withTimeout(30_000) {
            profiles.register(email, password)
            val uid = requireNotNull(auth.currentUser).uid
            expectAuthFailure { profiles.register(email, password) }
            profiles.signOut()
            expectAuthFailure { profiles.signIn(email, "IncorrectPassword!") }
            assertNull(auth.currentUser)
            request("/identitytoolkit.googleapis.com/v1/projects/$project/accounts:update",
                JSONObject().put("localId", uid).put("disableUser", true), admin = true)
            val disabled = expectAuthFailure { profiles.signIn(email, password) }
            assertEquals("ERROR_USER_DISABLED", disabled.errorCode)
        }
    }

    @Test fun passwordResetActionCodeChangesThePasswordAndIsSingleUse() = runBlocking {
        withTimeout(30_000) {
            profiles.register(email, password)
            profiles.signOut()
            profiles.sendPasswordReset(email)
            val codes = request("/emulator/v1/projects/$project/oobCodes").getJSONArray("oobCodes")
            val reset = (0 until codes.length()).map { codes.getJSONObject(it) }.last { it.getString("email") == email }
            assertTrue(reset.getString("oobLink").contains("mode=resetPassword"))
            val code = reset.getString("oobCode")
            assertEquals(email, auth.verifyPasswordResetCode(code).await())
            auth.confirmPasswordReset(code, "ChangedPassword2026!").await()
            expectAuthFailure { auth.verifyPasswordResetCode(code).await() }
            expectAuthFailure { profiles.signIn(email, password) }
            profiles.signIn(email, "ChangedPassword2026!")
            assertEquals(email, auth.currentUser?.email)
        }
    }

    @Test fun incomeQueriesRespectYearBoundaryAndRetriesOverwriteTheSameDocument() = runBlocking {
        withTimeout(30_000) {
            profiles.register(email, password)
            val december = entry("december", LocalDate.of(2025, 12, 31), "123.45")
            val january = entry("january", LocalDate.of(2026, 1, 1), "678.90")
            incomes.save(december)
            incomes.save(december)
            incomes.save(january)
            val decemberSnapshot = incomes.observeMonth(YearMonth.of(2025, 12)).first { !it.fromCache && !it.pendingWrites }
            val januarySnapshot = incomes.observeMonth(YearMonth.of(2026, 1)).first { !it.fromCache && !it.pendingWrites }
            assertEquals(listOf(december), decemberSnapshot.entries)
            assertEquals(listOf(january), januarySnapshot.entries)
            val documents = incomeCollection().get(Source.SERVER).await()
            assertEquals(2, documents.size())
            assertEquals(12345L, documents.documents.first { it.id == "december" }.getLong("amountLkrMinor"))
        }
    }

    @Test fun offlineCachePendingWriteAndReconnectSyncKeepOneRecord() = runBlocking {
        withTimeout(30_000) {
            profiles.register(email, password)
            val date = LocalDate.now().minusDays(1)
            val month = YearMonth.from(date)
            val snapshots = Channel<IncomeSnapshot>(Channel.UNLIMITED)
            val observation = launch {
                incomes.observeMonth(month).collect { snapshots.send(it) }
            }
            suspend fun nextSnapshot(predicate: (IncomeSnapshot) -> Boolean): IncomeSnapshot = withTimeout(10_000) {
                var snapshot = snapshots.receive()
                while (!predicate(snapshot)) snapshot = snapshots.receive()
                snapshot
            }
            try {
                incomes.save(entry("cached", date, "100"))
                nextSnapshot { !it.fromCache && !it.pendingWrites && it.entries.size == 1 }
                db.disableNetwork().await()
                val cached = nextSnapshot { it.fromCache && it.entries.size == 1 }
                assertEquals("cached", cached.entries.single().id)
                val write = async { incomes.save(entry("pending", date, "200")) }
                val pending = nextSnapshot { it.pendingWrites && it.entries.size == 2 }
                assertTrue(pending.fromCache)
                assertFalse(write.isCompleted)
                db.enableNetwork().await()
                write.await()
                val synced = nextSnapshot { !it.fromCache && !it.pendingWrites && it.entries.size == 2 }
                assertEquals(setOf("cached", "pending"), synced.entries.map { it.id }.toSet())
                assertEquals(2, incomeCollection().get(Source.SERVER).await().size())
            } finally {
                observation.cancel()
                snapshots.close()
            }
        }
    }

    @Test fun rulesRejectMalformedIncomeAndCrossAccountReadsAndWrites() = runBlocking {
        withTimeout(30_000) {
            profiles.register(email, password)
            val owner = requireNotNull(auth.currentUser).uid
            profiles.createProfile("Owner", "+94711234567")
            incomes.save(entry("private", LocalDate.now(), "100"))
            val malformed = entry("invalid", LocalDate.now(), "100").copy(amountLkrMinor = 0)
            expectDenied { incomes.save(malformed) }
            profiles.signOut()
            profiles.register("other-$email", password)
            val privateDocument = db.collection("users").document(owner).collection("incomes").document("private")
            expectDenied { privateDocument.get(Source.SERVER).await() }
            expectDenied { privateDocument.update("note", "tampered").await() }
            expectDenied { db.collection("users").document(owner).get(Source.SERVER).await() }
            expectDenied { db.collection("users").document(owner).update("name", "tampered").await() }
        }
    }

    private fun entry(id: String, date: LocalDate, amount: String) =
        IncomeValidation.toEntry(IncomeDraft(id, amount = amount, date = date.toString()))

    private fun incomeCollection() = db.collection("users").document(requireNotNull(auth.currentUser).uid).collection("incomes")

    private suspend fun expectAuthFailure(action: suspend () -> Unit): FirebaseAuthException {
        try { action() } catch (exception: FirebaseAuthException) { return exception }
        throw AssertionError("Expected Firebase Auth to reject the operation.")
    }

    private suspend fun expectDenied(action: suspend () -> Unit) {
        try { action() } catch (exception: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception.code)
            return
        }
        fail("Expected Firestore rules to deny the operation.")
    }

    private fun request(path: String, body: JSONObject? = null, admin: Boolean = false): JSONObject {
        val connection = URL("http://$host:9098$path").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            if (admin) connection.setRequestProperty("Authorization", "Bearer owner")
            if (body != null) {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toString().toByteArray()) }
            }
            val code = connection.responseCode
            val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
                .bufferedReader().use { it.readText() }
            check(code in 200..299) { "Emulator API failed ($code): $response" }
            return JSONObject(response)
        } finally { connection.disconnect() }
    }
}
