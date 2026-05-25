package com.maku.eyeonsite.ui.review

import com.maku.eyeonsite.domain.ReviewDecision

interface EvidenceReviewer {
    suspend fun submitDecision(
        taskId: String,
        decision: ReviewDecision,
        notes: String = "",
    ): Result<Unit>
}

class NoOpEvidenceReviewer : EvidenceReviewer {
    override suspend fun submitDecision(
        taskId: String,
        decision: ReviewDecision,
        notes: String,
    ): Result<Unit> = Result.success(Unit)
}
