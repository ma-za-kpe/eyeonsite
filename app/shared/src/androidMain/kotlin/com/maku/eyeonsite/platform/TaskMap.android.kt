package com.maku.eyeonsite.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.maku.eyeonsite.domain.NearbyTask
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.theme.CautionAmber

private const val DEFAULT_ZOOM = 13f
private const val SELECTED_ZOOM = 15f
private const val ACCRA_CBD_LAT = 5.5502
private const val ACCRA_CBD_LNG = -0.2174

private val DEFAULT_LAT_LNG = LatLng(ACCRA_CBD_LAT, ACCRA_CBD_LNG)

private fun NearbyTask.markerColor(): Float =
    when (type) {
        TaskType.CONSTRUCTION -> BitmapDescriptorFactory.HUE_ORANGE
        TaskType.PROPERTY -> BitmapDescriptorFactory.HUE_AZURE
        TaskType.FARM -> BitmapDescriptorFactory.HUE_GREEN
        TaskType.BUSINESS -> BitmapDescriptorFactory.HUE_YELLOW
    }

@Composable
actual fun TaskMapView(
    tasks: List<NearbyTask>,
    selectedTaskId: String?,
    isClustered: Boolean,
    modifier: Modifier,
    onMarkerClick: (NearbyTask) -> Unit,
) {
    val centerLatLng =
        tasks
            .firstOrNull()
            ?.let { LatLng(it.latitude, it.longitude) }
            ?: DEFAULT_LAT_LNG

    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(centerLatLng, DEFAULT_ZOOM)
        }

    LaunchedEffect(selectedTaskId) {
        val selected = tasks.find { it.id == selectedTaskId }
        if (selected != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(selected.latitude, selected.longitude),
                    SELECTED_ZOOM,
                ),
                durationMs = 500,
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties =
                MapProperties(
                    isTrafficEnabled = false,
                    isBuildingEnabled = false,
                ),
            uiSettings =
                MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                ),
        ) {
            tasks.forEach { task ->
                val isSelected = task.id == selectedTaskId
                Marker(
                    state = MarkerState(position = LatLng(task.latitude, task.longitude)),
                    title = task.type.label,
                    snippet = "${task.location} · $${task.bountyUsd}",
                    icon = BitmapDescriptorFactory.defaultMarker(task.markerColor()),
                    zIndex = if (isSelected) 1f else 0f,
                    onClick = {
                        onMarkerClick(task)
                        false
                    },
                )
            }
        }

        // Cluster badge when many tasks
        if (isClustered) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CautionAmber)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "${tasks.size} tasks nearby",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                )
            }
        }
    }
}
