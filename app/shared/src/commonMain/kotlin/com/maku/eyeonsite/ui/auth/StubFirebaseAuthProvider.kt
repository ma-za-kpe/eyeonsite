package com.maku.eyeonsite.ui.auth

import com.maku.eyeonsite.domain.UserRole

internal class StubFirebaseAuthProvider(
    private val reason: String,
) : FirebaseAuthProvider {
    override suspend fun sendOtp(phone: String): Result<String> = Result.failure(UnsupportedOperationException(reason))

    override suspend fun verifyOtp(
        verificationId: String,
        code: String,
    ): Result<String> = Result.failure(UnsupportedOperationException(reason))

    override fun currentUid(): String? = null

    override suspend fun fetchRole(uid: String): UserRole? = null

    override suspend fun saveRole(
        uid: String,
        role: UserRole,
    ) = Unit

    override fun signOut() = Unit
}
