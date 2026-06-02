package com.maku.eyeonsite.auth

import com.maku.eyeonsite.analytics.MockAnalyticsService
import com.maku.eyeonsite.domain.UserRole
import com.maku.eyeonsite.ui.auth.AuthState
import com.maku.eyeonsite.ui.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authProvider: FakeFirebaseAuthProvider
    private lateinit var analytics: MockAnalyticsService
    private lateinit var vm: AuthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authProvider = FakeFirebaseAuthProvider()
        analytics = MockAnalyticsService()
        vm = AuthViewModel(authProvider, analytics)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── session restore ─────────────────────────────────────────────────────────

    @Test
    fun `init restores Authenticated state when Firebase session and role exist`() =
        runTest {
            authProvider.storedUid = "returning-uid"
            authProvider.storedRole = UserRole.CLIENT
            val restoredVm = AuthViewModel(authProvider, analytics)

            val state = restoredVm.state.value
            assertIs<AuthState.Authenticated>(state)
            assertEquals("returning-uid", state.uid)
            assertEquals(UserRole.CLIENT, state.role)
        }

    @Test
    fun `init transitions to NeedsRole when session exists but role not saved`() =
        runTest {
            authProvider.storedUid = "returning-uid"
            authProvider.storedRole = null
            val restoredVm = AuthViewModel(authProvider, analytics)

            val state = restoredVm.state.value
            assertIs<AuthState.NeedsRole>(state)
            assertEquals("returning-uid", state.uid)
        }

    @Test
    fun `init transitions to Idle when no Firebase session exists`() =
        runTest {
            val freshVm = AuthViewModel(authProvider, analytics)

            assertIs<AuthState.Idle>(freshVm.state.value)
        }

    @Test
    fun `selectRole persists role so it survives restart`() =
        runTest {
            vm.selectRole("uid-123", UserRole.INSPECTOR)

            assertEquals(UserRole.INSPECTOR, authProvider.storedRole)
        }

    // ── sendOtp ─────────────────────────────────────────────────────────────────

    @Test
    fun `sendOtp transitions through SendingOtp to OtpSent on success`() =
        runTest {
            vm.sendOtp("+233201234567")

            assertIs<AuthState.OtpSent>(vm.state.value)
        }

    @Test
    fun `sendOtp logs analytics event on success`() =
        runTest {
            vm.sendOtp("+233201234567")

            assertTrue(analytics.wasEventLogged("auth_otp_requested"))
        }

    @Test
    fun `sendOtp transitions to Error on failure`() =
        runTest {
            authProvider.sendOtpResult = Result.failure(Exception("Invalid number"))

            vm.sendOtp("+000")

            val state = vm.state.value
            assertIs<AuthState.Error>(state)
            assertEquals("Invalid number", state.message)
        }

    // ── verifyOtp ───────────────────────────────────────────────────────────────

    @Test
    fun `verifyOtp transitions to NeedsRole on success`() =
        runTest {
            vm.sendOtp("+233201234567")
            vm.verifyOtp("123456")

            val state = vm.state.value
            assertIs<AuthState.NeedsRole>(state)
            assertEquals("fake-uid-001", state.uid)
        }

    @Test
    fun `verifyOtp logs analytics event on success`() =
        runTest {
            vm.sendOtp("+233201234567")
            vm.verifyOtp("123456")

            assertTrue(analytics.wasEventLogged("auth_otp_verified"))
        }

    @Test
    fun `verifyOtp transitions to Error on wrong code`() =
        runTest {
            authProvider.verifyOtpResult = Result.failure(Exception("Wrong code"))
            vm.sendOtp("+233201234567")
            vm.verifyOtp("000000")

            assertIs<AuthState.Error>(vm.state.value)
        }

    @Test
    fun `verifyOtp without prior sendOtp sets Error immediately`() =
        runTest {
            vm.verifyOtp("123456")

            assertIs<AuthState.Error>(vm.state.value)
        }

    // ── selectRole ──────────────────────────────────────────────────────────────

    @Test
    fun `selectRole transitions to Authenticated with correct role`() =
        runTest {
            vm.selectRole("uid-123", UserRole.CLIENT)

            val state = vm.state.value
            assertIs<AuthState.Authenticated>(state)
            assertEquals("uid-123", state.uid)
            assertEquals(UserRole.CLIENT, state.role)
        }

    @Test
    fun `selectRole logs analytics event with role name`() =
        runTest {
            vm.selectRole("uid-123", UserRole.INSPECTOR)

            analytics.assertEventLogged("auth_role_selected", mapOf("role" to "inspector"))
        }

    // ── signOut ─────────────────────────────────────────────────────────────────

    @Test
    fun `signOut resets state to Idle`() =
        runTest {
            vm.selectRole("uid-123", UserRole.CLIENT)
            vm.signOut()

            assertIs<AuthState.Idle>(vm.state.value)
        }

    @Test
    fun `signOut calls provider and logs analytics`() =
        runTest {
            vm.signOut()

            assertTrue(authProvider.signOutCalled)
            assertTrue(analytics.wasEventLogged("auth_sign_out"))
        }

    // ── dismissError ────────────────────────────────────────────────────────────

    @Test
    fun `dismissError resets Error state to Idle`() =
        runTest {
            authProvider.sendOtpResult = Result.failure(Exception("oops"))
            vm.sendOtp("+000")
            assertIs<AuthState.Error>(vm.state.value)

            vm.dismissError()

            assertIs<AuthState.Idle>(vm.state.value)
        }
}
