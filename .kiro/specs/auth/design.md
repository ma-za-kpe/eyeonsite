# Authentication — Design

## State Machine
```
IDLE → OTP_SENT → VERIFYING → ROLE_SELECTION (new user)
                             → AUTHENTICATED(role) (returning)
                → ERROR
```

## Screens
```
shared/commonMain/ui/auth/
├── SignInScreen.kt         # exists — phone entry
├── OtpScreen.kt            # 6-digit code entry
├── RoleSelectionScreen.kt  # CLIENT vs INSPECTOR
└── AuthViewModel.kt        # state + auth calls
```

## AuthState (commonMain)
```kotlin
sealed class AuthState {
    object Idle : AuthState()
    object SendingOtp : AuthState()
    object OtpSent : AuthState()
    object Verifying : AuthState()
    data class NeedsRole(val uid: String) : AuthState()
    data class Authenticated(val uid: String, val role: UserRole) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

## expect/actual FirebaseAuthProvider
```kotlin
// commonMain — interface
expect class FirebaseAuthProvider {
    suspend fun sendOtp(phone: String): Result<String>       // verificationId
    suspend fun verifyOtp(id: String, code: String): Result<String>  // uid
    fun currentUid(): String?
    fun signOut()
}
// androidMain, iosMain, jsMain — platform implementations
```

## Firestore Schema
```
users/{uid}: { phone, displayName, role, createdAt }
```

## Analytics Events
- `auth_otp_requested` | `auth_otp_verified` | `auth_role_selected` (role param) | `auth_sign_out`
