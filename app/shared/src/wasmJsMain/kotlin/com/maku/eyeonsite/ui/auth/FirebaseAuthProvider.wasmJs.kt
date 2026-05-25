package com.maku.eyeonsite.ui.auth

actual fun createFirebaseAuthProvider(): FirebaseAuthProvider = StubFirebaseAuthProvider("WasmJs auth not applicable")
