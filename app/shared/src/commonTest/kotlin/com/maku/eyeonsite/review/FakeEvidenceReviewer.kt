package com.maku.eyeonsite.review

import com.maku.eyeonsite.domain.DisputeReason
import com.maku.eyeonsite.domain.ReviewDecision
import com.maku.eyeonsite.ui.review.EvidenceReviewer

data class ReviewSubmission(
    val taskId: String,
    val decision: ReviewDecision,
    val disputeReason: DisputeReason?,
)

class FakeEvidenceReviewer(
    private val shouldFail: Boolean = false,
) : EvidenceReviewer {
    val submissions = mutableListOf<ReviewSubmission>()

    override suspend fun submitDecision(
        taskId: String,
        decision: ReviewDecision,
        notes: String,
        disputeReason: DisputeReason?,
    ): Result<Unit> =
        if (shouldFail) {
            Result.failure(RuntimeException("Server error"))
        } else {
            submissions.add(ReviewSubmission(taskId, decision, disputeReason))
            Result.success(Unit)
        }
}
