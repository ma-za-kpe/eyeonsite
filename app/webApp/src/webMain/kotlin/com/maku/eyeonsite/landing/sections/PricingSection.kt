package com.maku.eyeonsite.landing.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.landing.components.CtaButton
import com.maku.eyeonsite.landing.components.CtaVariant
import com.maku.eyeonsite.ui.theme.Ash
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.Ink
import com.maku.eyeonsite.ui.theme.Ivory
import com.maku.eyeonsite.ui.theme.Sand
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.White

private data class PricingTier(
    val name: String,
    val price: String,
    val description: String,
    val ctaId: String,
    val featured: Boolean = false,
)

private val tiers =
    listOf(
        PricingTier("Quick Check", "From \$20", "Photo evidence, GPS stamp, checklist, and inspector notes.", "pricing_quick_check"),
        PricingTier(
            "Construction",
            "From \$45",
            "Eight-photo progress package for build milestones and materials.",
            "pricing_construction",
            featured = true,
        ),
        PricingTier("Recurring", "Monthly", "Scheduled site visits and progress reports for remote owners.", "pricing_recurring"),
        PricingTier("Enterprise", "Custom", "Bulk verification workflows for lenders, insurers, and NGOs.", "pricing_enterprise"),
    )

@Composable
fun PricingSection(
    analytics: AnalyticsService,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        analytics.logEvent("section_viewed", mapOf("section" to "pricing"))
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Ivory)
                .padding(horizontal = 24.dp, vertical = 96.dp),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(modifier = Modifier.widthIn(max = 1040.dp)) {
            val isMobile = maxWidth < 700.dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(48.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("PRICING", style = MaterialTheme.typography.labelMedium, color = SavannaGold)
                    Text("Clear fees before anyone travels.", style = MaterialTheme.typography.headlineLarge, color = Ink)
                }

                if (isMobile) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        tiers.forEach { tier ->
                            PricingCard(tier = tier, analytics = analytics, onGetStarted = onGetStarted)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        tiers.forEach { tier ->
                            PricingCard(
                                tier = tier,
                                analytics = analytics,
                                onGetStarted = onGetStarted,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PricingCard(
    tier: PricingTier,
    analytics: AnalyticsService,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (tier.featured) SavannaGold else Sand
    val background = if (tier.featured) Forest else White
    val titleColor = if (tier.featured) White else Ink
    val bodyColor = if (tier.featured) White.copy(alpha = 0.76f) else Ash

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(background)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (tier.featured) "MOST POPULAR" else "PLAN",
            style = MaterialTheme.typography.labelSmall,
            color = if (tier.featured) SavannaGold else Forest,
        )
        Text(tier.name, style = MaterialTheme.typography.headlineSmall, color = titleColor)
        Text(tier.price, style = MaterialTheme.typography.headlineLarge, color = titleColor)
        Text(tier.description, style = MaterialTheme.typography.bodyMedium, color = bodyColor)
        CtaButton(
            label = "Join Waitlist",
            ctaId = tier.ctaId,
            section = "pricing",
            analytics = analytics,
            variant = if (tier.featured) CtaVariant.SECONDARY else CtaVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth(),
            onClick = onGetStarted,
        )
    }
}
