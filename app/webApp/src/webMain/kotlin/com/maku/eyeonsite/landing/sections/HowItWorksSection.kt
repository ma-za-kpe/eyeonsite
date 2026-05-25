package com.maku.eyeonsite.landing.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.White
import kotlinx.coroutines.delay

private data class Step(
    val number: String,
    val icon: String,
    val title: String,
    val description: String,
)

private val steps =
    listOf(
        Step(
            "01",
            "PIN",
            "Post your task",
            "Pin the location, tell us what to check, set your budget. Your money goes into escrow and stays safe until you're satisfied.",
        ),
        Step(
            "02",
            "GO",
            "We send an inspector",
            "A verified, ID-checked local inspector heads to your site within hours. GPS confirms they're there.",
        ),
        Step(
            "03",
            "SEE",
            "See the proof",
            "GPS-stamped photos, video, and checklist data captured in-app. Review and approve, or request changes. Your money only moves when you're happy.",
        ),
    )

@Composable
fun HowItWorksSection(
    analytics: AnalyticsService,
    modifier: Modifier = Modifier,
) {
    var visibleCount by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        analytics.logEvent("section_viewed", mapOf("section" to "how_it_works"))
        steps.indices.forEach { i ->
            delay((i * 150 + 100).toLong())
            visibleCount = i + 1
        }
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

        Column(
            modifier = Modifier.widthIn(max = 960.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("HOW IT WORKS", style = MaterialTheme.typography.labelMedium, color = SavannaGold, textAlign = TextAlign.Center)
                Text(
                    "Three steps to verified proof.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = White,
                    textAlign = TextAlign.Center,
                )
            }

            if (isMobile) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    steps.forEachIndexed { index, step ->
                        AnimatedVisibility(
                            visible = visibleCount > index,
                            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
                        ) {
                            StepCard(step, isMobile = true)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    steps.forEachIndexed { index, step ->
                        AnimatedVisibility(
                            visible = visibleCount > index,
                            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
                            modifier = Modifier.weight(1f),
                        ) {
                            StepCard(step)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    step: Step,
    isMobile: Boolean = false,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(White.copy(alpha = 0.05f))
                .border(1.dp, White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .padding(if (isMobile) 20.dp else 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(if (isMobile) 36.dp else 44.dp)
                        .clip(CircleShape)
                        .background(Forest),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    step.icon,
                    style = if (isMobile) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    color = White,
                )
            }
            Text(step.number, style = MaterialTheme.typography.labelLarge, color = SavannaGold)
        }
        Text(
            step.title,
            style = if (isMobile) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
            color = White,
        )
        Text(step.description, style = MaterialTheme.typography.bodyMedium, color = White.copy(alpha = 0.65f))
    }
}
