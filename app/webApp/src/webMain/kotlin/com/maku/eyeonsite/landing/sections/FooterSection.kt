package com.maku.eyeonsite.landing.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.APP_VERSION
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.landing.components.CtaButton
import com.maku.eyeonsite.landing.components.CtaVariant
import com.maku.eyeonsite.navigateTo
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

@Composable
fun FooterSection(
    analytics: AnalyticsService,
    onGetStartedClick: () -> Unit,
    onInspectorsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        analytics.logEvent("section_viewed", mapOf("section" to "footer"))
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color(0xFF011710))
                .padding(horizontal = 24.dp, vertical = 64.dp),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(modifier = Modifier.widthIn(max = 1040.dp).fillMaxWidth()) {
            val isMobile = maxWidth < 700.dp

            Column(verticalArrangement = Arrangement.spacedBy(48.dp)) {
                // Top row: logo + CTAs
                if (isMobile) {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        FooterBrand()
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CtaButton(
                                label = "Join the Waitlist",
                                ctaId = "footer_get_started",
                                section = "footer",
                                analytics = analytics,
                                variant = CtaVariant.PRIMARY,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onGetStartedClick,
                            )
                            CtaButton(
                                label = "Become an Inspector",
                                ctaId = "footer_inspectors",
                                section = "footer",
                                analytics = analytics,
                                variant = CtaVariant.GHOST,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onInspectorsClick,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FooterBrand()
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CtaButton(
                                label = "Join the Waitlist",
                                ctaId = "footer_get_started",
                                section = "footer",
                                analytics = analytics,
                                variant = CtaVariant.PRIMARY,
                                onClick = onGetStartedClick,
                            )
                            CtaButton(
                                label = "Become an Inspector",
                                ctaId = "footer_inspectors",
                                section = "footer",
                                analytics = analytics,
                                variant = CtaVariant.GHOST,
                                onClick = onInspectorsClick,
                            )
                        }
                    }
                }

                // Divider
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(White.copy(alpha = 0.08f)),
                )

                // Bottom row: legal links + copyright + version
                if (isMobile) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegalLinks(analytics)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "© 2025 Manti Labs LLC (Delaware) · makpalyy@gmail.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = Stone.copy(alpha = 0.5f),
                            )
                            VersionBadge(analytics)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "© 2025 Manti Labs LLC (Delaware) · makpalyy@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = Stone.copy(alpha = 0.5f),
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LegalLinks(analytics)
                            VersionBadge(analytics)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FooterBrand() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text("Eye", style = MaterialTheme.typography.headlineSmall, color = Forest)
            Text("On", style = MaterialTheme.typography.headlineSmall, color = White)
            Text("Site", style = MaterialTheme.typography.headlineSmall, color = Forest)
        }
        Text("Trust, verified.", style = MaterialTheme.typography.bodyMedium, color = SavannaGold)
        Text(
            "Built by Manti Labs for the African diaspora.",
            style = MaterialTheme.typography.bodySmall,
            color = Stone,
        )
    }
}

@Composable
private fun LegalLinks(analytics: AnalyticsService) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        LegalLink("Privacy Policy", analytics)
        LegalLink("Terms of Service", analytics)
        LegalLink("Contact", analytics)
    }
}

@Composable
private fun VersionBadge(analytics: AnalyticsService) {
    Text(
        text = "v$APP_VERSION",
        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
        color = SavannaGold.copy(alpha = 0.7f),
        modifier =
            Modifier.clickable {
                analytics.logEvent("version_badge_clicked", mapOf("version" to APP_VERSION))
                navigateTo("https://github.com/ma-za-kpe/eyeonsite/blob/main/CHANGELOG.md")
            },
    )
}

@Composable
private fun LegalLink(
    label: String,
    analytics: AnalyticsService,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
        color = SavannaGold,
        modifier =
            Modifier.clickable {
                analytics.logEvent("legal_link_clicked", mapOf("label" to label))
                when (label) {
                    "Privacy Policy" -> navigateTo("/privacy")
                    "Terms of Service" -> navigateTo("/terms")
                    "Contact" -> navigateTo("mailto:makpalyy@gmail.com")
                }
            },
    )
}
