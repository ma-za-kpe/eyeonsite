package com.maku.eyeonsite.payment

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import kotlin.math.abs
import kotlin.math.round

private fun Double.formatUsd(): String {
    val cents = round(this * 100).toLong()
    val dollars = cents / 100
    val remaining = abs(cents % 100)
    return "$dollars.${remaining.toString().padStart(2, '0')}"
}

@Composable
fun FundTaskScreen(
    vm: FundTaskViewModel,
    taskId: String,
    bountyUsd: Double,
    onPaymentReady: (clientSecret: String) -> Unit,
    onFunded: () -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state) {
        when (val s = state) {
            is FundTaskUiState.ReadyToPay -> onPaymentReady(s.clientSecret)
            is FundTaskUiState.Funded -> onFunded()
            else -> Unit
        }
    }

    EyeOnSiteTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(DeepForest),
        ) {
            Text(
                text = "←",
                fontSize = 24.sp,
                color = Color.White,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .clickable { onBack() },
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when (val s = state) {
                    is FundTaskUiState.Idle ->
                        EscrowIdleContent(
                            bountyUsd = bountyUsd,
                            onPay = { vm.startPayment(taskId) },
                        )
                    is FundTaskUiState.CreatingIntent -> CreatingIntentContent()
                    is FundTaskUiState.ReadyToPay -> CreatingIntentContent()
                    is FundTaskUiState.Funded -> EscrowFundedContent(onContinue = onFunded)
                    is FundTaskUiState.Error ->
                        EscrowErrorContent(
                            message = s.message,
                            onRetry = { vm.retry() },
                        )
                }
            }
        }
    }
}

@Composable
private fun EscrowIdleContent(
    bountyUsd: Double,
    onPay: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        BountyBadge(bountyUsd = bountyUsd)
        EscrowInfoCard()
        Button(
            onClick = onPay,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SavannaGold),
        ) {
            Text(
                text = "Pay $${bountyUsd.formatUsd()} · Hold in Escrow",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepForest,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        Text(
            text = "Secured by Stripe. Funds released to the inspector only after you approve their evidence.",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BountyBadge(bountyUsd: Double) {
    Box(
        modifier =
            Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Forest),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$${bountyUsd.toLong()}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = SavannaGold,
            )
            Text(
                text = "USD",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun EscrowInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Forest.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EscrowInfoRow(icon = "🔒", label = "Funds held securely until evidence accepted")
            EscrowInfoRow(icon = "📷", label = "Inspector paid after you approve GPS evidence")
            EscrowInfoRow(icon = "🔄", label = "Full refund if no match found within 48 h")
        }
    }
}

@Composable
private fun EscrowInfoRow(
    icon: String,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Text(text = label, fontSize = 14.sp, color = Color.White)
    }
}

@Composable
private fun CreatingIntentContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator(color = SavannaGold)
        Text(
            text = "Preparing secure payment…",
            fontSize = 16.sp,
            color = Color.White,
        )
    }
}

@Composable
private fun EscrowFundedContent(onContinue: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(text = "✅", fontSize = 64.sp)
        Text(
            text = "Escrow Funded",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SavannaGold,
        )
        Text(
            text = "Funds are held securely. We'll match you with an inspector and notify you within 24 hours.",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Forest),
        ) {
            Text(
                text = "Back to Dashboard",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun EscrowErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "⚠️", fontSize = 48.sp)
        Text(
            text = "Payment failed",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = SavannaGold,
        )
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Forest),
        ) {
            Text(
                text = "Try Again",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun FundTaskIdlePreview() {
    EyeOnSiteTheme {
        EscrowIdleContent(bountyUsd = 45.0, onPay = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun FundTaskFundedPreview() {
    EyeOnSiteTheme {
        EscrowFundedContent(onContinue = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun FundTaskErrorPreview() {
    EyeOnSiteTheme {
        EscrowErrorContent(
            message = "Your card was declined. Please use a different payment method.",
            onRetry = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun FundTaskLoadingPreview() {
    EyeOnSiteTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(DeepForest)
                    .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            FundTaskScreen(
                vm = FundTaskViewModel(NoOpPaymentRepository(), NoOpAnalyticsService),
                taskId = "task-preview",
                bountyUsd = 60.0,
                onPaymentReady = {},
                onFunded = {},
                onBack = {},
            )
        }
    }
}
