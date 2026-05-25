package com.maku.eyeonsite.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White
import kotlinx.coroutines.delay

private const val OTP_LENGTH = 6
private const val RESEND_COUNTDOWN_SECONDS = 60

@Composable
fun OtpScreen(
    phone: String,
    isVerifying: Boolean = false,
    errorMessage: String? = null,
    onVerify: (code: String) -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
) {
    var otp by remember { mutableStateOf("") }
    var secondsLeft by remember { mutableIntStateOf(RESEND_COUNTDOWN_SECONDS) }
    val canResend = secondsLeft == 0

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1_000L)
            secondsLeft--
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(DeepForest)
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(72.dp))

        LogoMark()

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Check your messages",
            style = MaterialTheme.typography.headlineLarge,
            color = White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "We sent a 6-digit code to\n$phone",
            style = MaterialTheme.typography.bodyLarge,
            color = Stone,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))

        OtpInputRow(
            otp = otp,
            onOtpChange = { value ->
                if (value.length <= OTP_LENGTH && value.all { it.isDigit() }) otp = value
            },
        )

        Spacer(Modifier.height(8.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = SavannaGold,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onVerify(otp) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = otp.length == OTP_LENGTH && !isVerifying,
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Forest,
                    contentColor = White,
                    disabledContainerColor = Forest.copy(alpha = 0.4f),
                    disabledContentColor = White.copy(alpha = 0.5f),
                ),
        ) {
            Text(
                text = if (isVerifying) "Verifying…" else "Verify",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(16.dp))

        if (canResend) {
            TextButton(onClick = {
                otp = ""
                secondsLeft = RESEND_COUNTDOWN_SECONDS
                onResend()
            }) {
                Text("Resend code", color = SavannaGold, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Text(
                text = "Resend in ${secondsLeft}s",
                style = MaterialTheme.typography.bodyMedium,
                color = Stone,
            )
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("← Change number", color = Stone, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun OtpInputRow(
    otp: String,
    onOtpChange: (String) -> Unit,
) {
    BasicTextField(
        value = otp,
        onValueChange = onOtpChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(OTP_LENGTH) { index ->
                    OtpDigitBox(digit = otp.getOrNull(index)?.toString() ?: "", isFocused = index == otp.length)
                }
            }
        },
    )
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
) {
    Box(
        modifier =
            Modifier
                .size(48.dp)
                .background(
                    color = if (digit.isNotEmpty()) Forest.copy(alpha = 0.15f) else DeepForest,
                    shape = RoundedCornerShape(10.dp),
                ).border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Forest else Stone.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineSmall,
            color = White,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OtpScreenPreview() {
    com.maku.eyeonsite.ui.theme.EyeOnSiteTheme {
        OtpScreen(
            phone = "+233 20 123 4567",
            onVerify = {},
            onResend = {},
            onBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OtpScreenErrorPreview() {
    com.maku.eyeonsite.ui.theme.EyeOnSiteTheme {
        OtpScreen(
            phone = "+233 20 123 4567",
            errorMessage = "Invalid code. Please try again.",
            onVerify = {},
            onResend = {},
            onBack = {},
        )
    }
}
