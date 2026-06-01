package com.maku.eyeonsite.payment

import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.domain.MoneyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class MoneyProviderRoutingTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `STRIPE payment intent surfaces STRIPE provider in ReadyToPay`() =
        runTest {
            val repo = FakeFundTaskRepository(provider = MoneyProvider.STRIPE)
            val vm = FundTaskViewModel(repo, NoOpAnalyticsService)

            vm.startPayment("task-1")

            val state = assertIs<FundTaskUiState.ReadyToPay>(vm.state.value)
            assertEquals(MoneyProvider.STRIPE, state.provider)
        }

    @Test
    fun `FLUTTERWAVE payment intent surfaces FLUTTERWAVE provider in ReadyToPay`() =
        runTest {
            val repo = FakeFundTaskRepository(provider = MoneyProvider.FLUTTERWAVE)
            val vm = FundTaskViewModel(repo, NoOpAnalyticsService)

            vm.startPayment("task-1")

            val state = assertIs<FundTaskUiState.ReadyToPay>(vm.state.value)
            assertEquals(MoneyProvider.FLUTTERWAVE, state.provider)
        }

    @Test
    fun `MOCK payment intent surfaces MOCK provider in ReadyToPay`() =
        runTest {
            val repo = FakeFundTaskRepository(provider = MoneyProvider.MOCK)
            val vm = FundTaskViewModel(repo, NoOpAnalyticsService)

            vm.startPayment("task-1")

            val state = assertIs<FundTaskUiState.ReadyToPay>(vm.state.value)
            assertEquals(MoneyProvider.MOCK, state.provider)
        }

    @Test
    fun `NoOpPaymentRepository returns STRIPE by default`() =
        runTest {
            val result = NoOpPaymentRepository().createPaymentIntent("task-x")
            assertEquals(MoneyProvider.STRIPE, result.getOrThrow().provider)
        }

    @Test
    fun `MockPaymentRepository always succeeds with MOCK provider`() =
        runTest {
            val result = MockPaymentRepository().createPaymentIntent("task-x")
            assertTrue(result.isSuccess)
            assertEquals(MoneyProvider.MOCK, result.getOrThrow().provider)
        }

    @Test
    fun `MockPaymentRepository paymentIntentId contains taskId`() =
        runTest {
            val result = MockPaymentRepository().createPaymentIntent("task-abc")
            assertTrue(result.getOrThrow().paymentIntentId.contains("task-abc"))
        }

    @Test
    fun `MockPaymentRepository getLedger returns empty list`() =
        runTest {
            val entries = MockPaymentRepository().getLedger("task-x").first()
            assertTrue(entries.isEmpty())
        }
}
