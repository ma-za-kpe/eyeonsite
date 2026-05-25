package com.maku.eyeonsite.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.maku.eyeonsite.domain.NearbyTask

@Composable
actual fun TaskMapView(
    tasks: List<NearbyTask>,
    selectedTaskId: String?,
    isClustered: Boolean,
    modifier: Modifier,
    onMarkerClick: (NearbyTask) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text("Map available in the mobile app.", style = MaterialTheme.typography.bodyMedium)
    }
}
