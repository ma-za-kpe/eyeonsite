package com.maku.eyeonsite.landing.sections

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.landing.waitlist.WaitlistEntry
import com.maku.eyeonsite.landing.waitlist.WaitlistRole
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

private sealed class WaitlistState {
    data object Idle : WaitlistState()

    data object Submitting : WaitlistState()

    data class Success(
        val role: WaitlistRole,
    ) : WaitlistState()

    data class Error(
        val message: String,
    ) : WaitlistState()
}

@Composable
fun WaitlistSection(
    analytics: AnalyticsService,
    onSubmit: (WaitlistEntry, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        analytics.logEvent("section_viewed", mapOf("section" to "waitlist"))
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(WaitlistRole.DIASPORA_CLIENT) }
    var location by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<WaitlistState>(WaitlistState.Idle) }

    val locationHint = if (role == WaitlistRole.DIASPORA_CLIENT) "e.g. London, UK" else "e.g. Accra, Ghana"

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(DeepForest)
                .padding(horizontal = 24.dp, vertical = 96.dp),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            val isMobile = maxWidth < 600.dp

            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "waitlist_state",
            ) { currentState ->
                when (currentState) {
                    is WaitlistState.Success -> WaitlistSuccess(currentState.role)

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(32.dp),
                        ) {
                            // Header
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "EARLY ACCESS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SavannaGold,
                                )
                                Text(
                                    text = "Be first in line.",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = White,
                                )
                                Text(
                                    text =
                                        "EyeOnSite launches in Accra first. Join the waitlist — " +
                                            "diaspora families and field inspectors both welcome.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Stone,
                                )
                            }

                            // Role selector
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "I am...",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Stone,
                                )
                                if (isMobile) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        WaitlistRole.entries.forEach { r ->
                                            RoleCard(r, selected = role == r) { role = r }
                                        }
                                    }
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        WaitlistRole.entries.forEach { r ->
                                            RoleCard(r, selected = role == r, modifier = Modifier.weight(1f)) { role = r }
                                        }
                                    }
                                }
                            }

                            // Form fields
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                if (isMobile) {
                                    WaitlistField("Full name", name, "e.g. Kwame Mensah") { name = it }
                                    WaitlistField("Email address", email, "e.g. kwame@example.com") { email = it }
                                    WaitlistField("Your city / country", location, locationHint) { location = it }
                                    WaitlistField("Anything else? (optional)", message, "Tell us your situation…", singleLine = false) {
                                        message =
                                            it
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        WaitlistField("Full name", name, "e.g. Kwame Mensah", modifier = Modifier.weight(1f)) { name = it }
                                        WaitlistField("Email address", email, "e.g. kwame@example.com", modifier = Modifier.weight(1f)) {
                                            email =
                                                it
                                        }
                                    }
                                    WaitlistField("Your city / country", location, locationHint) { location = it }
                                    WaitlistField("Anything else? (optional)", message, "Tell us your situation…", singleLine = false) {
                                        message =
                                            it
                                    }
                                }
                            }

                            // Error
                            if (state is WaitlistState.Error) {
                                Text(
                                    text = (state as WaitlistState.Error).message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color =
                                        androidx.compose.ui.graphics
                                            .Color(0xFFE8593C),
                                )
                            }

                            // Submit
                            val isValid = name.isNotBlank() && email.contains("@") && location.isNotBlank()
                            val isSubmitting = state is WaitlistState.Submitting

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isValid && !isSubmitting) Forest else Forest.copy(alpha = 0.4f))
                                        .clickable(enabled = isValid && !isSubmitting) {
                                            state = WaitlistState.Submitting
                                            analytics.logEvent(
                                                "waitlist_submit",
                                                mapOf("role" to role.apiValue, "location" to location),
                                            )
                                            onSubmit(
                                                WaitlistEntry(name.trim(), email.trim(), role, location.trim(), message.trim()),
                                                {
                                                    analytics.logEvent("waitlist_success", mapOf("role" to role.apiValue))
                                                    state = WaitlistState.Success(role)
                                                },
                                                { err ->
                                                    analytics.logEvent("waitlist_error", mapOf("error" to err))
                                                    state = WaitlistState.Error(err)
                                                },
                                            )
                                        }.padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text(
                                        text = "Request Early Access",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = White,
                                    )
                                }
                            }

                            Text(
                                text = "No spam. No commitment. We'll reach out when your city goes live.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Stone,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    role: WaitlistRole,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) SavannaGold else White.copy(alpha = 0.15f)
    val bg = if (selected) SavannaGold.copy(alpha = 0.08f) else White.copy(alpha = 0.03f)

    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Radio dot
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (selected) SavannaGold else Stone, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SavannaGold),
                )
            }
        }
        Text(
            text = role.label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) White else Stone,
        )
    }
}

@Composable
private fun WaitlistField(
    label: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Stone,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = White),
            cursorBrush = SolidColor(SavannaGold),
            visualTransformation = VisualTransformation.None,
            decorationBox = { inner ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(White.copy(alpha = 0.07f))
                            .border(1.dp, White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 14.dp, vertical = if (singleLine) 14.dp else 12.dp),
                ) {
                    if (value.isEmpty()) {
                        Text(hint, style = MaterialTheme.typography.bodyMedium, color = Stone.copy(alpha = 0.6f))
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun WaitlistSuccess(role: WaitlistRole) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier =
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SignalGreen.copy(alpha = 0.15f))
                    .border(2.dp, SignalGreen, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", style = MaterialTheme.typography.headlineLarge, color = SignalGreen)
        }
        Text(
            text = "You're on the list.",
            style = MaterialTheme.typography.headlineLarge,
            color = White,
        )
        Text(
            text =
                if (role == WaitlistRole.DIASPORA_CLIENT) {
                    "We'll reach out as soon as EyeOnSite launches — usually within the next few weeks."
                } else {
                    "We'll be in touch to start your inspector onboarding. Welcome to the team."
                },
            style = MaterialTheme.typography.bodyLarge,
            color = Stone,
        )
    }
}
