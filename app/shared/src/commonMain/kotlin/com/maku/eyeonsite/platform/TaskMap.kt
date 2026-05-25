package com.maku.eyeonsite.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.maku.eyeonsite.domain.NearbyTask

@Composable
expect fun TaskMapView(
    tasks: List<NearbyTask>,
    selectedTaskId: String?,
    isClustered: Boolean,
    modifier: Modifier = Modifier,
    onMarkerClick: (NearbyTask) -> Unit,
)
