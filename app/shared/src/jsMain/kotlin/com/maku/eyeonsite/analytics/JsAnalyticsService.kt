package com.maku.eyeonsite.analytics

class JsAnalyticsService : AnalyticsService {
    override fun logEvent(
        name: String,
        params: Map<String, String>,
    ) {
        val jsParams: dynamic = js("{}")
        params.forEach { (k, v) -> jsParams[k] = v }
        tryGtag(name, jsParams)
    }

    override fun logScreenView(screenName: String) {
        val jsParams: dynamic = js("{}")
        jsParams["screen_name"] = screenName
        tryGtag("screen_view", jsParams)
    }
}

@Suppress("TooGenericExceptionCaught", "SwallowedException")
private fun tryGtag(
    event: String,
    params: dynamic,
) {
    try {
        val w: dynamic = js("window")
        w.gtag("event", event, params)
    } catch (e: Throwable) {
        console.log("[Analytics] $event", params)
    }
}
