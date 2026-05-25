/** CtaButton - reusable CTA with built-in analytics. Every CTA on the landing page uses this. */
package com.maku.eyeonsite.landing.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.White

/**
 * Reusable CTA button that fires analytics on every click.
 *
 * @param label Button text
 * @param ctaId Unique identifier for analytics (e.g. "hero_start", "pricing_enterprise")
 * @param section Section name for analytics context (e.g. "hero", "pricing")
 * @param analytics AnalyticsService instance injected from LandingPage
 * @param variant Visual style: PRIMARY (Forest fill), SECONDARY (SavannaGold), GHOST (outline)
 * @param onClick Action to perform on click
 */
@Composable
fun CtaButton(
    label: String,
    ctaId: String,
    section: String,
    analytics: AnalyticsService,
    modifier: Modifier = Modifier,
    variant: CtaVariant = CtaVariant.PRIMARY,
    compact: Boolean = false,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgColor by animateColorAsState(
        targetValue =
            when (variant) {
                CtaVariant.PRIMARY -> if (isHovered) Forest.copy(alpha = 0.85f) else Forest
                CtaVariant.SECONDARY -> if (isHovered) SavannaGold.copy(alpha = 0.85f) else SavannaGold
                CtaVariant.GHOST -> if (isHovered) White.copy(alpha = 0.1f) else Color.Transparent
            },
        animationSpec = tween(durationMillis = 150),
        label = "cta_bg",
    )

    val textColor =
        when (variant) {
            CtaVariant.PRIMARY -> White
            CtaVariant.SECONDARY -> DeepForest
            CtaVariant.GHOST -> White
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    analytics.logEvent(
                        "cta_clicked",
                        mapOf("cta_id" to ctaId, "section" to section),
                    )
                    onClick()
                }.semantics { role = Role.Button }
                .padding(horizontal = if (compact) 16.dp else 28.dp, vertical = if (compact) 10.dp else 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
        )
    }
}
