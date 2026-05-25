/** TrustBadge - small pill badge used in the hero trust row. */
package com.maku.eyeonsite.landing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.White

/** Small pill badge, for example "GPS-verified" or "Escrow-protected". */
@Composable
fun TrustBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Forest.copy(alpha = 0.25f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = White.copy(alpha = 0.9f),
        )
    }
}
