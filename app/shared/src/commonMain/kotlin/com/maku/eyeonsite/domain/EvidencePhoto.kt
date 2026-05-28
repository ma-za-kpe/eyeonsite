package com.maku.eyeonsite.domain

data class EvidencePhoto(
    val id: String,
    val taskId: String,
    val latitude: Double,
    val longitude: Double,
    val capturedAtMs: Long,
    val uploadStatus: UploadStatus = UploadStatus.LOCAL,
    val storageUrl: String = "",
    val storagePath: String = "",
    val accuracyMeters: Double? = null,
    val locationProvider: LocationProvider = LocationProvider.UNKNOWN,
    val isMockLocation: Boolean = false,
    val capturedAtDeviceMs: Long = 0L,
    val receivedAtServerMs: Long = 0L,
    val exif: ExifMetadata = ExifMetadata(),
    val perceptualHash: String = "",
)
