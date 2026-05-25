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

@JsFun("() => window.location.pathname")
private external fun jsGetPathname(): String

actual fun currentPath(): String = jsGetPathname()

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

@JsFun("(path) => { window.location.href = path; }")
private external fun jsSetHref(path: String)

actual fun navigateTo(path: String) = jsSetHref(path)
