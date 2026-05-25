package com.maku.eyeonsite.landing.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SignalGreen
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

@Composable
fun NavBar(
    analytics: AnalyticsService,
    onHowItWorksClick: () -> Unit,
    onPricingClick: () -> Unit,
    onInspectorsClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .background(DeepForest.copy(alpha = 0.97f))
                .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        val isMobile = maxWidth < 700.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Logo lockup
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoMark(size = if (isMobile) 24 else 32)
                Spacer(Modifier.width(8.dp))
                val logoStyle = if (isMobile) MaterialTheme.typography.titleSmall else MaterialTheme.typography.headlineSmall
                Row {
                    Text("Eye", style = logoStyle, color = SignalGreen)
                    Text("On", style = logoStyle, color = White)
                    Text("Site", style = logoStyle, color = SignalGreen)
                }
            }

            if (isMobile) {
                // Mobile: only the primary CTA, compact size
                CtaButton(
                    label = "Join Waitlist",
                    ctaId = "nav_get_started_mobile",
                    section = "nav",
                    analytics = analytics,
                    variant = CtaVariant.PRIMARY,
                    compact = true,
                    onClick = onGetStartedClick,
                )
            } else {
                // Desktop: full nav links + CTA
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavLink("How it works", analytics, onClick = onHowItWorksClick)
                    NavLink("Pricing", analytics, onClick = onPricingClick)
                    NavLink("For Inspectors", analytics, onClick = onInspectorsClick)
                    Spacer(Modifier.width(8.dp))
                    CtaButton(
                        label = "Join Waitlist",
                        ctaId = "nav_get_started",
                        section = "nav",
                        analytics = analytics,
                        variant = CtaVariant.PRIMARY,
                        onClick = onGetStartedClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun NavLink(
    label: String,
    analytics: AnalyticsService,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val color by animateColorAsState(
        targetValue = if (isHovered) White else Stone,
        animationSpec = tween(150),
        label = "nav_link_color",
    )
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier =
            Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                analytics.logEvent("nav_link_clicked", mapOf("label" to label))
                onClick()
            },
    )
}

/** EyeOnSite pin-drop logo mark drawn via Compose Canvas — no bitmap required. */
@Composable
fun LogoMark(size: Int = 40) {
    Canvas(modifier = Modifier.size(size.dp)) {
        drawLogoMark(this)
    }
}

fun drawLogoMark(scope: DrawScope) {
    val h = scope.size.height
    val sc = h / 160f
    val ox = (scope.size.width - 120f * sc) / 2f

    fun fx(v: Float) = ox + v * sc

    fun fy(v: Float) = v * sc

    // Forest pin-drop silhouette
    val pin =
        Path().apply {
            moveTo(fx(60f), fy(4f))
            cubicTo(fx(91f), fy(4f), fx(116f), fy(29f), fx(116f), fy(60f))
            cubicTo(fx(116f), fy(91f), fx(60f), fy(152f), fx(60f), fy(152f))
            cubicTo(fx(60f), fy(152f), fx(4f), fy(91f), fx(4f), fy(60f))
            cubicTo(fx(4f), fy(29f), fx(29f), fy(4f), fx(60f), fy(4f))
            close()
        }
    scope.drawPath(pin, Forest)

    // White eye lens (wave)
    val lens =
        Path().apply {
            moveTo(fx(26f), fy(62f))
            quadraticTo(fx(60f), fy(32f), fx(94f), fy(62f))
            quadraticTo(fx(60f), fy(92f), fx(26f), fy(62f))
            close()
        }
    scope.drawPath(lens, White.copy(alpha = 0.95f))

    // Iris (Forest ring)
    scope.drawCircle(Forest, radius = 16f * sc, center = Offset(fx(60f), fy(62f)))

    // Pupil (DeepForest)
    scope.drawCircle(DeepForest, radius = 8f * sc, center = Offset(fx(60f), fy(62f)))

    // Catchlight highlight
    scope.drawCircle(White.copy(alpha = 0.85f), radius = 4f * sc, center = Offset(fx(54f), fy(57f)))
}
