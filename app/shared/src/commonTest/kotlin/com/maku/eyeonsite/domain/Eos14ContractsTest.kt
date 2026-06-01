package com.maku.eyeonsite.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Eos14ContractsTest {
    private fun photo() =
        EvidencePhoto(
            id = "photo-1",
            taskId = "task-1",
            latitude = 5.603717,
            longitude = -0.186964,
            capturedAtMs = 1_748_000_000_000L,
        )

    private fun task() =
        VerificationTask(
            id = "task-1",
            clientUid = "client-1",
            type = TaskType.CONSTRUCTION,
            location = "Accra, Ghana",
            locationLat = 5.603717,
            locationLng = -0.186964,
            description = "Verify roof progress",
            status = TaskStatus.ESCROW_FUNDED,
        )

    @Test
    fun `evidence photo defaults to conservative trust metadata`() {
        val evidence = photo()

        assertEquals(null, evidence.accuracyMeters)
        assertEquals(LocationProvider.UNKNOWN, evidence.locationProvider)
        assertFalse(evidence.isMockLocation)
        assertEquals(0L, evidence.receivedAtServerMs)
        assertFalse(evidence.exif.hasGps())
        assertTrue(evidence.perceptualHash.isBlank())
    }

    @Test
    fun `evidence photo carries capture integrity metadata`() {
        val evidence =
            photo().copy(
                accuracyMeters = 4.5,
                locationProvider = LocationProvider.GPS,
                capturedAtDeviceMs = 1_748_000_000_100L,
                receivedAtServerMs = 1_748_000_000_900L,
                exif =
                    ExifMetadata(
                        latitude = 5.603700,
                        longitude = -0.186960,
                        capturedAtMs = 1_748_000_000_050L,
                        cameraMake = "Pixel",
                        cameraModel = "8",
                    ),
                perceptualHash = "phash:abcd1234",
            )

        assertEquals(4.5, evidence.accuracyMeters)
        assertEquals(LocationProvider.GPS, evidence.locationProvider)
        assertTrue(evidence.exif.hasGps())
        assertEquals("phash:abcd1234", evidence.perceptualHash)
    }

    @Test
    fun `evidence package carries quality attestation content safety and fraud signals`() {
        val pkg =
            EvidencePackage(
                taskId = "task-1",
                photos = listOf(photo()),
                qualityScore = 0.92f,
                attestation =
                    DeviceAttestation(
                        provider = AttestationProvider.PLAY_INTEGRITY,
                        verdict = AttestationVerdict.PASSED,
                        checkedAtMs = 1_748_000_001_000L,
                    ),
                contentSafety = ContentSafety(ContentSafetyVerdict.SAFE),
                fraudSignals =
                    listOf(
                        FraudSignal(
                            type = FraudSignalType.GPS_EXIF_MISMATCH,
                            severity = FraudSignalSeverity.HIGH,
                            message = "GPS and EXIF are too far apart",
                        ),
                    ),
                provenanceManifestHash = "sha256:manifest",
            )

        assertEquals(0.92f, pkg.qualityScore)
        assertEquals(AttestationVerdict.PASSED, pkg.attestation.verdict)
        assertTrue(pkg.hasHighRiskSignals())
        assertFalse(pkg.isTrustedForAutoAccept())
    }

    @Test
    fun `evidence package is trusted only with strong provenance and low risk`() {
        val pkg =
            EvidencePackage(
                taskId = "task-1",
                photos = listOf(photo()),
                qualityScore = 0.85f,
                attestation =
                    DeviceAttestation(
                        provider = AttestationProvider.PLAY_INTEGRITY,
                        verdict = AttestationVerdict.PASSED,
                    ),
                contentSafety = ContentSafety(ContentSafetyVerdict.SAFE),
            )

        assertTrue(pkg.isTrustedForAutoAccept())
    }

    @Test
    fun `verification task carries H3 and escrow state`() {
        val task =
            task().copy(
                h3Res7 = "87754e64dffffff",
                h3Res9 = "89754e64d2fffff",
                h3NeighborRes9 = listOf("89754e64d23ffff", "89754e64d27ffff"),
                escrow =
                    EscrowSnapshot(
                        status = EscrowStatus.HELD,
                        provider = MoneyProvider.STRIPE,
                        amountUsdCents = 10_000,
                        inspectorPayoutUsdCents = 8_000,
                        platformFeeUsdCents = 1_800,
                        escrowFeeUsdCents = 200,
                        providerPaymentId = "pi_test_123",
                    ),
            )

        assertTrue(task.hasH3Index())
        assertEquals(EscrowStatus.HELD, task.escrow.status)
        assertTrue(task.escrow.hasBalancedFeeBreakdown())
    }

    @Test
    fun `evidence package with MOCK attestation is trusted for auto accept in test environments`() {
        val pkg =
            EvidencePackage(
                taskId = "task-1",
                photos = listOf(photo()),
                qualityScore = 0.85f,
                attestation =
                    DeviceAttestation(
                        provider = AttestationProvider.MOCK,
                        verdict = AttestationVerdict.PASSED,
                    ),
                contentSafety = ContentSafety(ContentSafetyVerdict.SAFE),
            )

        assertTrue(pkg.isTrustedForAutoAccept())
        assertEquals(AttestationProvider.MOCK, pkg.attestation.provider)
        assertEquals(AttestationVerdict.PASSED, pkg.attestation.verdict)
    }

    @Test
    fun `inspector profile carries KYC check without raw private document data`() {
        val inspector =
            InspectorProfile(
                uid = "inspector-1",
                fullName = "Ama Mensah",
                city = "Accra",
                serviceRadiusKm = 12,
                kycCheck =
                    KycCheck(
                        provider = KycProvider.SMILE_ID,
                        status = KycStatus.PENDING_REVIEW,
                        providerJobId = "smile-job-123",
                        submittedAtMs = 1_748_000_000_000L,
                    ),
            )

        assertEquals(KycStatus.PENDING_REVIEW, inspector.kycCheck.status)
        assertEquals("smile-job-123", inspector.kycCheck.providerJobId)
        assertTrue(inspector.kycCheck.requiresProviderDecision())
    }
}
