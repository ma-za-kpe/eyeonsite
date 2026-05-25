package com.maku.eyeonsite.admin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.fetchWaitlist
import com.maku.eyeonsite.signInAdmin
import com.maku.eyeonsite.ui.theme.Ash
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

private sealed class AdminState {
    data object Login : AdminState()

    data object SigningIn : AdminState()

    data class Err(
        val msg: String,
    ) : AdminState()

    data class Dashboard(
        val token: String,
        val waitlist: List<WaitlistDocument>,
        val loading: Boolean,
        val fetchError: String? = null,
    ) : AdminState()
}

@Composable
fun AdminPage() {
    val scrollState = rememberScrollState()
    var state by remember { mutableStateOf<AdminState>(AdminState.Login) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(DeepForest)
                .verticalScroll(scrollState),
    ) {
        // Header bar
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF011710))
                    .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row {
                Text("Eye", style = MaterialTheme.typography.titleLarge, color = SignalGreen)
                Text("On", style = MaterialTheme.typography.titleLarge, color = White)
                Text("Site", style = MaterialTheme.typography.titleLarge, color = SignalGreen)
                Text(" · Admin", style = MaterialTheme.typography.titleLarge, color = SavannaGold)
            }
            if (state is AdminState.Dashboard) {
                Text(
                    "Sign out",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Stone,
                    modifier =
                        Modifier.clickable {
                            state = AdminState.Login
                            email = ""
                            password = ""
                        },
                )
            }
        }

        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "admin_content",
        ) { s ->
            when (s) {
                is AdminState.Login, is AdminState.Err ->
                    LoginPanel(
                        email = email,
                        password = password,
                        error = (s as? AdminState.Err)?.msg,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onSignIn = {
                            state = AdminState.SigningIn
                            signInAdmin(
                                email = email.trim(),
                                password = password,
                                onSuccess = { token ->
                                    state = AdminState.Dashboard(token, emptyList(), true)
                                    fetchWaitlist(
                                        idToken = token,
                                        onSuccess = { docs ->
                                            state = AdminState.Dashboard(token, docs, false)
                                        },
                                        onError = { err ->
                                            state = AdminState.Dashboard(token, emptyList(), false, err)
                                        },
                                    )
                                },
                                onError = { err -> state = AdminState.Err(err) },
                            )
                        },
                    )
                is AdminState.SigningIn ->
                    Box(
                        Modifier.fillMaxWidth().height(500.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            CircularProgressIndicator(color = SignalGreen, modifier = Modifier.size(40.dp))
                            Text("Signing in…", style = MaterialTheme.typography.bodyLarge, color = Stone)
                        }
                    }
                is AdminState.Dashboard -> DashboardPanel(s)
            }
        }
    }
}

// ── Login ─────────────────────────────────────────────────────────────────────

@Composable
private fun LoginPanel(
    email: String,
    password: String,
    error: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(600.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Admin Access", style = MaterialTheme.typography.headlineLarge, color = White)
            Text(
                "EyeOnSite internal dashboard",
                style = MaterialTheme.typography.bodyMedium,
                color = Stone,
            )
            Spacer(Modifier.height(8.dp))

            if (error != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8593C).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFE8593C).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                ) {
                    Text(error, style = MaterialTheme.typography.bodySmall, color = Color(0xFFE8593C))
                }
            }

            AdminField("Email", email, "makpalyy@gmail.com", onEmailChange)
            AdminField("Password", password, "••••••••", onPasswordChange, isPassword = true)

            val canSubmit = email.contains("@") && password.isNotBlank()
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (canSubmit) Forest else Forest.copy(alpha = 0.4f))
                        .clickable(enabled = canSubmit, onClick = onSignIn)
                        .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Sign in", style = MaterialTheme.typography.labelLarge, color = White)
            }
        }
    }
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

@Composable
private fun DashboardPanel(state: AdminState.Dashboard) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        // Stats
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Total signups", state.waitlist.size.toString())
            StatCard(
                "Diaspora clients",
                state.waitlist.count { it.role == "diaspora_client" }.toString(),
            )
            StatCard(
                "Field agents",
                state.waitlist.count { it.role == "field_agent" }.toString(),
            )
        }

        // Waitlist section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Waitlist", style = MaterialTheme.typography.headlineSmall, color = White)

            when {
                state.loading ->
                    Box(
                        Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = SignalGreen)
                    }
                state.fetchError != null ->
                    Text(
                        state.fetchError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE8593C),
                    )
                state.waitlist.isEmpty() ->
                    Text(
                        "No signups yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Stone,
                    )
                else -> {
                    WaitlistHeader()
                    state.waitlist.forEach { doc -> WaitlistRow(doc) }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
) {
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(White.copy(alpha = 0.07f))
                .border(1.dp, White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(value, style = MaterialTheme.typography.headlineLarge, color = SignalGreen)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Stone)
    }
}

@Composable
private fun WaitlistHeader() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(White.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Name", style = MaterialTheme.typography.labelMedium, color = SavannaGold, modifier = Modifier.weight(1.2f))
        Text("Email", style = MaterialTheme.typography.labelMedium, color = SavannaGold, modifier = Modifier.weight(1.8f))
        Text("Role", style = MaterialTheme.typography.labelMedium, color = SavannaGold, modifier = Modifier.weight(0.8f))
        Text("Location", style = MaterialTheme.typography.labelMedium, color = SavannaGold, modifier = Modifier.weight(1f))
        Text("Message", style = MaterialTheme.typography.labelMedium, color = SavannaGold, modifier = Modifier.weight(2f))
        Text("Date", style = MaterialTheme.typography.labelMedium, color = SavannaGold, modifier = Modifier.weight(0.8f))
    }
}

@Composable
private fun WaitlistRow(doc: WaitlistDocument) {
    val roleLabel = if (doc.role == "diaspora_client") "Client" else "Inspector"
    val roleColor = if (doc.role == "diaspora_client") SavannaGold else SignalGreen
    val date = doc.createdAt.take(10).ifBlank { "—" }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(White.copy(alpha = 0.04f))
                .border(1.dp, White.copy(alpha = 0.07f), RoundedCornerShape(6.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(doc.name, style = MaterialTheme.typography.bodyMedium, color = White, modifier = Modifier.weight(1.2f))
        Text(doc.email, style = MaterialTheme.typography.bodySmall, color = Stone, modifier = Modifier.weight(1.8f))
        Box(
            modifier =
                Modifier
                    .weight(0.8f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(roleColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
        ) {
            Text(roleLabel, style = MaterialTheme.typography.labelSmall, color = roleColor)
        }
        Text(doc.location, style = MaterialTheme.typography.bodySmall, color = Ash, modifier = Modifier.weight(1f))
        Text(
            doc.message.ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall,
            color = Stone,
            modifier = Modifier.weight(2f),
        )
        Text(date, style = MaterialTheme.typography.bodySmall, color = Stone, modifier = Modifier.weight(0.8f))
    }
}

// ── Field input ───────────────────────────────────────────────────────────────

@Composable
private fun AdminField(
    label: String,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Stone)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = White),
            cursorBrush = SolidColor(SavannaGold),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            decorationBox = { inner ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(White.copy(alpha = 0.07f))
                            .border(1.dp, White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp),
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
