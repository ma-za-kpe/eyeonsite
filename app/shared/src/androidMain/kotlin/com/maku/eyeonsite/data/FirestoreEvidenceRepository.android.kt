@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.util.generateUlid
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreEvidenceRepository : EvidenceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun submitEvidence(
        taskId: String,
        photos: List<EvidencePhoto>,
        notes: String,
    ): Result<Unit> =
        runCatching {
            val uid = auth.currentUser?.uid.orEmpty()
            val evidenceId = generateUlid()
            commitEvidenceBatch(taskId, uid, evidenceId, photos, notes)
        }

    private suspend fun commitEvidenceBatch(
        taskId: String,
        inspectorUid: String,
        evidenceId: String,
        photos: List<EvidencePhoto>,
        notes: String,
    ) {
        val photoMaps: List<Map<String, Any>> =
            photos.map { p ->
                mapOf(
                    "id" to p.id,
                    "latitude" to p.latitude,
                    "longitude" to p.longitude,
                    "capturedAtMs" to p.capturedAtMs,
                    "storageUrl" to p.storageUrl,
                )
            }

        val evidenceData: Map<String, Any> =
            mapOf(
                "taskId" to taskId,
                "inspectorUid" to inspectorUid,
                "photos" to photoMaps,
                "notes" to notes,
                "submittedAtMs" to FieldValue.serverTimestamp(),
                "status" to "PENDING",
            )

        val batch = db.batch()
        batch.set(db.collection("evidence").document(evidenceId), evidenceData)
        batch.update(db.document("tasks/$taskId"), "status", "EVIDENCE_SUBMITTED", "evidenceId", evidenceId)

        suspendCancellableCoroutine { cont ->
            batch
                .commit()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }
}
