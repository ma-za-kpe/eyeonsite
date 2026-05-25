package com.maku.eyeonsite.landing.waitlist

data class WaitlistEntry(
    val name: String,
    val email: String,
    val role: WaitlistRole,
    val location: String,
    val message: String,
)

enum class WaitlistRole(
    val label: String,
    val apiValue: String,
) {
    DIASPORA_CLIENT("I need verification (diaspora)", "diaspora_client"),
    FIELD_AGENT("I want to inspect (local field agent)", "field_agent"),
}
