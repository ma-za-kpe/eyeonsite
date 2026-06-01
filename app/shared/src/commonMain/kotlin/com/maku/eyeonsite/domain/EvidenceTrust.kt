package com.maku.eyeonsite.domain

enum class LocationProvider {
    UNKNOWN,
    GPS,
    NETWORK,
    FUSED,
    MANUAL,
}

fun String.toLocationProvider(): LocationProvider =
    when (this.lowercase()) {
        "gps" -> LocationProvider.GPS
        "fused" -> LocationProvider.FUSED
        "network" -> LocationProvider.NETWORK
        "" -> LocationProvider.MANUAL
        else -> LocationProvider.UNKNOWN
    }

data class ExifMetadata(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val capturedAtMs: Long = 0L,
    val cameraMake: String = "",
    val cameraModel: String = "",
) {
    fun hasGps(): Boolean = latitude != null && longitude != null
}

enum class AttestationProvider {
    NONE,
    PLAY_INTEGRITY,
    MOCK,
}

enum class AttestationVerdict {
    UNKNOWN,
    PASSED,
    FAILED,
    UNAVAILABLE,
}

data class DeviceAttestation(
    val provider: AttestationProvider = AttestationProvider.NONE,
    val verdict: AttestationVerdict = AttestationVerdict.UNKNOWN,
    val checkedAtMs: Long = 0L,
    val reason: String = "",
)

enum class FraudSignalType {
    MOCK_LOCATION,
    LOW_ACCURACY,
    GPS_EXIF_MISMATCH,
    TIMESTAMP_DRIFT,
    IMPOSSIBLE_TRAVEL,
    PHASH_DUPLICATE,
    ATTESTATION_FAILED,
    CONTENT_UNSAFE,
}

enum class FraudSignalSeverity {
    INFO,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}

data class FraudSignal(
    val type: FraudSignalType,
    val severity: FraudSignalSeverity,
    val message: String = "",
    val source: String = "",
    val observedAtMs: Long = 0L,
)

enum class ContentSafetyVerdict {
    UNKNOWN,
    SAFE,
    NEEDS_REVIEW,
    UNSAFE,
}

data class ContentSafety(
    val verdict: ContentSafetyVerdict = ContentSafetyVerdict.UNKNOWN,
    val provider: String = "",
    val checkedAtMs: Long = 0L,
    val reason: String = "",
)
