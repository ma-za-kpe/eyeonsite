package com.maku.eyeonsite.onboarding

data class ProfileSubmission(
    val uid: String,
    val fullName: String,
    val city: String,
    val serviceRadiusKm: Int,
    val testScore: Int,
    val idDocPath: String,
    val selfiePath: String,
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
            Result.success("id_docs/$uid/govt_id.jpg" to "id_docs/$uid/selfie.jpg")
        }

    override suspend fun submitProfile(
        uid: String,
        fullName: String,
        city: String,
        serviceRadiusKm: Int,
        testScore: Int,
        idDocPath: String,
        selfiePath: String,
    ): Result<Unit> =
        if (submitShouldFail) {
            Result.failure(RuntimeException("Submit failed"))
        } else {
            submissions.add(ProfileSubmission(uid, fullName, city, serviceRadiusKm, testScore, idDocPath, selfiePath))
            Result.success(Unit)
        }
}
