package com.maku.eyeonsite.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.domain.UserRole
import com.maku.eyeonsite.ui.theme.CautionAmber
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

@Composable
fun HomeScreen(
    role: UserRole,
    onCreateTask: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(DeepForest)
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (role == UserRole.CLIENT) "Client Dashboard" else "Inspector Hub",
                style = MaterialTheme.typography.headlineMedium,
                color = White,
            )
            Text(
                text = if (role == UserRole.CLIENT) "Manage your verification tasks" else "Your task feed",
                style = MaterialTheme.typography.bodyMedium,
                color = Stone,
            )
        }

        if (role == UserRole.CLIENT) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Forest)
                        .clickable { onCreateTask() }
                        .padding(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Create Verification Task",
                        style = MaterialTheme.typography.titleMedium,
                        color = White,
                    )
                    Text(
                        text = "Hire a local inspector to verify your property, construction, or business",
                        style = MaterialTheme.typography.bodySmall,
                        color = White.copy(alpha = 0.7f),
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "No tasks yet",
                        style = MaterialTheme.typography.titleSmall,
                        color = Stone,
                    )
                    Text(
                        text = "Your verification tasks will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = Stone.copy(alpha = 0.6f),
                    )
                }
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(White.copy(alpha = 0.05f))
                        .border(1.dp, White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Task Feed",
                        style = MaterialTheme.typography.titleMedium,
                        color = White,
                    )
                    Text(
                        text = "Available tasks near you will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = Stone,
                    )
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CautionAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Coming soon",
                            style = MaterialTheme.typography.labelSmall,
                            color = CautionAmber,
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun HomeScreenClientPreview() {
    EyeOnSiteTheme {
        HomeScreen(role = UserRole.CLIENT, onCreateTask = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun HomeScreenInspectorPreview() {
    EyeOnSiteTheme {
        HomeScreen(role = UserRole.INSPECTOR, onCreateTask = {})
    }
}
