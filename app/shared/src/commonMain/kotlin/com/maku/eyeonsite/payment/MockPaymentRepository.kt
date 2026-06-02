package com.maku.eyeonsite.payment

import com.maku.eyeonsite.domain.EscrowLedgerEntry
import com.maku.eyeonsite.domain.MoneyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockPaymentRepository : PaymentRepository {
    override suspend fun createPaymentIntent(taskId: String): Result<PaymentIntentResult> =
        Result.success(
            PaymentIntentResult(
                clientSecret = "mock_secret_$taskId",
                paymentIntentId = "pi_mock_$taskId",
                provider = MoneyProvider.MOCK,
            ),
        )

    override fun getLedger(taskId: String): Flow<List<EscrowLedgerEntry>> = flowOf(emptyList())
}
