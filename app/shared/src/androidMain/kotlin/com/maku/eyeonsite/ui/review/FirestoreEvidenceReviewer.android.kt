@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.ui.review

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    ): Result<Unit> =
        runCatching {
            val uid = auth.currentUser?.uid.orEmpty()
            val status =
                when (decision) {
                    ReviewDecision.ACCEPTED -> "ACCEPTED"
                    ReviewDecision.DISPUTED -> "DISPUTED"
                    ReviewDecision.REVISION_REQUESTED -> "REVISION_REQUESTED"
                }
            val taskRef = db.document("tasks/$taskId")
            suspendCancellableCoroutine { cont ->
                taskRef
                    .update("status", status, "reviewNotes", notes, "reviewedByUid", uid)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
        }
}
