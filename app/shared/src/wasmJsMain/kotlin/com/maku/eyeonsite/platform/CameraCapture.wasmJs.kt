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
    onPhotoCaptured: (ByteArray, Double, Double, String, Float?) -> Unit,
    onPermissionDenied: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Camera capture is only available on the mobile app.", style = MaterialTheme.typography.bodyMedium)
    }
}
