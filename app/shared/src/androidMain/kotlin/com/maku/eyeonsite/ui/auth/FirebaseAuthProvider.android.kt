package com.maku.eyeonsite.ui.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.maku.eyeonsite.domain.UserRole
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val OTP_TIMEOUT_SECONDS = 60L

actual fun createFirebaseAuthProvider(): FirebaseAuthProvider = AndroidFirebaseAuthProvider()

class AndroidFirebaseAuthProvider : FirebaseAuthProvider {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun sendOtp(phone: String): Result<String> =
        suspendCoroutine { cont ->
            val callbacks =
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        cont.resume(Result.success("auto"))
                    }

                    override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                        cont.resume(Result.failure(e))
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken,
                    ) {
                        cont.resume(Result.success(verificationId))
                    }
                }

            val builder =
                PhoneAuthOptions
                    .newBuilder(auth)
                    .setPhoneNumber(phone)
                    .setTimeout(OTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .setCallbacks(callbacks)

            ActivityHolder.activity?.let { builder.setActivity(it) }
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }

    override suspend fun verifyOtp(
        verificationId: String,
        code: String,
    ): Result<String> {
        if (verificationId == "auto") {
            val uid = auth.currentUser?.uid
            return if (uid != null) {
                Result.success(uid)
            } else {
                Result.failure(Exception("Auto-verification failed: no user"))
            }
        }
        return suspendCoroutine { cont ->
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            auth
                .signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    cont.resume(Result.success(result.user?.uid.orEmpty()))
                }.addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }
    }

    override fun currentUid(): String? = auth.currentUser?.uid

    override suspend fun fetchRole(uid: String): UserRole? =
        suspendCoroutine { cont ->
            firestore
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val roleStr = doc.getString("role")
                    cont.resume(roleStr?.let { runCatching { UserRole.valueOf(it) }.getOrNull() })
                }.addOnFailureListener { cont.resume(null) }
        }

    override suspend fun saveRole(
        uid: String,
        role: UserRole,
    ) = suspendCoroutine<Unit> { cont ->
        firestore
            .collection("users")
            .document(uid)
            .set(mapOf("role" to role.name), SetOptions.merge())
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { cont.resume(Unit) }
    }

    override fun signOut() = auth.signOut()
}

object ActivityHolder {
    var activity: Activity? = null
}
