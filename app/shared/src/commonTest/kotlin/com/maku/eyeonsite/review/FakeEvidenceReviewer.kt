package com.maku.eyeonsite.review

import com.maku.eyeonsite.domain.ReviewDecision
import com.maku.eyeonsite.ui.review.EvidenceReviewer

class FakeEvidenceReviewer(
    private val shouldFail: Boolean = false,
) : EvidenceReviewer {
    val submissions = mutableListOf<Pair<String, ReviewDecision>>()

    override suspend fun submitDecision(
        taskId: String,
        decision: ReviewDecision,
        notes: String,
    ): Result<Unit> =
        if (shouldFail) {
            Result.failure(RuntimeException("Server error"))
        } else {
            submissions.add(taskId to decision)
            Result.success(Unit)
        }
}
