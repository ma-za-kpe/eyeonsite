package com.maku.eyeonsite.ui.evidence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.domain.ChecklistAnswer
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.UploadStatus
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.domain.computeQualityScore
import com.maku.eyeonsite.ui.task.TopBar
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen

@Composable
fun EvidenceSummaryScreen(
    task: VerificationTask,
    photos: List<EvidencePhoto>,
    checklistAnswers: List<ChecklistAnswer>,
    onBack: () -> Unit,
    onConfirmed: () -> Unit,
) {
    val score = computeQualityScore(photos, checklistAnswers)
    val scorePercent = (score * 100).toInt()
    val scoreColor =
        when {
            score >= 0.75f -> SignalGreen
            score >= 0.5f -> SavannaGold
            else -> AlertCoral
        }
    val yesCount = checklistAnswers.count { it.boolValue == true }
    val yesNoCount = checklistAnswers.count { it.boolValue != null }
    val textNotes = checklistAnswers.filter { it.textValue.isNotBlank() }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        TopBar(title = "Evidence Summary", onBack = onBack)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Quality score badge
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(scoreColor.copy(alpha = 0.12f))
                                .border(2.dp, scoreColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$scorePercent%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = scoreColor,
                        )
                    }
                    Text(
                        text = "Quality Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Task details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = task.type.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = task.location,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Photo grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SummarySection("Photos · ${photos.size}")
                if (photos.isEmpty()) {
                    Text(
                        text = "No photos captured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val photoRows = photos.chunked(3)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        photoRows.forEach { rowPhotos ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                rowPhotos.forEach { photo ->
                                    SummaryPhotoThumbnail(
                                        photo = photo,
                                        modifier = Modifier.weight(1f).aspectRatio(1f),
                                    )
                                }
                                repeat(3 - rowPhotos.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Checklist summary
            if (yesNoCount > 0) {
                SummarySection("Checklist · $yesCount / $yesNoCount confirmed")
            }

            // Notes
            if (textNotes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummarySection("Notes")
                    textNotes.forEach { answer ->
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp),
                                    ).padding(14.dp),
                        ) {
                            Text(
                                text = answer.textValue,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Confirm button
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Forest)
                        .clickable { onConfirmed() }
                        .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Confirm & Finalise",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun SummarySection(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SummaryPhotoThumbnail(
    photo: EvidencePhoto,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
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
    }
}

private fun makeSummaryPreviewPhotos() =
    listOf(
        EvidencePhoto(
            id = "p1",
            taskId = "t1",
            latitude = 5.6037,
            longitude = -0.1870,
            capturedAtMs = 1_748_000_000_000L,
            uploadStatus = UploadStatus.UPLOADED,
        ),
        EvidencePhoto(
            id = "p2",
            taskId = "t1",
            latitude = 5.6038,
            longitude = -0.1871,
            capturedAtMs = 1_748_000_060_000L,
            uploadStatus = UploadStatus.UPLOADED,
        ),
        EvidencePhoto(
            id = "p3",
            taskId = "t1",
            latitude = 5.6039,
            longitude = -0.1872,
            capturedAtMs = 1_748_000_120_000L,
            uploadStatus = UploadStatus.FAILED,
        ),
    )

private fun makeSummaryPreviewAnswers() =
    listOf(
        ChecklistAnswer(itemId = "q1", boolValue = true),
        ChecklistAnswer(itemId = "q2", boolValue = true),
        ChecklistAnswer(itemId = "q3", boolValue = false),
        ChecklistAnswer(itemId = "q4", boolValue = true),
        ChecklistAnswer(itemId = "notes", textValue = "The columns look slightly off-centre on the north wall."),
    )

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun EvidenceSummaryScreenPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        EvidenceSummaryScreen(
            task =
                VerificationTask(
                    id = "task-001",
                    type = TaskType.CONSTRUCTION,
                    location = "East Legon, Accra",
                    description = "Verify floor slab and column placement",
                ),
            photos = makeSummaryPreviewPhotos(),
            checklistAnswers = makeSummaryPreviewAnswers(),
            onBack = {},
            onConfirmed = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun EvidenceSummaryScreenDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        EvidenceSummaryScreen(
            task =
                VerificationTask(
                    id = "task-002",
                    type = TaskType.FARM,
                    location = "Kumasi, Ghana",
                    description = "Verify cocoa farm acreage",
                ),
            photos = makeSummaryPreviewPhotos(),
            checklistAnswers = makeSummaryPreviewAnswers(),
            onBack = {},
            onConfirmed = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun EvidenceSummaryScreenEmptyPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        EvidenceSummaryScreen(
            task =
                VerificationTask(
                    id = "task-003",
                    type = TaskType.PROPERTY,
                    location = "Airport Residential, Accra",
                    description = "Verify boundary and occupancy",
                ),
            photos = emptyList(),
            checklistAnswers = emptyList(),
            onBack = {},
            onConfirmed = {},
        )
    }
}
