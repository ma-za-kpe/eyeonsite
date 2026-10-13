@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.platform

import com.maku.eyeonsite.domain.AttestationProvider
import com.maku.eyeonsite.domain.AttestationVerdict
import com.maku.eyeonsite.domain.DeviceAttestation

class AppCheckAttestationService(
    private val isDebug: Boolean,
) : AttestationService {
    override suspend fun attest(): DeviceAttestation =
        DeviceAttestation(
            provider = if (isDebug) AttestationProvider.MOCK else AttestationProvider.NONE,
            verdict = AttestationVerdict.UNKNOWN,
            checkedAtMs = 0L,
            reason = if (isDebug) "debug_attestation_not_verified" else "server_attestation_not_configured",
        )
}
