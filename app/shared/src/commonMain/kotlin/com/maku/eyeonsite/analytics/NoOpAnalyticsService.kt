package com.maku.eyeonsite.analytics

internal object NoOpAnalyticsService : AnalyticsService {
    override fun logEvent(
        name: String,
        params: Map<String, String>,
    ) = Unit

    override fun logScreenView(screenName: String) = Unit
}
