package com.maku.eyeonsite.ui.auth

actual fun createFirebaseAuthProvider(): FirebaseAuthProvider = StubFirebaseAuthProvider("Web auth not applicable — web uses waitlist only")
