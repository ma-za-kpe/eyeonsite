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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import com.maku.eyeonsite.ui.theme.Ash
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.Ink
import com.maku.eyeonsite.ui.theme.Ivory
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

@Composable
fun EvidencePreviewSection(
    analytics: AnalyticsService,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        analytics.logEvent("section_viewed", mapOf("section" to "evidence_preview"))
        visible = true
    }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Ivory)
                .padding(horizontal = 24.dp, vertical = 80.dp),
        contentAlignment = Alignment.Center,
    ) {
        val isMobile = maxWidth < 700.dp

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
        ) {
            Column(
                modifier = Modifier.widthIn(max = 900.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isMobile) 16.dp else 24.dp),
            ) {
                Text("EVIDENCE", style = MaterialTheme.typography.labelMedium, color = SavannaGold, textAlign = TextAlign.Center)
                Text(
                    "This is what proof looks like.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Ink,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Not a blurry WhatsApp photo. GPS-verified evidence, captured in-app.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ash,
                    textAlign = TextAlign.Center,
                )
                EvidenceReportCard(isMobile = isMobile)
            }
        }
    }
}

@Composable
private fun EvidenceReportCard(isMobile: Boolean) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(White)
                .border(1.dp, Stone.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(if (isMobile) 20.dp else 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header
        if (isMobile) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text(
                        "Construction Progress Check",
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                    Text("Tema, Greater Accra · #01HXK9NBRZ", style = MaterialTheme.typography.bodySmall, color = Stone)
                }
                QualityBadge("0.87")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Construction Progress Check", style = MaterialTheme.typography.headlineSmall, color = Ink)
                    Text("Tema, Greater Accra · Task #01HXK9NBRZ", style = MaterialTheme.typography.bodySmall, color = Stone)
                }
                QualityBadge("0.87")
            }
        }

        // GPS row
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(SignalGreen))
            Text(
                "5.6037°N, 0.1870°W · 14:32:07 UTC · Accuracy ±12m",
                style = MaterialTheme.typography.bodySmall,
                color = Forest,
            )
        }

        // Photo grid: 4+4 on mobile, all 8 in one row on desktop
        PhotoGrid(isMobile = isMobile)

        ChecklistPreview(isMobile = isMobile)

        // Inspector notes
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Ivory)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Inspector notes", style = MaterialTheme.typography.labelMedium, color = Ash)
            Text(
                "Roof trusses installed. Iron sheets partially laid on the east wing. 3 workers on site. " +
                    "Materials: 20 bags cement, rebar, iron sheets. Construction quality acceptable. " +
                    "Walls straight and level.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
            )
        }
    }
}

@Composable
private fun QualityBadge(score: String) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(SignalGreen.copy(alpha = 0.12f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(score, style = MaterialTheme.typography.headlineSmall, color = SignalGreen)
            Text("Quality score", style = MaterialTheme.typography.labelSmall, color = SignalGreen.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun PhotoGrid(isMobile: Boolean) {
    val angles = listOf("FRONT", "BACK", "LEFT", "RIGHT", "INTERIOR", "MATERIALS", "DETAIL", "OVERVIEW")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Photos (8)", style = MaterialTheme.typography.labelMedium, color = Ash)
        if (isMobile) {
            // 2 rows of 4 on mobile
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    angles.take(4).forEach { PhotoTile(it, Modifier.weight(1f)) }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    angles.drop(4).forEach { PhotoTile(it, Modifier.weight(1f)) }
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                angles.forEach { PhotoTile(it, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PhotoTile(
    angle: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(64.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DeepForest.copy(alpha = 0.08f))
                .border(1.dp, Stone.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(angle, style = MaterialTheme.typography.labelSmall, color = Stone)
    }
}

@Suppress("UnusedParameter")
@Composable
private fun ChecklistPreview(isMobile: Boolean) {
    val items =
        listOf(
            "Workers on site" to "Yes · 3",
            "Stage" to "Roofing",
            "Materials visible" to "Yes",
            "Quality" to "Acceptable",
            "Completion" to "65%",
        )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Checklist", style = MaterialTheme.typography.labelMedium, color = Ash)
        items.forEach { (question, answer) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("✓  $question", style = MaterialTheme.typography.bodyMedium, color = Ink)
                Text(answer, style = MaterialTheme.typography.bodyMedium, color = Forest)
            }
        }
    }
}
