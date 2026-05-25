package com.maku.eyeonsite.ui.auth

import com.maku.eyeonsite.domain.UserRole

sealed class AuthState {
    data object Idle : AuthState()

    data object SendingOtp : AuthState()

    data object OtpSent : AuthState()

    data object Verifying : AuthState()

    data class NeedsRole(
        val uid: String,
    ) : AuthState()

    data class Authenticated(
        val uid: String,
        val role: UserRole,
    ) : AuthState()

    data class Error(
        val message: String,
    ) : AuthState()
}
