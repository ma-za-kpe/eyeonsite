package com.maku.eyeonsite.payment

import com.maku.eyeonsite.domain.EscrowLedgerEntry
import com.maku.eyeonsite.domain.MoneyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class PaymentIntentResult(
    val clientSecret: String,
    val paymentIntentId: String,
    val provider: MoneyProvider = MoneyProvider.STRIPE,
)

interface PaymentRepository {
    suspend fun createPaymentIntent(taskId: String): Result<PaymentIntentResult>

    fun getLedger(taskId: String): Flow<List<EscrowLedgerEntry>>
}

class NoOpPaymentRepository : PaymentRepository {
    override suspend fun createPaymentIntent(taskId: String): Result<PaymentIntentResult> =
        Result.success(
            PaymentIntentResult(
                clientSecret = "mock_secret_$taskId",
                paymentIntentId = "pi_mock",
                provider = MoneyProvider.STRIPE,
            ),
        )

    override fun getLedger(taskId: String): Flow<List<EscrowLedgerEntry>> = flowOf(emptyList())
}
