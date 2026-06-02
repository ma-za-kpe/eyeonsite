package com.maku.eyeonsite.auth

import com.maku.eyeonsite.domain.UserRole
import com.maku.eyeonsite.ui.auth.FirebaseAuthProvider

class FakeFirebaseAuthProvider : FirebaseAuthProvider {
    var sendOtpResult: Result<String> = Result.success("fake-verification-id")
    var verifyOtpResult: Result<String> = Result.success("fake-uid-001")
    var storedUid: String? = null
    var storedRole: UserRole? = null
    var signOutCalled = false

    val sendOtpCalls = mutableListOf<String>()
    val verifyOtpCalls = mutableListOf<Pair<String, String>>()

    override suspend fun sendOtp(phone: String): Result<String> {
        sendOtpCalls.add(phone)
        return sendOtpResult
    }

    override suspend fun verifyOtp(
        verificationId: String,
        code: String,
    ): Result<String> {
        verifyOtpCalls.add(verificationId to code)
        if (verifyOtpResult.isSuccess) storedUid = verifyOtpResult.getOrNull()
        return verifyOtpResult
    }

    override fun currentUid(): String? = storedUid

    override suspend fun fetchRole(uid: String): UserRole? = storedRole

    override suspend fun saveRole(
        uid: String,
        role: UserRole,
    ) {
        storedRole = role
    }

    override fun signOut() {
        signOutCalled = true
        storedUid = null
        storedRole = null
    }
}
