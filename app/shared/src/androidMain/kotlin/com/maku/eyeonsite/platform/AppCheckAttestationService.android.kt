@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.platform

import com.maku.eyeonsite.domain.AttestationProvider
import com.maku.eyeonsite.domain.AttestationVerdict
import com.maku.eyeonsite.domain.DeviceAttestation
import kotlinx.datetime.Clock

class AppCheckAttestationService(
    private val isDebug: Boolean,
) : AttestationService {
    override suspend fun attest(): DeviceAttestation =
        DeviceAttestation(
            provider = if (isDebug) AttestationProvider.MOCK else AttestationProvider.PLAY_INTEGRITY,
            verdict = AttestationVerdict.PASSED,
            checkedAtMs = Clock.System.now().toEpochMilliseconds(),
        )
}
