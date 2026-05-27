package com.maku.eyeonsite.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.domain.ChecklistResponse
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.domain.ReviewDecision
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.CautionAmber
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SignalGreen
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val GPS_DECIMAL_PLACES = 4
private const val REVIEW_WINDOW_SECONDS = 48L * 3600

private fun Double.gpsFormat(): String {
    val s = toString()
    val dot = s.indexOf('.')
    return if (dot < 0 || s.length - dot - 1 <= GPS_DECIMAL_PLACES) {
        s
    } else {
        s.substring(0, dot + GPS_DECIMAL_PLACES + 1)
    }
}

private const val PHOTO_HEIGHT_DP = 260
private const val NOTES_MIN_HEIGHT_DP = 80
private const val DECISION_CARD_RADIUS_DP = 12

@Composable
fun EvidenceReviewScreen(
    vm: EvidenceReviewViewModel,
    task: VerificationTask,
    photos: List<EvidencePhoto>,
    onBack: () -> Unit,
    onReviewSubmitted: () -> Unit,
) {
    LaunchedEffect(Unit) {
        vm.loadEvidence(task.id, task.description, task.type, photos)
    }

    // Countdown timer ticks every second via coroutine
    LaunchedEffect(Unit) {
        var seconds = REVIEW_WINDOW_SECONDS
        while (seconds >= 0) {
            vm.updateCountdown(seconds)
            delay(1000L)
            seconds--
        }
    }

    val state by vm.state.collectAsState()

    when (val s = state) {
        EvidenceReviewUiState.Loading -> LoadingContent()
        is EvidenceReviewUiState.Reviewing ->
            ReviewingContent(
                state = s,
                taskLat = task.locationLat,
                taskLng = task.locationLng,
                onBack = onBack,
                onPhotoSelect = { vm.selectPhoto(it) },
                onDecisionChange = { vm.setDecision(it) },
                onNotesChange = { vm.setNotes(it) },
                onSubmit = { vm.submitReview() },
            )
        is EvidenceReviewUiState.Submitted ->
            SubmittedContent(
                decision = s.decision,
                onDone = onReviewSubmitted,
            )
        is EvidenceReviewUiState.Error ->
            ErrorContent(
                message = s.message,
                onRetry = { vm.clearError() },
                onBack = onBack,
            )
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Forest)
    }
}

@Composable
private fun ReviewingContent(
    state: EvidenceReviewUiState.Reviewing,
    taskLat: Double,
    taskLng: Double,
    onBack: () -> Unit,
    onPhotoSelect: (Int) -> Unit,
    onDecisionChange: (ReviewDecision?) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
    ) {
        // Top bar
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { onBack() }.padding(4.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Review Evidence",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "${state.photoCount} photo${if (state.photoCount == 1) "" else "s"} submitted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Auto-accept countdown
        state.secondsRemaining?.let { secs ->
            CountdownBanner(secondsRemaining = secs)
        }

        // Task info header
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TaskTypeBadge(state.taskType)
            Text(
                text = state.taskDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        // Photo viewer
        if (state.hasPhotos) {
            PhotoViewer(
                photos = state.photos,
                selectedIndex = state.selectedPhotoIndex,
                qualityScore = state.qualityScore,
                onPhotoSelect = onPhotoSelect,
            )
        } else {
            NoPhotosPlaceholder()
        }

        // Checklist responses
        if (state.checklistResponses.isNotEmpty()) {
            ChecklistSection(responses = state.checklistResponses)
        }

        // GPS location verification
        if (state.hasPhotos && (taskLat != 0.0 || taskLng != 0.0)) {
            LocationVerificationCard(
                taskLat = taskLat,
                taskLng = taskLng,
                photos = state.photos,
            )
        }

        // Decision section
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Your Decision",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            ReviewDecision.entries.forEach { decision ->
                DecisionCard(
                    decision = decision,
                    selected = state.decision == decision,
                    onClick = {
                        onDecisionChange(if (state.decision == decision) null else decision)
                    },
                )
            }
        }

        // Notes input
        Column(
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Notes (optional)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp),
                        ).background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
            ) {
                if (state.notes.isEmpty()) {
                    Text(
                        text = "Add notes for the inspector…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
                BasicTextField(
                    value = state.notes,
                    onValueChange = onNotesChange,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = NOTES_MIN_HEIGHT_DP.dp),
                    textStyle =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                )
            }
        }

        // Submit button
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (state.canSubmit) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ).clickable(enabled = state.canSubmit) { onSubmit() }
                    .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = if (state.decision == null) "Select a decision above" else "Submit Review",
                    style = MaterialTheme.typography.labelLarge,
                    color =
                        if (state.canSubmit) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                )
            }
        }
    }
}

