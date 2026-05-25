package com.maku.eyeonsite.ui.auth

interface FirebaseAuthProvider {
    suspend fun sendOtp(phone: String): Result<String>

    suspend fun verifyOtp(
        verificationId: String,
        code: String,
    ): Result<String>

    fun currentUid(): String?

    fun signOut()
}

expect fun createFirebaseAuthProvider(): FirebaseAuthProvider
