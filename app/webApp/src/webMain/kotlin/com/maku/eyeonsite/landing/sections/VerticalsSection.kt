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
import com.maku.eyeonsite.ui.theme.Ash
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.Ink
import com.maku.eyeonsite.ui.theme.Ivory
import com.maku.eyeonsite.ui.theme.Sand
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.White

private data class VerticalCard(
    val title: String,
    val description: String,
    val signal: String,
)

private val verticals =
    listOf(
        VerticalCard("Construction", "Progress checks, material verification, site activity, and milestone evidence.", "Launch vertical"),
        VerticalCard("Property", "Land, rental, and home condition checks before buyers or tenants commit money.", "V1 ready"),
        VerticalCard("Agriculture", "Farm visits, input checks, crop progress, and seasonal reporting for remote owners.", "V1 pilot"),
        VerticalCard("Enterprise", "Field verification API for lenders, insurers, NGOs, and distributed operations teams.", "Pipeline"),
    )

@Composable
fun VerticalsSection(
    analytics: AnalyticsService,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        analytics.logEvent("section_viewed", mapOf("section" to "verticals"))
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

        Column(
            modifier = Modifier.widthIn(max = 1040.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("VERTICALS", style = MaterialTheme.typography.labelMedium, color = SavannaGold, textAlign = TextAlign.Center)
                Text(
                    "Start with construction. Expand where trust breaks.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Ink,
                    textAlign = TextAlign.Center,
                )
            }

            if (isMobile) {
                // 2-column grid on mobile
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    verticals.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            pair.forEach { vertical ->
                                VerticalTile(vertical, isMobile = true, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    verticals.forEach { vertical ->
                        VerticalTile(vertical, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalTile(
    vertical: VerticalCard,
    isMobile: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(White)
                .border(1.dp, Sand, RoundedCornerShape(8.dp))
                .padding(if (isMobile) 14.dp else 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(vertical.signal.uppercase(), style = MaterialTheme.typography.labelSmall, color = Forest)
        Text(
            vertical.title,
            style = if (isMobile) MaterialTheme.typography.titleSmall else MaterialTheme.typography.headlineSmall,
            color = Ink,
        )
        Text(
            vertical.description,
            style = if (isMobile) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = Ash,
        )
    }
}
