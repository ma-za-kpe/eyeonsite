package com.maku.eyeonsite

import com.maku.eyeonsite.admin.WaitlistDocument
import com.maku.eyeonsite.landing.waitlist.WaitlistEntry

actual fun submitWaitlistEntry(
    entry: WaitlistEntry,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    onError("WASM: not supported")
}

actual fun currentPath(): String = "/"

actual fun signInAdmin(
    email: String,
    password: String,
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit,
) {
    onError("WASM: not supported")
}

actual fun fetchWaitlist(
    idToken: String,
    onSuccess: (List<WaitlistDocument>) -> Unit,
    onError: (String) -> Unit,
) {
    onSuccess(emptyList())
}

actual fun trackAnalyticsEvent(
    name: String,
    params: Map<String, String>,
) { /* no-op */ }

actual fun navigateTo(path: String) { /* no-op */ }
