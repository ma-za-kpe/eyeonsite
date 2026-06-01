package com.maku.eyeonsite.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.domain.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PHONE_LOG_PREFIX_LENGTH = 4

class AuthViewModel(
    private val authProvider: FirebaseAuthProvider,
    private val analytics: AnalyticsService,
) : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.CheckingSession)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private var pendingVerificationId: String? = null

    init {
        viewModelScope.launch {
            val uid = authProvider.currentUid()
            _state.value =
                if (uid == null) {
                    AuthState.Idle
                } else {
                    val role = authProvider.fetchRole(uid)
                    if (role != null) AuthState.Authenticated(uid, role) else AuthState.NeedsRole(uid)
                }
        }
    }

    fun sendOtp(phone: String) {
        _phone.value = phone
        viewModelScope.launch {
            _state.value = AuthState.SendingOtp
            authProvider
                .sendOtp(phone)
                .onSuccess { verificationId ->
                    pendingVerificationId = verificationId
                    _state.value = AuthState.OtpSent
                    analytics.logEvent("auth_otp_requested", mapOf("phone_prefix" to phone.take(PHONE_LOG_PREFIX_LENGTH)))
                }.onFailure { error ->
                    _state.value = AuthState.Error(error.message ?: "Failed to send OTP")
                }
        }
    }

    fun verifyOtp(code: String) {
        val verificationId =
            pendingVerificationId ?: run {
                _state.value = AuthState.Error("Session expired. Please request a new code.")
                return
            }
        viewModelScope.launch {
            _state.value = AuthState.Verifying
            authProvider
                .verifyOtp(verificationId, code)
                .onSuccess { uid ->
                    analytics.logEvent("auth_otp_verified", emptyMap())
                    _state.value = AuthState.NeedsRole(uid)
                }.onFailure { error ->
                    _state.value = AuthState.Error(error.message ?: "Invalid code. Please try again.")
                }
        }
    }

    fun selectRole(
        uid: String,
        role: UserRole,
    ) {
        analytics.logEvent("auth_role_selected", mapOf("role" to role.name.lowercase()))
        _state.value = AuthState.Authenticated(uid, role)
        viewModelScope.launch { authProvider.saveRole(uid, role) }
    }

    fun signOut() {
        authProvider.signOut()
        analytics.logEvent("auth_sign_out", emptyMap())
        pendingVerificationId = null
        _phone.value = ""
        _state.value = AuthState.Idle
    }

    fun dismissError() {
        _state.value = AuthState.Idle
    }
}
