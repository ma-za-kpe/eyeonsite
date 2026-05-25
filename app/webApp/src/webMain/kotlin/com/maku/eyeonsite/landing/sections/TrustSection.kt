package com.maku.eyeonsite.landing.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

private data class TrustPillar(
    val title: String,
    val body: String,
)

private val pillars =
    listOf(
        TrustPillar("Escrow-backed", "Funds stay locked until evidence is accepted or a dispute is resolved."),
        TrustPillar("Inspector-verified", "Field agents are screened, trained, scored, and monitored task by task."),
        TrustPillar("GPS-stamped", "Evidence is captured in-app with location, time, and device signals attached."),
    )

@Composable
fun TrustSection(
    analytics: AnalyticsService,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        analytics.logEvent("section_viewed", mapOf("section" to "trust"))
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
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("TRUST AND SAFETY", style = MaterialTheme.typography.labelMedium, color = SavannaGold, textAlign = TextAlign.Center)
                Text("Proof beats promises.", style = MaterialTheme.typography.headlineLarge, color = White, textAlign = TextAlign.Center)
            }

            if (isMobile) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    pillars.forEach { TrustPillarCard(it) }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    pillars.forEach { TrustPillarCard(it, modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun TrustPillarCard(
    pillar: TrustPillar,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(White.copy(alpha = 0.05f))
                .border(1.dp, White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("VERIFIED", style = MaterialTheme.typography.labelSmall, color = SignalGreen)
        Text(pillar.title, style = MaterialTheme.typography.headlineSmall, color = White)
        Text(pillar.body, style = MaterialTheme.typography.bodyMedium, color = Stone)
    }
}
