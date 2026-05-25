package com.maku.eyeonsite.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun CameraCapture(
    modifier: Modifier,
    onPhotoCaptured: (ByteArray, Double, Double) -> Unit,
    onPermissionDenied: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Camera not yet supported on iOS", style = MaterialTheme.typography.bodyMedium)
    }
}
