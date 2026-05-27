package com.maku.eyeonsite.payment

data class PaymentIntentResult(
    val clientSecret: String,
    val paymentIntentId: String,
)

interface PaymentRepository {
    suspend fun createPaymentIntent(taskId: String): Result<PaymentIntentResult>
}

class NoOpPaymentRepository : PaymentRepository {
    override suspend fun createPaymentIntent(taskId: String): Result<PaymentIntentResult> =
        Result.success(PaymentIntentResult(clientSecret = "mock_secret_$taskId", paymentIntentId = "pi_mock"))
}
