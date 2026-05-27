@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.onboarding

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.maku.eyeonsite.domain.InspectorOnboardingStatus
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
            val idDocUrl = uploadBytes("id_docs/$uid/govt_id.jpg", idBytes)
            val selfieUrl = uploadBytes("id_docs/$uid/selfie.jpg", selfieBytes)
            idDocUrl to selfieUrl
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
                .continueWithTask { ref.downloadUrl }
                .addOnSuccessListener { uri -> cont.resume(uri.toString()) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    override suspend fun submitProfile(
        uid: String,
        fullName: String,
        city: String,
        serviceRadiusKm: Int,
        testScore: Int,
        idDocUrl: String,
        selfieUrl: String,
    ): Result<Unit> =
        runCatching {
            val data: Map<String, Any> =
                mapOf(
                    "uid" to uid,
                    "fullName" to fullName,
                    "city" to city,
                    "serviceRadiusKm" to serviceRadiusKm,
                    "testScore" to testScore,
                    "idDocUrl" to idDocUrl,
                    "selfieUrl" to selfieUrl,
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
}
