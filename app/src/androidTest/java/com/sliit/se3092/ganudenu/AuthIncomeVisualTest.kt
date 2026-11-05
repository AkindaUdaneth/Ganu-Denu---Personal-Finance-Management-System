package com.sliit.se3092.ganudenu

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.sliit.se3092.GanuDenu.data.auth.*
import com.sliit.se3092.GanuDenu.data.income.*
import com.sliit.se3092.GanuDenu.ui.auth.*
import com.sliit.se3092.GanuDenu.ui.income.*
import com.sliit.se3092.GanuDenu.ui.theme.GanuDenuTheme
import java.io.File
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Fixed 360 x 900 pixel canvas. Baselines were reviewed on the Pixel 6 API 37 emulator. */
class AuthIncomeVisualTest {
    @get:Rule val compose = createComposeRule()
    private val vm = AuthViewModel(VisualAuthRepository())

    @Test fun loginLayoutMatchesBaseline() = compare("login") {
        LoginScreen(AuthUiState(session = AuthSession.SIGNED_OUT), vm)
    }

    @Test fun signupLayoutMatchesBaseline() = compare("signup") {
        SignUpScreen(AuthUiState(session = AuthSession.SIGNED_OUT, signingUp = true), vm)
    }

    @Test fun incomeDashboardLayoutMatchesBaseline() = compare("income-dashboard") {
        IncomeDashboardContent(IncomeUiState(month = YearMonth.of(2026, 10), loading = false), {}, {}, {}, {})
    }

    @Test fun addIncomeLayoutMatchesBaseline() = compare("add-income") {
        AddIncomeContent(IncomeUiState(loading = false),
            IncomeDraft("visual", amount = "135,000.00", date = "2026-10-25"), {}, {}, {}, {})
    }

    private fun compare(name: String, content: @Composable () -> Unit) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                GanuDenuTheme {
                    Box(Modifier.requiredSize(360.dp, 900.dp).background(Color.Black).consumeWindowInsets(WindowInsets.safeDrawing).testTag("visual-screen")) { content() }
                }
            }
        }
        val actual = compose.onNodeWithTag("visual-screen").captureToImage().asAndroidBitmap()
        val args = InstrumentationRegistry.getArguments()
        if (args.getString("updateGoldens") == "true") {
            val directory = File(requireNotNull(args.getString("additionalTestOutputDir")), "goldens")
            directory.mkdirs()
            File(directory, "$name.png").outputStream().use { actual.compress(Bitmap.CompressFormat.PNG, 100, it) }
            return
        }
        val expected = InstrumentationRegistry.getInstrumentation().context.assets.open("goldens/$name.png")
            .use { requireNotNull(BitmapFactory.decodeStream(it)) }
        assertEquals("Screenshot width", expected.width, actual.width)
        assertEquals("Screenshot height", expected.height, actual.height)
        val width = actual.width
        val height = actual.height
        val actualPixels = IntArray(width * height)
        val expectedPixels = IntArray(width * height)
        actual.getPixels(actualPixels, 0, width, 0, 0, width, height)
        expected.getPixels(expectedPixels, 0, width, 0, 0, width, height)
        var different = 0
        for (index in actualPixels.indices) {
            val a = actualPixels[index]
            val e = expectedPixels[index]
            val redChanged = kotlin.math.abs(((a shr 16) and 255) - ((e shr 16) and 255)) > 16
            val greenChanged = kotlin.math.abs(((a shr 8) and 255) - ((e shr 8) and 255)) > 16
            val blueChanged = kotlin.math.abs((a and 255) - (e and 255)) > 16
            if (redChanged || greenChanged || blueChanged) different++
        }
        val fraction = different.toDouble() / (actual.width * actual.height)
        assertTrue("$name changed by " + (fraction * 100) + "%; review the image before updating its baseline.",
            fraction <= 0.005)
    }
}

private class VisualAuthRepository : AuthRepository {
    override val identities = MutableStateFlow<AuthIdentity?>(null)
    override val currentIdentity: AuthIdentity? = null
    override suspend fun signIn(email: String, password: String) = Unit
    override suspend fun register(email: String, password: String) = Unit
    override suspend fun signInWithGoogle(idToken: String) = Unit
    override suspend fun loadProfile(): UserProfile? = null
    override suspend fun createProfile(name: String, phone: String) = error("Visual test does not submit")
    override suspend fun sendPasswordReset(email: String) = Unit
    override fun signOut() = Unit
}