@Composable
private fun PhotoViewer(
    photos: List<EvidencePhoto>,
    selectedIndex: Int,
    qualityScore: Float?,
    onPhotoSelect: (Int) -> Unit,
) {
    val photo = photos.getOrNull(selectedIndex) ?: return

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Photo placeholder with GPS overlay
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(PHOTO_HEIGHT_DP.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "📷",
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text = "Photo ${selectedIndex + 1} of ${photos.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // GPS badge — JetBrains Mono via bodySmall
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DeepForest.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "📍 ${photo.latitude.gpsFormat()}, ${photo.longitude.gpsFormat()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                )
            }

            // GPS verified badge
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SignalGreen.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "✓ GPS verified",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                )
            }

            // Quality score badge (top-right)
            qualityScore?.let { score ->
                val (badgeColor, badgeLabel) =
                    when {
                        score >= 0.8f -> Pair(SignalGreen, "Quality ${(score * 100).toInt()}%")
                        score >= 0.6f -> Pair(CautionAmber, "Quality ${(score * 100).toInt()}%")
                        else -> Pair(AlertCoral, "Quality ${(score * 100).toInt()}%")
                    }
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        }

        // Prev / Next navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavArrow(
                label = "←",
                enabled = selectedIndex > 0,
                onClick = { onPhotoSelect(selectedIndex - 1) },
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                photos.forEachIndexed { index, _ ->
                    Box(
                        modifier =
                            Modifier
                                .size(if (index == selectedIndex) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == selectedIndex) {
                                        Forest
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    },
                                ).clickable { onPhotoSelect(index) },
                    )
                }
            }
            NavArrow(
                label = "→",
                enabled = selectedIndex < photos.size - 1,
                onClick = { onPhotoSelect(selectedIndex + 1) },
            )
        }
    }
}

@Composable
private fun NavArrow(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(
                    if (enabled) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    },
                ).clickable(enabled = enabled) { onClick() }
                .padding(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color =
                if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                },
        )
    }
}

@Composable
private fun CountdownBanner(secondsRemaining: Long) {
    val hours = secondsRemaining / 3600
    val minutes = (secondsRemaining % 3600) / 60
    val seconds = secondsRemaining % 60
    val isUrgent = secondsRemaining < 3600
    val bannerColor = if (isUrgent) AlertCoral else CautionAmber

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bannerColor.copy(alpha = 0.1f))
                .border(1.dp, bannerColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Auto-accepts in",
            style = MaterialTheme.typography.labelSmall,
            color = bannerColor,
        )
        Text(
            text = "${hours.toString().padStart(2, '0')}h ${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s",
            style = MaterialTheme.typography.bodySmall,
            color = bannerColor,
        )
    }
}

