package com.maku.eyeonsite.ui.review

import com.maku.eyeonsite.domain.DisputeReason
import com.maku.eyeonsite.domain.ReviewDecision

interface EvidenceReviewer {
    suspend fun submitDecision(
        taskId: String,
        decision: ReviewDecision,
        notes: String = "",
        disputeReason: DisputeReason? = null,
    ): Result<Unit>
}

class NoOpEvidenceReviewer : EvidenceReviewer {
    override suspend fun submitDecision(
        taskId: String,
        decision: ReviewDecision,
        notes: String,
        disputeReason: DisputeReason?,
    ): Result<Unit> = Result.success(Unit)
}
