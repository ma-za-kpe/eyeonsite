package com.maku.eyeonsite.ui.auth

actual fun createFirebaseAuthProvider(): FirebaseAuthProvider = StubFirebaseAuthProvider("iOS Firebase Auth not yet wired")
