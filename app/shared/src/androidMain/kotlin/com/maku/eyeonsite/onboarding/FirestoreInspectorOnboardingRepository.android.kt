@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.onboarding

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.maku.eyeonsite.domain.InspectorOnboardingStatus
import com.maku.eyeonsite.domain.KycCheck
import com.maku.eyeonsite.domain.KycProvider
import com.maku.eyeonsite.domain.KycStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("InjectDispatcher", "IgnoredReturnValue")
class FirestoreInspectorOnboardingRepository : InspectorOnboardingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override suspend fun uploadIdDocuments(
        uid: String,
        idBytes: ByteArray,
        selfieBytes: ByteArray,
    ): Result<Pair<String, String>> =
        runCatching {
            val idDocPath = uploadBytes("id_docs/$uid/govt_id.jpg", idBytes)
            val selfiePath = uploadBytes("id_docs/$uid/selfie.jpg", selfieBytes)
            idDocPath to selfiePath
        }

    private suspend fun uploadBytes(
        path: String,
        bytes: ByteArray,
    ): String =
        suspendCancellableCoroutine { cont ->
            val ref = storage.reference.child(path)
            val metadata =
                StorageMetadata
                    .Builder()
                    .setContentType("image/jpeg")
                    .build()
            ref
                .putBytes(bytes, metadata)
                .addOnSuccessListener { cont.resume(ref.path) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    override suspend fun submitProfile(
        uid: String,
        fullName: String,
        city: String,
        serviceRadiusKm: Int,
        testScore: Int,
        idDocPath: String,
        selfiePath: String,
    ): Result<Unit> =
        runCatching {
            val data: Map<String, Any> =
                mapOf(
                    "uid" to uid,
                    "fullName" to fullName,
                    "city" to city,
                    "serviceRadiusKm" to serviceRadiusKm,
                    "testScore" to testScore,
                    "idDocPath" to idDocPath,
                    "selfiePath" to selfiePath,
                    "onboardingStatus" to InspectorOnboardingStatus.PENDING_REVIEW.name,
                    "submittedAtMs" to FieldValue.serverTimestamp(),
                    "available" to false,
                    "rating" to 0.0,
                    "tierLevel" to 1,
                )
            suspendCancellableCoroutine { cont ->
                db
                    .collection("inspectors")
                    .document(uid)
                    .set(data)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
        }

    override fun observeKycStatus(uid: String): Flow<KycCheck> =
        callbackFlow {
            val registration =
                db
                    .collection("inspectors")
                    .document(uid)
                    .addSnapshotListener { snap, _ ->
                        if (snap == null) return@addSnapshotListener
                        val kyc =
                            KycCheck(
                                provider =
                                    runCatching {
                                        KycProvider.valueOf(snap.getString("kycProvider") ?: "")
                                    }.getOrDefault(KycProvider.NONE),
                                status =
                                    runCatching {
                                        KycStatus.valueOf(snap.getString("kycStatus") ?: "")
                                    }.getOrDefault(KycStatus.NOT_STARTED),
                                providerJobId = snap.getString("kycJobId").orEmpty(),
                                submittedAtMs = snap.getLong("kycSubmittedAtMs") ?: 0L,
                                decidedAtMs = snap.getLong("kycDecidedAtMs") ?: 0L,
                                resultCode = snap.getString("kycResultCode").orEmpty(),
                                failureReason = snap.getString("kycFailureReason").orEmpty(),
                            )
                        trySend(kyc)
                    }
            awaitClose { registration.remove() }
        }
}
