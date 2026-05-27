package com.maku.eyeonsite.data

import com.maku.eyeonsite.domain.EvidencePhoto

interface EvidenceRepository {
    suspend fun submitEvidence(
        taskId: String,
        photos: List<EvidencePhoto>,
        pendingBytes: Map<String, ByteArray>,
        notes: String,
    ): Result<Unit>
}

class NoOpEvidenceRepository : EvidenceRepository {
    override suspend fun submitEvidence(
        taskId: String,
        photos: List<EvidencePhoto>,
        pendingBytes: Map<String, ByteArray>,
        notes: String,
    ): Result<Unit> = Result.success(Unit)
}
