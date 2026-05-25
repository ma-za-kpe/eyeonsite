package com.maku.eyeonsite.ui.auth

import androidx.compose.foundation.background
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
                .size(72.dp)
                .clip(CircleShape)
                .background(Forest),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "E",
                style = MaterialTheme.typography.headlineMedium,
                color = White,
            )
        }
    }
}
