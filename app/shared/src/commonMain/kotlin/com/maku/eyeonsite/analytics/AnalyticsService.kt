/** AnalyticsService — platform-agnostic analytics interface. All screens use this, never Firebase directly. */
package com.maku.eyeonsite.analytics

/** Tracks user events across all platforms. Inject via DI; use MockAnalyticsService in tests. */
interface AnalyticsService {
    /** Log a named event with optional string parameters. */
    fun logEvent(name: String, params: Map<String, String> = emptyMap())

    /** Log a screen view. Call from every screen's LaunchedEffect. */
    fun logScreenView(screenName: String)
}
