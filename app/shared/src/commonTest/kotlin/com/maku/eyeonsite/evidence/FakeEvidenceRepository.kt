package com.maku.eyeonsite.evidence

import com.maku.eyeonsite.data.EvidenceRepository
import com.maku.eyeonsite.domain.EvidencePhoto

class FakeEvidenceRepository(
    private val shouldFail: Boolean = false,
) : EvidenceRepository {
    data class Submission(
        val taskId: String,
        val photos: List<EvidencePhoto>,
        val notes: String,
    )

    val submissions = mutableListOf<Submission>()

    override suspend fun submitEvidence(
        taskId: String,
        photos: List<EvidencePhoto>,
        pendingBytes: Map<String, ByteArray>,
        notes: String,
    ): Result<Unit> =
        if (shouldFail) {
            Result.failure(Exception("Storage unavailable"))
        } else {
            submissions.add(Submission(taskId, photos, notes))
            Result.success(Unit)
        }
}
