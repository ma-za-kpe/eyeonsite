/** FirebaseAnalyticsService — Android implementation backed by Firebase Analytics. */
package com.maku.eyeonsite.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/** Production analytics using Firebase. Registered via Koin in androidMain. */
class FirebaseAnalyticsService : AnalyticsService {

    private val analytics: FirebaseAnalytics = Firebase.analytics

    override fun logEvent(name: String, params: Map<String, String>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) -> putString(key, value) }
        }
        analytics.logEvent(name, bundle)
    }

    override fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}
