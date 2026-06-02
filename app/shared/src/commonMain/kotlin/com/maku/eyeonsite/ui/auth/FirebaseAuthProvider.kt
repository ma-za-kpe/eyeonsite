package com.maku.eyeonsite.ui.auth

import com.maku.eyeonsite.domain.UserRole

interface FirebaseAuthProvider {
    suspend fun sendOtp(phone: String): Result<String>

    suspend fun verifyOtp(
        verificationId: String,
        code: String,
    ): Result<String>

    fun currentUid(): String?

    suspend fun fetchRole(uid: String): UserRole?

    suspend fun saveRole(
        uid: String,
        role: UserRole,
    )

    fun signOut()
}

expect fun createFirebaseAuthProvider(): FirebaseAuthProvider
