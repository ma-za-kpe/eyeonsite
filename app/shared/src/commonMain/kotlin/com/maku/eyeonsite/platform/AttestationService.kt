package com.maku.eyeonsite.platform

import com.maku.eyeonsite.domain.AttestationProvider
import com.maku.eyeonsite.domain.AttestationVerdict
import com.maku.eyeonsite.domain.DeviceAttestation
import kotlinx.datetime.Clock

interface AttestationService {
    suspend fun attest(): DeviceAttestation
}

object MockAttestationService : AttestationService {
    override suspend fun attest(): DeviceAttestation =
        DeviceAttestation(
            provider = AttestationProvider.MOCK,
            verdict = AttestationVerdict.PASSED,
            checkedAtMs = Clock.System.now().toEpochMilliseconds(),
        )
}
