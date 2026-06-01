package com.maku.eyeonsite.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraCapture(
    modifier: Modifier = Modifier,
    onPhotoCaptured: (bytes: ByteArray, lat: Double, lng: Double, provider: String, accuracyMeters: Float?) -> Unit,
    onPermissionDenied: () -> Unit,
)
