package com.maku.eyeonsite.onboarding

import com.maku.eyeonsite.domain.KycCheck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface InspectorOnboardingRepository {
    suspend fun uploadIdDocuments(
        uid: String,
        idBytes: ByteArray,
        selfieBytes: ByteArray,
    ): Result<Pair<String, String>>

    suspend fun submitProfile(
        uid: String,
        fullName: String,
        city: String,
        serviceRadiusKm: Int,
        testScore: Int,
        idDocPath: String,
        selfiePath: String,
    ): Result<Unit>

    fun observeKycStatus(uid: String): Flow<KycCheck>
}

class NoOpInspectorOnboardingRepository : InspectorOnboardingRepository {
    override suspend fun uploadIdDocuments(
        uid: String,
        idBytes: ByteArray,
        selfieBytes: ByteArray,
    ): Result<Pair<String, String>> = Result.success("" to "")

    override suspend fun submitProfile(
        uid: String,
        fullName: String,
        city: String,
        serviceRadiusKm: Int,
        testScore: Int,
        idDocPath: String,
        selfiePath: String,
    ): Result<Unit> = Result.success(Unit)

    override fun observeKycStatus(uid: String): Flow<KycCheck> = flowOf(KycCheck())
}
