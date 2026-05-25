package com.maku.eyeonsite.domain

data class EvidencePhoto(
    val id: String,
    val taskId: String,
    val latitude: Double,
    val longitude: Double,
    val capturedAtMs: Long,
    val uploadStatus: UploadStatus = UploadStatus.LOCAL,
    val storageUrl: String = "",
)
