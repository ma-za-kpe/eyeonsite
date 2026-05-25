package com.maku.eyeonsite.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.maku.eyeonsite.domain.UserRole
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

@Composable
fun HomeScreen(role: UserRole) {
    Column(
        modifier = Modifier.fillMaxSize().background(DeepForest),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (role == UserRole.CLIENT) "Client Dashboard" else "Inspector Hub",
            style = MaterialTheme.typography.headlineLarge,
            color = White,
        )
        Text(
            text = "Coming soon…",
            style = MaterialTheme.typography.bodyLarge,
            color = Stone,
        )
        Text(
            text = role.name,
            style = MaterialTheme.typography.labelSmall,
            color = SavannaGold,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun HomeScreenClientPreview() {
    com.maku.eyeonsite.ui.theme.EyeOnSiteTheme {
        HomeScreen(role = UserRole.CLIENT)
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun HomeScreenInspectorPreview() {
    com.maku.eyeonsite.ui.theme.EyeOnSiteTheme {
        HomeScreen(role = UserRole.INSPECTOR)
    }
}
