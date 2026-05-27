package com.maku.eyeonsite.payment

class FakeFundTaskRepository(
    private val shouldFail: Boolean = false,
) : PaymentRepository {
    val calls = mutableListOf<String>()

    override suspend fun createPaymentIntent(taskId: String): Result<PaymentIntentResult> {
        calls += taskId
        return if (shouldFail) {
            Result.failure(RuntimeException("CF error"))
        } else {
            Result.success(PaymentIntentResult(clientSecret = "secret_$taskId", paymentIntentId = "pi_$taskId"))
        }
    }
}
