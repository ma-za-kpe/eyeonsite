package com.maku.eyeonsite.landing.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.maku.eyeonsite.navigateTo
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

// Replace these with the group invite links from:
// Firebase Console → App Distribution → Testers & Groups → internal → Copy invite link
private const val CLIENT_INVITE_URL =
    "https://appdistribution.firebase.google.com/testerapps/1:922315268903:android:9a262a3db6364511fb7f02/groups/internal"
private const val INSPECTOR_INVITE_URL =
    "https://appdistribution.firebase.google.com/testerapps/1:922315268903:android:83ace1bab8aeac06fb7f02/groups/internal"

@Composable
fun GetTheAppSection(
    analytics: AnalyticsService,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        analytics.logEvent("section_viewed", mapOf("section" to "get_the_app"))
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(DeepForest)
                .padding(horizontal = 24.dp, vertical = 64.dp),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            val isMobile = maxWidth < 600.dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "CLOSED BETA · ANDROID",
                        style = MaterialTheme.typography.labelMedium,
                        color = SavannaGold,
                    )
                    Text(
                        text = "Beta testing is open.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = White,
                    )
                    Text(
                        text =
                            "We're running a private Android beta in Accra. " +
                                "Install the build that matches your role — real flows, real feedback welcome.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Stone,
                    )
                }

                if (isMobile) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AppDownloadButton(
                            label = "Diaspora Client",
                            sublabel = "Track & verify your property",
                            url = CLIENT_INVITE_URL,
                            analytics = analytics,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        AppDownloadButton(
                            label = "Field Inspector",
                            sublabel = "Accept jobs, submit evidence",
                            url = INSPECTOR_INVITE_URL,
                            analytics = analytics,
                            modifier = Modifier.fillMaxWidth(),
                            ghost = true,
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        AppDownloadButton(
                            label = "Diaspora Client",
                            sublabel = "Track & verify your property",
                            url = CLIENT_INVITE_URL,
                            analytics = analytics,
                        )
                        AppDownloadButton(
                            label = "Field Inspector",
                            sublabel = "Accept jobs, submit evidence",
                            url = INSPECTOR_INVITE_URL,
                            analytics = analytics,
                            ghost = true,
                        )
                    }
                }

                Text(
                    text =
                        "iOS coming soon. " +
                            "Beta builds are distributed via Firebase — you'll receive an install link after tapping.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Stone.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun AppDownloadButton(
    label: String,
    sublabel: String,
    url: String,
    analytics: AnalyticsService,
    modifier: Modifier = Modifier,
    ghost: Boolean = false,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(if (ghost) White.copy(alpha = 0.04f) else Forest)
                .border(1.5.dp, if (ghost) White.copy(alpha = 0.2f) else Forest, shape)
                .clickable {
                    analytics.logEvent("app_download_click", mapOf("variant" to label))
                    navigateTo(url)
                }.padding(horizontal = 28.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Download — $label",
                style = MaterialTheme.typography.labelLarge,
                color = White,
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.bodySmall,
                color = if (ghost) Stone else White.copy(alpha = 0.75f),
            )
        }
    }
}
