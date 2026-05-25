package com.maku.eyeonsite.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

@Composable
fun SignInScreen(onContinue: (phoneNumber: String) -> Unit = {}) {
    var phoneNumber by remember { mutableStateOf("") }

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
            text = "EyeOnSite",
            style = MaterialTheme.typography.headlineLarge,
            color = White,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Trust, verified.",
            style = MaterialTheme.typography.bodyLarge,
            color = SavannaGold,
        )

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.headlineMedium,
                color = White,
            )
            Text(
                text = "Enter your phone number to continue.",
                style = MaterialTheme.typography.bodyLarge,
                color = Stone,
            )
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("+233 XX XXX XXXX", color = Stone)
            },
            label = {
                Text("Phone number", color = Stone)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = Forest,
                    unfocusedBorderColor = Stone,
                    focusedLabelColor = Forest,
                    cursorColor = SavannaGold,
                ),
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onContinue(phoneNumber) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            enabled = phoneNumber.length >= 10,
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
                text = "Continue",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "By continuing, you agree to our Terms of Service and Privacy Policy.",
            style = MaterialTheme.typography.bodySmall,
            color = Stone,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun LogoMark() {
    Box(
        modifier =
            Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Forest),
        contentAlignment = Alignment.Center,
    ) {
        // EyeOnSite mark: white drop-pin shape with dark iris.
        // Drawn via Canvas (no SVG dependency — works on all platforms).
        val dark = DeepForest
        Canvas(modifier = Modifier.size(64.dp)) {
            val w = size.width
            val h = size.height
            // Original mark SVG viewBox: 120 × 160
            val scale = minOf(w / 120f, h / 160f)
            val ox = (w - 120f * scale) / 2f
            val oy = (h - 160f * scale) / 2f

            fun tx(x: Float) = x * scale + ox

            fun ty(y: Float) = y * scale + oy

            // Drop/pin body (white)
            val drop =
                Path().apply {
                    moveTo(tx(60f), ty(4f))
                    cubicTo(tx(91f), ty(4f), tx(116f), ty(29f), tx(116f), ty(60f))
                    cubicTo(tx(116f), ty(91f), tx(60f), ty(152f), tx(60f), ty(152f))
                    cubicTo(tx(60f), ty(152f), tx(4f), ty(91f), tx(4f), ty(60f))
                    cubicTo(tx(4f), ty(29f), tx(29f), ty(4f), tx(60f), ty(4f))
                    close()
                }
            drawPath(drop, color = Color.White)

            // Eye lens highlight (dark, quadratic bezier lens shape)
            val lens =
                Path().apply {
                    moveTo(tx(26f), ty(62f))
                    quadraticBezierTo(tx(60f), ty(32f), tx(94f), ty(62f))
                    quadraticBezierTo(tx(60f), ty(92f), tx(26f), ty(62f))
                    close()
                }
            drawPath(lens, color = dark)

            // Iris (white circle)
            drawCircle(
                color = Color.White,
                radius = 16f * scale,
                center = Offset(tx(60f), ty(62f)),
            )

            // Pupil (dark circle)
            drawCircle(
                color = dark,
                radius = 8f * scale,
                center = Offset(tx(60f), ty(62f)),
            )
        }
    }
}
