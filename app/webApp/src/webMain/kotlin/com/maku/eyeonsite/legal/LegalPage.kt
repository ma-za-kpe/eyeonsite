package com.maku.eyeonsite.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.navigateTo
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

@Composable
fun LegalPage(
    title: String,
    lastUpdated: String,
    content: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF011710)),
        contentPadding = PaddingValues(bottom = 64.dp),
    ) {
        item {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(DeepForest)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    Text("Eye", style = MaterialTheme.typography.titleMedium, color = Forest)
                    Text("On", style = MaterialTheme.typography.titleMedium, color = White)
                    Text("Site", style = MaterialTheme.typography.titleMedium, color = Forest)
                }
                Text(
                    "← Home",
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                    color = SavannaGold,
                    modifier = Modifier.clickable { navigateTo("/") },
                )
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 720.dp).padding(vertical = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = MaterialTheme.typography.displaySmall, color = White)
                        Text(
                            "Last updated: $lastUpdated · Manti Labs LLC",
                            style = MaterialTheme.typography.bodySmall,
                            color = Stone,
                        )
                    }
                    Spacer(
                        Modifier.fillMaxWidth().height(1.dp).background(White.copy(alpha = 0.08f)),
                    )
                    content()
                }
            }
        }
        item {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(DeepForest)
                        .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "© 2025 Manti Labs LLC (Delaware) · All rights reserved",
                    style = MaterialTheme.typography.bodySmall,
                    color = Stone.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
fun LegalSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = SavannaGold)
        content()
    }
}

@Composable
fun LegalBody(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = Stone)
}

@Composable
fun LegalSubheading(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = White)
}

@Composable
fun LegalBulletList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("·", style = MaterialTheme.typography.bodyMedium, color = SavannaGold)
                Text(item, style = MaterialTheme.typography.bodyMedium, color = Stone)
            }
        }
    }
}

@Composable
fun LegalKeyValue(rows: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { (key, value) ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    key,
                    style = MaterialTheme.typography.bodySmall,
                    color = White,
                    modifier = Modifier.widthIn(min = 140.dp),
                )
                Text(value, style = MaterialTheme.typography.bodySmall, color = Stone)
            }
        }
    }
}
