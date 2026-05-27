@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.ui.review

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.maku.eyeonsite.domain.DisputeReason
import com.maku.eyeonsite.domain.ReviewDecision
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreEvidenceReviewer : EvidenceReviewer {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun submitDecision(
        taskId: String,
        decision: ReviewDecision,
        notes: String,
        disputeReason: DisputeReason?,
    ): Result<Unit> =
        runCatching {
            val uid = auth.currentUser?.uid.orEmpty()
            when (decision) {
                ReviewDecision.ACCEPTED -> acceptTask(taskId, uid, notes)
                ReviewDecision.REVISION_REQUESTED -> requestRevision(taskId, uid, notes)
                ReviewDecision.DISPUTED -> disputeTask(taskId, uid, notes, disputeReason ?: DisputeReason.OTHER)
            }
        }

    private suspend fun acceptTask(
        taskId: String,
        uid: String,
        notes: String,
    ) {
        suspendCancellableCoroutine { cont ->
            db
                .document("tasks/$taskId")
                .update(
                    "status",
                    "ACCEPTED",
                    "reviewNotes",
                    notes,
                    "reviewedByUid",
                    uid,
                ).addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    private suspend fun requestRevision(
        taskId: String,
        uid: String,
        notes: String,
    ) {
        // Status set to REVISION_REQUESTED; onTaskReviewed Cloud Function
        // rolls it back to IN_PROGRESS and notifies the inspector via FCM.
        suspendCancellableCoroutine { cont ->
            db
                .document("tasks/$taskId")
                .update(
                    "status",
                    "REVISION_REQUESTED",
                    "revisionNotes",
                    notes,
                    "reviewedByUid",
                    uid,
                ).addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    private suspend fun disputeTask(
        taskId: String,
        uid: String,
        notes: String,
        reason: DisputeReason,
    ) {
        // Batch: update task + create disputes/{taskId} document.
        // onTaskReviewed Cloud Function notifies the inspector via FCM.
        suspendCancellableCoroutine { cont ->
            val batch = db.batch()
            batch.update(
                db.document("tasks/$taskId"),
                mapOf(
                    "status" to "DISPUTED",
                    "disputeReason" to reason.name,
                    "reviewNotes" to notes,
                    "reviewedByUid" to uid,
                ),
            )
            batch.set(
                db.collection("disputes").document(taskId),
                mapOf(
                    "taskId" to taskId,
                    "reason" to reason.name,
                    "notes" to notes,
                    "reviewedByUid" to uid,
                    "createdAtMs" to FieldValue.serverTimestamp(),
                    "status" to "OPEN",
                ),
            )
            batch
                .commit()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }
}
