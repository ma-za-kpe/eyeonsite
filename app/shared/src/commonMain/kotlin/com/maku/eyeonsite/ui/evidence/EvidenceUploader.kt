package com.maku.eyeonsite.ui.evidence

import com.maku.eyeonsite.domain.EvidencePhoto

interface EvidenceUploader {
    suspend fun upload(
        photo: EvidencePhoto,
        bytes: ByteArray,
    ): Result<String>
}

class NoOpEvidenceUploader : EvidenceUploader {
    override suspend fun upload(
        photo: EvidencePhoto,
        bytes: ByteArray,
    ): Result<String> = Result.success("https://storage.googleapis.com/eyeonsite/${photo.id}.jpg")
}
