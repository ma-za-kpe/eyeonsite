package com.maku.eyeonsite.domain

data class EvidencePackage(
    val taskId: String,
    val photos: List<EvidencePhoto>,
    val qualityScore: Float = 0f,
    val attestation: DeviceAttestation = DeviceAttestation(),
    val contentSafety: ContentSafety = ContentSafety(),
    val fraudSignals: List<FraudSignal> = emptyList(),
    val provenanceManifestHash: String = "",
) {
    fun isValid(): Boolean = photos.isNotEmpty() && photos.size <= MAX_PHOTOS

    fun hasHighRiskSignals(): Boolean =
        fraudSignals.any {
            it.severity == FraudSignalSeverity.HIGH || it.severity == FraudSignalSeverity.CRITICAL
        }

    fun isTrustedForAutoAccept(): Boolean =
        isValid() &&
            qualityScore >= AUTO_ACCEPT_QUALITY_THRESHOLD &&
            attestation.verdict == AttestationVerdict.PASSED &&
            contentSafety.verdict == ContentSafetyVerdict.SAFE &&
            !hasHighRiskSignals()

    companion object {
        const val MAX_PHOTOS = 10
        private const val AUTO_ACCEPT_QUALITY_THRESHOLD = 0.75f
    }
}
