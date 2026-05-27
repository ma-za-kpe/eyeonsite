package com.maku.eyeonsite.ui.evidence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.data.NoOpEvidenceRepository
import com.maku.eyeonsite.domain.EvidencePackage
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.domain.UploadStatus
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.platform.CameraCapture
import com.maku.eyeonsite.ui.task.TopBar
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen

private const val PHOTO_GRID_COLUMNS = 3

@Composable
fun EvidenceCaptureScreen(
    task: VerificationTask,
    vm: EvidenceCaptureViewModel,
    onBack: () -> Unit,
    onSubmitted: (List<EvidencePhoto>) -> Unit,
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(task.id) { vm.initTask(task.id) }
    LaunchedEffect(state.isSubmitted) { if (state.isSubmitted) onSubmitted(state.photos) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        TopBar(title = "Capture Evidence", onBack = onBack)

        if (state.isSubmitted) {
            SubmittedContent()
            return@Column
        }

        // Camera view — takes upper portion of screen
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(360.dp),
        ) {
            if (state.canAddMore) {
                CameraCapture(
                    modifier = Modifier.fillMaxSize(),
                    onPhotoCaptured = { bytes, lat, lng -> vm.addPhoto(bytes, lat, lng) },
                    onPermissionDenied = {},
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Maximum ${EvidencePackage.MAX_PHOTOS} photos reached",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Photo counter badge
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "${state.photos.size} / ${EvidencePackage.MAX_PHOTOS}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Upload progress bar
        if (state.isUploading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Error banner
        state.error?.let { err ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(AlertCoral.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = AlertCoral,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "×",
                    style = MaterialTheme.typography.titleMedium,
                    color = AlertCoral,
                    modifier = Modifier.clickable { vm.clearError() }.padding(8.dp),
                )
            }
        }

        // Photo grid + submit
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.photos.isEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Tap the shutter to capture your first photo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(PHOTO_GRID_COLUMNS),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(state.photos, key = { it.id }) { photo ->
                        PhotoThumbnail(
                            photo = photo,
                            onRemove = { vm.removePhoto(photo.id) },
                        )
                    }
                }
            }

            // Submit button
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (state.canSubmit) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            },
                        ).clickable(enabled = state.canSubmit) { vm.submitEvidence() }
                        .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text =
                            if (state.photos.isEmpty()) {
                                "Add photos to submit"
                            } else {
                                "Submit Evidence (${state.photos.size} photo${if (state.photos.size == 1) "" else "s"})"
                            },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            Text(
                text = "Photos are GPS-stamped and cannot be modified after submission.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: EvidencePhoto,
    onRemove: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    when (photo.uploadStatus) {
                        UploadStatus.UPLOADED -> SignalGreen
                        UploadStatus.FAILED -> AlertCoral
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    },
                    RoundedCornerShape(6.dp),
                ),
    ) {
        // GPS badge
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
        ) {
            Text(
                text = "GPS",
                style = MaterialTheme.typography.labelSmall,
                color = if (photo.latitude != 0.0) SignalGreen else SavannaGold,
            )
        }

        // Upload status indicator
        when (photo.uploadStatus) {
            UploadStatus.UPLOADING ->
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp).align(Alignment.TopStart).padding(4.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            UploadStatus.UPLOADED ->
                Text(
                    text = "✓",
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = SignalGreen,
                )
            UploadStatus.FAILED ->
                Text(
                    text = "!",
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = AlertCoral,
                )
            else -> Unit
        }

        // Remove button
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(AlertCoral)
                    .clickable { onRemove() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "×",
                style = MaterialTheme.typography.labelSmall,
                color = com.maku.eyeonsite.ui.theme.White,
            )
        }
    }
}

@Composable
private fun SubmittedContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SignalGreen.copy(alpha = 0.15f))
                        .border(2.dp, SignalGreen, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SignalGreen,
                )
            }
            Text(
                text = "Evidence Submitted",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "The client will be notified and can now review your evidence.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun EvidenceCaptureScreenEmptyPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        EvidenceCaptureScreen(
            task =
                VerificationTask(
                    id = "task-001",
                    type = com.maku.eyeonsite.domain.TaskType.CONSTRUCTION,
                    location = "East Legon, Accra",
                    description = "Verify third floor slab",
                ),
            vm = EvidenceCaptureViewModel(NoOpEvidenceRepository(), NoOpPhotoQualityAnalyzer, NoOpAnalyticsService),
            onBack = {},
            onSubmitted = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun EvidenceCaptureScreenInspectorLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        EvidenceCaptureScreen(
            task =
                VerificationTask(
                    id = "task-001",
                    type = com.maku.eyeonsite.domain.TaskType.FARM,
                    location = "Kumasi, Ghana",
                    description = "Verify cocoa farm",
                ),
            vm = EvidenceCaptureViewModel(NoOpEvidenceRepository(), NoOpPhotoQualityAnalyzer, NoOpAnalyticsService),
            onBack = {},
            onSubmitted = {},
        )
    }
}
