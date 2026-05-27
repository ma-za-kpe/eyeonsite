package com.maku.eyeonsite.payment

import com.maku.eyeonsite.analytics.NoOpAnalyticsService
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
class FundTaskViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeFundTaskRepository
    private lateinit var vm: FundTaskViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeFundTaskRepository()
        vm = FundTaskViewModel(repo, NoOpAnalyticsService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertIs<FundTaskUiState.Idle>(vm.state.value)
    }

    @Test
    fun `startPayment transitions to ReadyToPay on success`() =
        runTest {
            vm.startPayment("task-1")
            val s = assertIs<FundTaskUiState.ReadyToPay>(vm.state.value)
            assertEquals("secret_task-1", s.clientSecret)
            assertEquals("pi_task-1", s.paymentIntentId)
        }

    @Test
    fun `startPayment calls repository with taskId`() =
        runTest {
            vm.startPayment("task-abc")
            assertEquals(listOf("task-abc"), repo.calls)
        }

    @Test
    fun `startPayment transitions to Error on failure`() =
        runTest {
            repo = FakeFundTaskRepository(shouldFail = true)
            vm = FundTaskViewModel(repo, NoOpAnalyticsService)
            vm.startPayment("task-fail")
            val s = assertIs<FundTaskUiState.Error>(vm.state.value)
            assertEquals("CF error", s.message)
        }

    @Test
    fun `startPayment is no-op when already CreatingIntent`() =
        runTest {
            vm.startPayment("task-1")
            val first = vm.state.value
            vm.startPayment("task-2")
            assertEquals(first, vm.state.value)
            assertEquals(1, repo.calls.size)
        }

    @Test
    fun `confirmPaymentSucceeded transitions to Funded`() =
        runTest {
            vm.startPayment("task-1")
            vm.confirmPaymentSucceeded("task-1")
            assertIs<FundTaskUiState.Funded>(vm.state.value)
        }

    @Test
    fun `confirmPaymentSucceeded from Idle is no-op`() {
        vm.confirmPaymentSucceeded("task-1")
        assertIs<FundTaskUiState.Idle>(vm.state.value)
    }

    @Test
    fun `confirmPaymentFailed transitions to Error`() =
        runTest {
            vm.startPayment("task-1")
            vm.confirmPaymentFailed("Card declined")
            val s = assertIs<FundTaskUiState.Error>(vm.state.value)
            assertEquals("Card declined", s.message)
        }

    @Test
    fun `confirmPaymentFailed from Idle is no-op`() {
        vm.confirmPaymentFailed("err")
        assertIs<FundTaskUiState.Idle>(vm.state.value)
    }

    @Test
    fun `retry from Error returns to Idle`() =
        runTest {
            repo = FakeFundTaskRepository(shouldFail = true)
            vm = FundTaskViewModel(repo, NoOpAnalyticsService)
            vm.startPayment("task-1")
            assertIs<FundTaskUiState.Error>(vm.state.value)
            vm.retry()
            assertIs<FundTaskUiState.Idle>(vm.state.value)
        }

    @Test
    fun `startPayment allowed again after retry`() =
        runTest {
            repo = FakeFundTaskRepository(shouldFail = true)
            vm = FundTaskViewModel(repo, NoOpAnalyticsService)
            vm.startPayment("task-fail")
            vm.retry()

            repo = FakeFundTaskRepository()
            vm = FundTaskViewModel(repo, NoOpAnalyticsService)
            vm.startPayment("task-ok")
            assertIs<FundTaskUiState.ReadyToPay>(vm.state.value)
        }
}
