package com.maku.eyeonsite.onboarding

data class ProfileSubmission(
    val uid: String,
    val fullName: String,
    val city: String,
    val serviceRadiusKm: Int,
    val testScore: Int,
)

class FakeInspectorOnboardingRepository(
    private val uploadShouldFail: Boolean = false,
    private val submitShouldFail: Boolean = false,
) : InspectorOnboardingRepository {
    val uploadCalls = mutableListOf<String>()
    val submissions = mutableListOf<ProfileSubmission>()

    override suspend fun uploadIdDocuments(
        uid: String,
        idBytes: ByteArray,
        selfieBytes: ByteArray,
    ): Result<Pair<String, String>> =
        if (uploadShouldFail) {
            Result.failure(RuntimeException("Upload failed"))
        } else {
            uploadCalls.add(uid)
            Result.success("https://storage/id_$uid.jpg" to "https://storage/selfie_$uid.jpg")
        }

    override suspend fun submitProfile(
        uid: String,
        fullName: String,
        city: String,
        serviceRadiusKm: Int,
        testScore: Int,
        idDocUrl: String,
        selfieUrl: String,
    ): Result<Unit> =
        if (submitShouldFail) {
            Result.failure(RuntimeException("Submit failed"))
        } else {
            submissions.add(ProfileSubmission(uid, fullName, city, serviceRadiusKm, testScore))
            Result.success(Unit)
        }
}
