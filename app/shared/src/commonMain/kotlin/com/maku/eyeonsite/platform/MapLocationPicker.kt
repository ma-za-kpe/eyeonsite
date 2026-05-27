package com.maku.eyeonsite.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MapLocationPicker(
    initialLat: Double,
    initialLng: Double,
    onLocationPicked: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier,
)