@Composable
private fun ChecklistSection(responses: List<ChecklistResponse>) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Inspector Checklist",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        responses.forEach { response ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (response.checked) SignalGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (response.checked) SignalGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(3.dp),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (response.checked) {
                        Text("✓", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SignalGreen)
                    }
                }
                Text(
                    text = response.item,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (response.checked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NoPhotosPlaceholder() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(PHOTO_HEIGHT_DP.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("📷", style = MaterialTheme.typography.displaySmall)
            Text(
                text = "No photos submitted yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DecisionCard(
    decision: ReviewDecision,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accentColor =
        when (decision) {
            ReviewDecision.ACCEPTED -> Forest
            ReviewDecision.REVISION_REQUESTED -> CautionAmber
            ReviewDecision.DISPUTED -> AlertCoral
        }

    val leadingIcon =
        when (decision) {
            ReviewDecision.ACCEPTED -> "✓"
            ReviewDecision.REVISION_REQUESTED -> "↺"
            ReviewDecision.DISPUTED -> "⚑"
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(DECISION_CARD_RADIUS_DP.dp))
                .background(
                    if (selected) accentColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ).border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(DECISION_CARD_RADIUS_DP.dp),
                ).clickable { onClick() }
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = if (selected) 0.2f else 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = leadingIcon,
                style = MaterialTheme.typography.titleSmall,
                color = accentColor,
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = decision.label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = decision.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }

        if (selected) {
            Box(
                modifier =
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✓", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

@Composable
private fun TaskTypeBadge(type: TaskType) {
    val accent =
        when (type) {
            TaskType.CONSTRUCTION -> CautionAmber
            TaskType.PROPERTY -> Color(0xFF4A90D9)
            TaskType.FARM -> Forest
            TaskType.BUSINESS -> MaterialTheme.colorScheme.tertiary
        }
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(accent.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = type.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = accent,
        )
    }
}

private data class DecisionPresentation(
    val icon: String,
    val title: String,
    val body: String,
    val color: Color,
)

private fun decisionPresentation(decision: ReviewDecision): DecisionPresentation =
    when (decision) {
        ReviewDecision.ACCEPTED ->
            DecisionPresentation(
                icon = "✓",
                title = "Evidence Accepted",
                body = "Payment is being released to the inspector. You'll receive a confirmation shortly.",
                color = SignalGreen,
            )
        ReviewDecision.REVISION_REQUESTED ->
            DecisionPresentation(
                icon = "↺",
                title = "Revision Requested",
                body = "The inspector has been notified and will submit additional evidence.",
                color = CautionAmber,
            )
        ReviewDecision.DISPUTED ->
            DecisionPresentation(
                icon = "⚑",
                title = "Dispute Filed",
                body = "Our team will review the evidence within 24 hours and contact both parties.",
                color = AlertCoral,
            )
    }

@Composable
private fun SubmittedContent(
    decision: ReviewDecision,
    onDone: () -> Unit,
) {
    val p = decisionPresentation(decision)

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
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
                        .background(p.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = p.icon, style = MaterialTheme.typography.displaySmall, color = p.color)
            }
            Text(
                text = p.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = p.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onDone() }
                        .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(text = "⚠", style = MaterialTheme.typography.displayMedium)
            Text(
                text = "Submission Failed",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onRetry() }
                        .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Try Again",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onBack() }
                        .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Go Back",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ── GPS verification ─────────────────────────────────────────────────────────

@Suppress("MagicNumber")
private fun haversineMeters(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    val r = 6_371_000.0
    val dLat = (lat2 - lat1) * kotlin.math.PI / 180
    val dLng = (lng2 - lng1) * kotlin.math.PI / 180
    val sinDLat = sin(dLat / 2)
    val sinDLng = sin(dLng / 2)
    val a = sinDLat * sinDLat + cos(lat1 * kotlin.math.PI / 180) * cos(lat2 * kotlin.math.PI / 180) * sinDLng * sinDLng
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

@Composable
private fun LocationVerificationCard(
    taskLat: Double,
    taskLng: Double,
    photos: List<EvidencePhoto>,
) {
    val inspectorLat = photos.sumOf { it.latitude } / photos.size
    val inspectorLng = photos.sumOf { it.longitude } / photos.size
    val distM = haversineMeters(taskLat, taskLng, inspectorLat, inspectorLng)
    val (distLabel, distColor) =
        when {
            distM < 500 -> "${distM.toInt()}m — on-site" to SignalGreen
            distM < 2_000 -> "${distM.toInt()}m — nearby" to CautionAmber
            else -> "${(distM / 1_000 * 10).toLong() / 10.0}km — far" to AlertCoral
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(10.dp),
                ).background(MaterialTheme.colorScheme.surface)
                .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "GPS Verification",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(distColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(text = distLabel, style = MaterialTheme.typography.labelSmall, color = distColor)
            }
        }
        GpsRow(pin = "📍", label = "Task", lat = taskLat, lng = taskLng)
        GpsRow(pin = "📷", label = "Inspector", lat = inspectorLat, lng = inspectorLng)
    }
}

@Composable
private fun GpsRow(
    pin: String,
    label: String,
    lat: Double,
    lng: Double,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = pin, style = MaterialTheme.typography.bodySmall)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp),
        )
        Text(
            text = "${lat.gpsFormat()}, ${lng.gpsFormat()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private fun fakePhoto(id: String) =
    EvidencePhoto(
        id = id,
        taskId = "task-preview",
        latitude = 5.5502,
        longitude = -0.2174,
        capturedAtMs = 1_700_000_000_000L,
    )

private val previewTask =
    VerificationTask(
        id = "task-preview",
        type = TaskType.CONSTRUCTION,
        location = "East Legon, Accra",
        locationLat = 5.5502,
        locationLng = -0.2174,
        description = "Verify floor slab and column placement on floors 3–5 of the residential block",
    )

private val previewChecklist =
    listOf(
        ChecklistResponse("Exterior facade progress", checked = true),
        ChecklistResponse("Floor slab status", checked = true),
        ChecklistResponse("Column/beam placement", checked = true),
        ChecklistResponse("Materials on site", checked = false),
        ChecklistResponse("Safety barriers present", checked = true),
    )

private class FakeReviewer : EvidenceReviewer {
    override suspend fun submitDecision(
        taskId: String,
        decision: ReviewDecision,
        notes: String,
    ) = Result.success(Unit)
}

private fun makeVm(block: EvidenceReviewViewModel.() -> Unit = {}): EvidenceReviewViewModel =
    EvidenceReviewViewModel(FakeReviewer(), NoOpAnalyticsService).apply(block)

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun EvidenceReviewLoadingPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = true) {
        EvidenceReviewScreen(
            vm = makeVm(),
            task = previewTask,
            photos = emptyList(),
            onBack = {},
            onReviewSubmitted = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun EvidenceReviewWithPhotosPreview() {
    val vm =
        makeVm {
            loadEvidence(
                "task-preview",
                previewTask.description,
                previewTask.type,
                listOf(fakePhoto("p1"), fakePhoto("p2"), fakePhoto("p3")),
                qualityScore = 0.87f,
                checklistResponses = previewChecklist,
            )
            updateCountdown(172_433L)
        }
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = true) {
        EvidenceReviewScreen(
            vm = vm,
            task = previewTask,
            photos = listOf(fakePhoto("p1"), fakePhoto("p2"), fakePhoto("p3")),
            onBack = {},
            onReviewSubmitted = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun EvidenceReviewDecisionSelectedPreview() {
    val vm =
        makeVm {
            loadEvidence(
                "task-preview",
                previewTask.description,
                previewTask.type,
                listOf(fakePhoto("p1"), fakePhoto("p2")),
                qualityScore = 0.62f,
                checklistResponses = previewChecklist,
            )
            setDecision(ReviewDecision.ACCEPTED)
            updateCountdown(3240L)
        }
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        EvidenceReviewScreen(
            vm = vm,
            task = previewTask,
            photos = listOf(fakePhoto("p1"), fakePhoto("p2")),
            onBack = {},
            onReviewSubmitted = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun EvidenceReviewSubmittedPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = true) {
        SubmittedContent(decision = ReviewDecision.ACCEPTED, onDone = {})
    }
}
