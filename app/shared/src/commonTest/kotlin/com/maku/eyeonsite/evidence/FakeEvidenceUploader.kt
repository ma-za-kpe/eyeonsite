package com.maku.eyeonsite.evidence

import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.ui.evidence.EvidenceUploader

class FakeEvidenceUploader(
    private val shouldFail: Boolean = false,
) : EvidenceUploader {
    val uploadedPhotoIds = mutableListOf<String>()

    override suspend fun upload(
        photo: EvidencePhoto,
        bytes: ByteArray,
    ): Result<String> =
        if (shouldFail) {
            Result.failure(Exception("Network unavailable"))
        } else {
            uploadedPhotoIds += photo.id
            Result.success("https://storage.googleapis.com/fake/${photo.id}.jpg")
        }
}
