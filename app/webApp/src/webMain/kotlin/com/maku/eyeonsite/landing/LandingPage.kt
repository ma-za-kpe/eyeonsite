package com.maku.eyeonsite.landing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.landing.components.NavBar
import com.maku.eyeonsite.landing.sections.EvidencePreviewSection
import com.maku.eyeonsite.landing.sections.FooterSection
import com.maku.eyeonsite.landing.sections.HeroSection
import com.maku.eyeonsite.landing.sections.HowItWorksSection
import com.maku.eyeonsite.landing.sections.PricingSection
import com.maku.eyeonsite.landing.sections.ProblemSection
import com.maku.eyeonsite.landing.sections.TrustSection
import com.maku.eyeonsite.landing.sections.VerticalsSection
import com.maku.eyeonsite.landing.sections.WaitlistSection
import com.maku.eyeonsite.landing.waitlist.WaitlistEntry
import kotlinx.coroutines.launch

@Composable
fun LandingPage(
    analytics: AnalyticsService,
    onWaitlistSubmit: (WaitlistEntry, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var howItWorksY by remember { mutableStateOf(0) }
    var pricingY by remember { mutableStateOf(0) }
    var waitlistY by remember { mutableStateOf(0) }

    fun scrollTo(y: Int) {
        scope.launch { scrollState.animateScrollTo(y) }
    }

    LaunchedEffect(Unit) {
        analytics.logScreenView("landing")
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState),
    ) {
        NavBar(
            analytics = analytics,
            onHowItWorksClick = { scrollTo(howItWorksY) },
            onPricingClick = { scrollTo(pricingY) },
            onInspectorsClick = { scrollTo(waitlistY) },
            onGetStartedClick = { scrollTo(waitlistY) },
        )
        HeroSection(
            analytics = analytics,
            onGetStarted = { scrollTo(waitlistY) },
            onForInspectors = { scrollTo(waitlistY) },
        )
        ProblemSection(analytics = analytics)
        HowItWorksSection(
            analytics = analytics,
            modifier =
                Modifier.onGloballyPositioned {
                    howItWorksY = it.positionInRoot().y.toInt()
                },
        )
        EvidencePreviewSection(analytics = analytics)
        VerticalsSection(analytics = analytics)
        TrustSection(analytics = analytics)
        PricingSection(
            analytics = analytics,
            onGetStarted = { scrollTo(waitlistY) },
            modifier =
                Modifier.onGloballyPositioned {
                    pricingY = it.positionInRoot().y.toInt()
                },
        )
        WaitlistSection(
            analytics = analytics,
            onSubmit = onWaitlistSubmit,
            modifier =
                Modifier.onGloballyPositioned {
                    waitlistY = it.positionInRoot().y.toInt()
                },
        )
        FooterSection(
            analytics = analytics,
            onGetStartedClick = { scrollTo(waitlistY) },
            onInspectorsClick = { scrollTo(waitlistY) },
        )
    }
}
