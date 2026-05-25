package com.maku.eyeonsite.landing.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.landing.components.CtaButton
import com.maku.eyeonsite.landing.components.CtaVariant
import com.maku.eyeonsite.landing.components.TrustBadge
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun HeroSection(
    analytics: AnalyticsService,
    onGetStarted: () -> Unit,
    onForInspectors: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .background(DeepForest)
                .padding(horizontal = 24.dp, vertical = 80.dp),
        contentAlignment = Alignment.Center,
    ) {
        val isMobile = maxWidth < 700.dp

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
        ) {
            Column(
                modifier = Modifier.widthIn(max = 800.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "ON-DEMAND VERIFICATION FOR AFRICA",
                    style = MaterialTheme.typography.labelMedium,
                    color = SavannaGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp),
                )

                Text(
                    text =
                        buildAnnotatedString {
                            append("See your house\n")
                            withStyle(SpanStyle(color = SavannaGold)) {
                                append("before you fly home.")
                            }
                        },
                    style = if (isMobile) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayLarge,
                    color = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                )

                Text(
                    text =
                        "On-demand, GPS-verified inspections of your construction, property, or farm " +
                            "in Africa. Your money stays in escrow until you're satisfied.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Stone,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .widthIn(max = 600.dp)
                            .padding(bottom = 40.dp),
                )

                if (isMobile) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 48.dp),
                    ) {
                        CtaButton(
                            label = "Join the Waitlist",
                            ctaId = "hero_start",
                            section = "hero",
                            analytics = analytics,
                            variant = CtaVariant.PRIMARY,
                            onClick = onGetStarted,
                        )
                        CtaButton(
                            label = "Become an Inspector",
                            ctaId = "hero_inspectors",
                            section = "hero",
                            analytics = analytics,
                            variant = CtaVariant.GHOST,
                            onClick = onForInspectors,
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 48.dp),
                    ) {
                        CtaButton(
                            label = "Join the Waitlist",
                            ctaId = "hero_start",
                            section = "hero",
                            analytics = analytics,
                            variant = CtaVariant.PRIMARY,
                            onClick = onGetStarted,
                        )
                        CtaButton(
                            label = "Become an Inspector",
                            ctaId = "hero_inspectors",
                            section = "hero",
                            analytics = analytics,
                            variant = CtaVariant.GHOST,
                            onClick = onForInspectors,
                        )
                    }
                }

                if (isMobile) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        ) {
                            TrustBadge("GPS-verified")
                            TrustBadge("Escrow-backed")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        ) {
                            TrustBadge("48h turnaround")
                            TrustBadge("From \$20")
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TrustBadge("GPS-verified")
                        TrustBadge("Escrow-protected")
                        TrustBadge("48-hour turnaround")
                        TrustBadge("From \$20")
                    }
                }
            }
        }
    }
}
