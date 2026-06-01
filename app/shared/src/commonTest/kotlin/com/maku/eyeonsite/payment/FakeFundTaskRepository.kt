package com.maku.eyeonsite.payment

import com.maku.eyeonsite.domain.EscrowLedgerEntry
import com.maku.eyeonsite.domain.MoneyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFundTaskRepository(
    private val shouldFail: Boolean = false,
    private val ledgerEntries: List<EscrowLedgerEntry> = emptyList(),
    private val provider: MoneyProvider = MoneyProvider.STRIPE,
) : PaymentRepository {
    val calls = mutableListOf<String>()

    override suspend fun createPaymentIntent(taskId: String): Result<PaymentIntentResult> {
        calls += taskId
        return if (shouldFail) {
            Result.failure(RuntimeException("CF error"))
        } else {
            Result.success(
                PaymentIntentResult(
                    clientSecret = "secret_$taskId",
                    paymentIntentId = "pi_$taskId",
                    provider = provider,
                ),
            )
        }
    }

    override fun getLedger(taskId: String): Flow<List<EscrowLedgerEntry>> =
        flowOf(
            ledgerEntries
                .filter { it.taskId == taskId }
                .sortedBy { it.createdAtMs },
        )
}
