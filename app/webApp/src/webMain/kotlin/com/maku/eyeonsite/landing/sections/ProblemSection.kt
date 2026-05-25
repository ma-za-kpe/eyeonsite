package com.maku.eyeonsite.landing.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.ui.theme.Ash
import com.maku.eyeonsite.ui.theme.Ink
import com.maku.eyeonsite.ui.theme.Ivory
import com.maku.eyeonsite.ui.theme.SavannaGold

@Composable
fun ProblemSection(
    analytics: AnalyticsService,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        analytics.logEvent("section_viewed", mapOf("section" to "problem"))
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
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 },
        ) {
            Column(
                modifier = Modifier.widthIn(max = 720.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "PROBLEM",
                    style = MaterialTheme.typography.labelMedium,
                    color = SavannaGold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "You sent the money.\nNow what?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    StoryParagraph(
                        "You're in London, Houston, or Toronto. You wired GBP 40,000 for a house in Accra. " +
                            "Your brother hired a contractor. The WhatsApp photos came monthly at first.",
                    )
                    StoryParagraph(
                        "By month four, the updates stop. \"Materials are expensive.\" \"Rain delayed us.\" " +
                            "\"Don't worry, it's coming.\"",
                    )
                    StoryParagraph(
                        "This isn't an edge case. This is the default experience for diaspora families " +
                            "building back home. And it costs millions every year.",
                    )
                }
                Spacer(Modifier.height(24.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "\$100B",
                        style = if (isMobile) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayLarge,
                        color = SavannaGold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Africa's diaspora sends \$100 billion home every year.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Most of it builds on faith, not facts.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ash,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryParagraph(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = Ash)
}
