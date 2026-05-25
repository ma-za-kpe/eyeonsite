package com.maku.eyeonsite

import com.maku.eyeonsite.admin.WaitlistDocument
import com.maku.eyeonsite.landing.waitlist.WaitlistEntry

expect fun submitWaitlistEntry(
    entry: WaitlistEntry,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
)

expect fun currentPath(): String

expect fun signInAdmin(
    email: String,
    password: String,
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit,
)

expect fun fetchWaitlist(
    idToken: String,
    onSuccess: (List<WaitlistDocument>) -> Unit,
    onError: (String) -> Unit,
)

expect fun trackAnalyticsEvent(
    name: String,
    params: Map<String, String>,
)

expect fun navigateTo(path: String)
