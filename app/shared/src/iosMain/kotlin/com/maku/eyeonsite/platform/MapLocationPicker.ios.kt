package com.maku.eyeonsite.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun MapLocationPicker(
    initialLat: Double,
    initialLng: Double,
    onLocationPicked: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier,
) {
    // iOS placeholder until MapKit-based picker is wired up.
    LaunchedEffect(initialLat, initialLng) {
        onLocationPicked(initialLat, initialLng)
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Map picker not yet implemented on iOS")
    }
}
