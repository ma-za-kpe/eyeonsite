/** JsAnalyticsService — JS/Web analytics stub. Logs to console in dev; wire to gtag in prod. */
package com.maku.eyeonsite.analytics

/**
 * Web analytics implementation for Kotlin/JS.
 * Currently logs to browser console. Replace with real gtag calls when
 * Firebase Analytics JS SDK is wired into index.html.
 */
class JsAnalyticsService : AnalyticsService {
    override fun logEvent(
        name: String,
        params: Map<String, String>,
    ) {
        val paramsStr = if (params.isEmpty()) "" else " ${params.entries.joinToString { "${it.key}=${it.value}" }}"
        console.log("[Analytics] $name$paramsStr")
    }

    override fun logScreenView(screenName: String) {
        console.log("[Analytics] screen_view screen=$screenName")
    }
}
