package com.maku.eyeonsite.onboarding

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
        idDocUrl: String,
        selfieUrl: String,
    ): Result<Unit>
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
        idDocUrl: String,
        selfieUrl: String,
    ): Result<Unit> = Result.success(Unit)
}
