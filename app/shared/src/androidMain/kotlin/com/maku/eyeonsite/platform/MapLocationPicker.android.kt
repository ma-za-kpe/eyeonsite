package com.maku.eyeonsite.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.maku.eyeonsite.ui.theme.Forest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val DEFAULT_ZOOM = 14f

@Composable
actual fun MapLocationPicker(
    initialLat: Double,
    initialLng: Double,
    onLocationPicked: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier,
) {
    val initialLatLng = LatLng(initialLat, initialLng)
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialLatLng, DEFAULT_ZOOM)
        }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                val target = cameraPositionState.position.target
                onLocationPicked(target.latitude, target.longitude)
            }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isTrafficEnabled = false),
            uiSettings =
                MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                ),
        )

        // Fixed pin at center
        Box(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 20.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center),
            ) {
                Text(
                    text = "📍",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Forest,
                )
            }
        }
    }
}
