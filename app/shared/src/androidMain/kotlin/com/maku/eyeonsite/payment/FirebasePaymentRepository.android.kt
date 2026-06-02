@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.payment

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.maku.eyeonsite.domain.EscrowLedgerEntry
import com.maku.eyeonsite.domain.EscrowLedgerEntryStatus
import com.maku.eyeonsite.domain.EscrowLedgerEntryType
import com.maku.eyeonsite.domain.MoneyProvider
import com.maku.eyeonsite.domain.MoneyProvider.FLUTTERWAVE
import com.maku.eyeonsite.domain.MoneyProvider.STRIPE
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebasePaymentRepository : PaymentRepository {
    private val functions = FirebaseFunctions.getInstance("africa-south1")
    private val firestore = FirebaseFirestore.getInstance()

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
                        val provider =
                            when (data?.get("provider")?.uppercase()) {
                                "FLUTTERWAVE" -> FLUTTERWAVE
                                else -> STRIPE
                            }
                        cont.resume(PaymentIntentResult(clientSecret, paymentIntentId, provider))
                    }.addOnFailureListener { cont.resumeWithException(it) }
            }
        }

    override fun getLedger(taskId: String): Flow<List<EscrowLedgerEntry>> =
        callbackFlow {
            val registration =
                firestore
                    .collection("escrow_ledger")
                    .whereEqualTo("taskId", taskId)
                    .orderBy("createdAtMs")
                    .addSnapshotListener { snapshot, _ ->
                        val entries =
                            snapshot?.documents?.mapNotNull { doc ->
                                runCatching {
                                    EscrowLedgerEntry(
                                        id = doc.id,
                                        taskId = doc.getString("taskId") ?: return@mapNotNull null,
                                        type =
                                            runCatching {
                                                EscrowLedgerEntryType.valueOf(doc.getString("type") ?: "")
                                            }.getOrElse { EscrowLedgerEntryType.ESCROW_HOLD },
                                        status =
                                            runCatching {
                                                EscrowLedgerEntryStatus.valueOf(doc.getString("status") ?: "")
                                            }.getOrElse { EscrowLedgerEntryStatus.PENDING },
                                        provider =
                                            runCatching {
                                                MoneyProvider.valueOf(doc.getString("provider") ?: "")
                                            }.getOrElse { MoneyProvider.NONE },
                                        amountUsdCents = doc.getLong("amountUsdCents") ?: 0L,
                                        createdAtMs = doc.getLong("createdAtMs") ?: 0L,
                                        providerReference = doc.getString("providerReference").orEmpty(),
                                        idempotencyKey = doc.getString("idempotencyKey").orEmpty(),
                                    )
                                }.getOrNull()
                            } ?: emptyList()
                        trySend(entries)
                    }
            awaitClose { registration.remove() }
        }
}
