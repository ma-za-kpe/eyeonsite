package com.maku.eyeonsite.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.domain.UserRole
import com.maku.eyeonsite.ui.theme.Ash
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

@Composable
fun RoleSelectionScreen(
    uid: String,
    onRoleSelected: (uid: String, role: UserRole) -> Unit,
) {
    var selected by remember { mutableStateOf<UserRole?>(null) }

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
            text = "How are you using\nEyeOnSite?",
            style = MaterialTheme.typography.headlineLarge,
            color = White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Choose your role — you can only have one.",
            style = MaterialTheme.typography.bodyLarge,
            color = Stone,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            RoleCard(
                role = UserRole.CLIENT,
                title = "Verifying my project",
                description = "I have a property, construction project, or investment I need verified on the ground.",
                isSelected = selected == UserRole.CLIENT,
                onClick = { selected = UserRole.CLIENT },
            )

            RoleCard(
                role = UserRole.INSPECTOR,
                title = "I'm here to inspect",
                description = "I'll visit sites, capture GPS-verified photos, and submit evidence for clients.",
                isSelected = selected == UserRole.INSPECTOR,
                onClick = { selected = UserRole.INSPECTOR },
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { selected?.let { onRoleSelected(uid, it) } },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = selected != null,
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
                text = "Continue as ${selected?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "…"}",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun RoleCard(
    role: UserRole,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Forest else Stone.copy(alpha = 0.25f)
    val bgColor = if (isSelected) Forest.copy(alpha = 0.12f) else White.copy(alpha = 0.04f)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        RoleIcon(role = role, isSelected = isSelected)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) White else White.copy(alpha = 0.85f),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Stone else Ash,
            )
        }
    }
}

@Composable
private fun RoleIcon(
    role: UserRole,
    isSelected: Boolean,
) {
    val iconColor = if (isSelected) SavannaGold else Stone
    Canvas(
        modifier = Modifier.size(40.dp),
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f * 0.85f
        drawCircle(color = iconColor.copy(alpha = 0.15f), radius = r, center = Offset(cx, cy))
        when (role) {
            UserRole.CLIENT -> {
                // Simple building icon: two rectangles
                drawRect(
                    color = iconColor,
                    topLeft = Offset(cx - r * 0.5f, cy - r * 0.4f),
                    size = Size(r * 1.0f, r * 0.9f),
                )
                drawRect(
                    color = if (isSelected) Forest else DeepForest,
                    topLeft = Offset(cx - r * 0.15f, cy + r * 0.05f),
                    size = Size(r * 0.3f, r * 0.45f),
                )
            }
            UserRole.INSPECTOR -> {
                // Simple camera icon: filled circle (lens)
                drawCircle(color = iconColor, radius = r * 0.38f, center = Offset(cx, cy + r * 0.05f))
                drawCircle(color = if (isSelected) Forest else DeepForest, radius = r * 0.2f, center = Offset(cx, cy + r * 0.05f))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun RoleSelectionScreenPreview() {
    com.maku.eyeonsite.ui.theme.EyeOnSiteTheme {
        RoleSelectionScreen(uid = "preview-uid", onRoleSelected = { _, _ -> })
    }
}
