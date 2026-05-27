@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.payment

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebasePaymentRepository : PaymentRepository {
    private val functions = FirebaseFunctions.getInstance("africa-south1")

    override suspend fun createPaymentIntent(taskId: String): Result<PaymentIntentResult> =
        runCatching {
            suspendCancellableCoroutine { cont ->
                functions
                    .getHttpsCallable("createPaymentIntent")
                    .call(mapOf("taskId" to taskId))
                    .addOnSuccessListener { result ->
                        @Suppress("UNCHECKED_CAST")
                        val data = result.data as? Map<String, String>
                        val clientSecret = data?.get("clientSecret").orEmpty()
                        val paymentIntentId = data?.get("paymentIntentId").orEmpty()
                        cont.resume(PaymentIntentResult(clientSecret, paymentIntentId))
                    }.addOnFailureListener { cont.resumeWithException(it) }
            }
        }
}
