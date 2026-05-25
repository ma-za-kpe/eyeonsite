package com.maku.eyeonsite.ui.auth

internal class StubFirebaseAuthProvider(
    private val reason: String,
) : FirebaseAuthProvider {
    override suspend fun sendOtp(phone: String): Result<String> = Result.failure(UnsupportedOperationException(reason))

    override suspend fun verifyOtp(
        verificationId: String,
        code: String,
    ): Result<String> = Result.failure(UnsupportedOperationException(reason))

    override fun currentUid(): String? = null

    override fun signOut() = Unit
}
