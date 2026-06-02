package com.maku.eyeonsite.onboarding

import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.domain.KycCheck
import com.maku.eyeonsite.domain.KycStatus
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

@OptIn(ExperimentalCoroutinesApi::class)
class KycStatusTransitionTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeInspectorOnboardingRepository
    private lateinit var vm: InspectorOnboardingViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeInspectorOnboardingRepository()
        vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadKycStatus with APPROVED transitions to KycApproved`() =
        runTest {
            repo = FakeInspectorOnboardingRepository(kycCheck = KycCheck(status = KycStatus.APPROVED))
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)

            vm.loadKycStatus("uid-1")

            assertIs<InspectorOnboardingUiState.KycApproved>(vm.state.value)
        }

    @Test
    fun `loadKycStatus with REJECTED transitions to KycRejected with reason`() =
        runTest {
            repo =
                FakeInspectorOnboardingRepository(
                    kycCheck = KycCheck(status = KycStatus.REJECTED, failureReason = "ID expired"),
                )
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)

            vm.loadKycStatus("uid-1")

            val state = assertIs<InspectorOnboardingUiState.KycRejected>(vm.state.value)
            assertEquals("ID expired", state.reason)
        }

    @Test
    fun `loadKycStatus with RETRY_REQUIRED transitions to KycRetryRequired with reason`() =
        runTest {
            repo =
                FakeInspectorOnboardingRepository(
                    kycCheck = KycCheck(status = KycStatus.RETRY_REQUIRED, failureReason = "Selfie too dark"),
                )
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)

            vm.loadKycStatus("uid-1")

            val state = assertIs<InspectorOnboardingUiState.KycRetryRequired>(vm.state.value)
            assertEquals("Selfie too dark", state.reason)
        }

    @Test
    fun `loadKycStatus with EXPIRED transitions to KycExpired`() =
        runTest {
            repo = FakeInspectorOnboardingRepository(kycCheck = KycCheck(status = KycStatus.EXPIRED))
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)

            vm.loadKycStatus("uid-1")

            assertIs<InspectorOnboardingUiState.KycExpired>(vm.state.value)
        }

    @Test
    fun `loadKycStatus with PENDING_REVIEW stays in KycPending`() =
        runTest {
            repo = FakeInspectorOnboardingRepository(kycCheck = KycCheck(status = KycStatus.PENDING_REVIEW))
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)

            vm.loadKycStatus("uid-1")

            assertIs<InspectorOnboardingUiState.KycPending>(vm.state.value)
        }

    @Test
    fun `retryKyc from KycRetryRequired returns to IdCapture`() =
        runTest {
            repo =
                FakeInspectorOnboardingRepository(
                    kycCheck = KycCheck(status = KycStatus.RETRY_REQUIRED),
                )
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)
            vm.loadKycStatus("uid-1")
            assertIs<InspectorOnboardingUiState.KycRetryRequired>(vm.state.value)

            vm.retryKyc()

            assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
        }

    @Test
    fun `retryKyc from KycExpired returns to IdCapture`() =
        runTest {
            repo = FakeInspectorOnboardingRepository(kycCheck = KycCheck(status = KycStatus.EXPIRED))
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)
            vm.loadKycStatus("uid-1")
            assertIs<InspectorOnboardingUiState.KycExpired>(vm.state.value)

            vm.retryKyc()

            assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
        }

    @Test
    fun `retryKyc from KycApproved is a no-op`() =
        runTest {
            repo = FakeInspectorOnboardingRepository(kycCheck = KycCheck(status = KycStatus.APPROVED))
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)
            vm.loadKycStatus("uid-1")
            assertIs<InspectorOnboardingUiState.KycApproved>(vm.state.value)

            vm.retryKyc()

            assertIs<InspectorOnboardingUiState.KycApproved>(vm.state.value)
        }
}
