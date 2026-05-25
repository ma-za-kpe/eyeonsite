/** FirebaseAnalyticsService — Android implementation backed by Firebase Analytics. */
package com.maku.eyeonsite.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/** Production analytics using Firebase. Construct from the Android app layer. */
class FirebaseAnalyticsService(
    private val analytics: FirebaseAnalytics,
) : AnalyticsService {
    constructor(context: Context) : this(FirebaseAnalytics.getInstance(context))

    override fun logEvent(
        name: String,
        params: Map<String, String>,
    ) {
        val bundle =
            Bundle().apply {
                params.forEach { (key, value) -> putString(key, value) }
            }
        analytics.logEvent(name, bundle)
    }

    override fun logScreenView(screenName: String) {
        val bundle =
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}
