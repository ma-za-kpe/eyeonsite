@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.maku.eyeonsite.admin.AdminPage
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.landing.LandingPage
import com.maku.eyeonsite.legal.PrivacyPage
import com.maku.eyeonsite.legal.TermsPage
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme

private object WebAnalytics : AnalyticsService {
    override fun logEvent(
        name: String,
        params: Map<String, String>,
    ) {
        trackAnalyticsEvent(name, params)
    }

    override fun logScreenView(screenName: String) {
        trackAnalyticsEvent("screen_view", mapOf("screen_name" to screenName))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("ComposeTarget") {
        EyeOnSiteTheme {
            when (currentPath()) {
                "/mazakpe" -> AdminPage()
                "/privacy" -> PrivacyPage()
                "/terms" -> TermsPage()
                else ->
                    LandingPage(
                        analytics = WebAnalytics,
                        onWaitlistSubmit = { entry, onSuccess, onError ->
                            submitWaitlistEntry(entry, onSuccess, onError)
                        },
                    )
            }
        }
    }
}
