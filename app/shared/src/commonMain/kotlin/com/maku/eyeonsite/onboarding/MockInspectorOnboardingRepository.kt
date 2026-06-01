package com.maku.eyeonsite.onboarding

import com.maku.eyeonsite.domain.KycCheck
import com.maku.eyeonsite.domain.KycProvider
import com.maku.eyeonsite.domain.KycStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockInspectorOnboardingRepository : InspectorOnboardingRepository {
    override suspend fun uploadIdDocuments(
        uid: String,
        idBytes: ByteArray,
        selfieBytes: ByteArray,
    ): Result<Pair<String, String>> = Result.success("mock_id_docs/$uid/govt_id.jpg" to "mock_id_docs/$uid/selfie.jpg")

    override suspend fun submitProfile(
        uid: String,
        fullName: String,
        city: String,
        serviceRadiusKm: Int,
        testScore: Int,
        idDocPath: String,
        selfiePath: String,
    ): Result<Unit> = Result.success(Unit)

    override fun observeKycStatus(uid: String): Flow<KycCheck> = flowOf(KycCheck(provider = KycProvider.MOCK, status = KycStatus.APPROVED))
}
