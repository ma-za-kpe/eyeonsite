package com.maku.eyeonsite.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.task.TopBar
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen
import com.maku.eyeonsite.ui.theme.Stone

private val REVIEW_STATUSES = setOf(TaskStatus.EVIDENCE_SUBMITTED, TaskStatus.UNDER_REVIEW)

private val TIMELINE_STEPS =
    listOf("Posted", "Matched", "In Progress", "Evidence", "Complete")

@Suppress("MagicNumber")
private fun TaskStatus.timelineStep(): Int =
    when (this) {
        TaskStatus.DRAFT -> -1
        TaskStatus.POSTED, TaskStatus.ESCROW_FUNDED -> 0
        TaskStatus.INSPECTOR_MATCHED -> 1
        TaskStatus.IN_PROGRESS -> 2
        TaskStatus.EVIDENCE_SUBMITTED, TaskStatus.UNDER_REVIEW, TaskStatus.REVISION_REQUESTED -> 3
        TaskStatus.ACCEPTED, TaskStatus.AUTO_ACCEPTED -> 4
        TaskStatus.DISPUTED -> 3
        TaskStatus.CANCELLED, TaskStatus.EXPIRED -> -2
    }

@Composable
fun TaskDetailScreen(
    task: ClientTaskSummary,
    onBack: () -> Unit,
    onReviewEvidence: (() -> Unit)? = null,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        TopBar(title = "Task Detail", onBack = onBack)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Status timeline
            StatusTimeline(status = task.status)

            // Task info card
            DetailCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = task.type.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "$${task.bountyUsd}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Forest,
                        )
                    }
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = task.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Inspector card
            if (task.inspectorName != null) {
                InspectorCard(name = task.inspectorName)
            } else if (task.status == TaskStatus.POSTED || task.status == TaskStatus.ESCROW_FUNDED) {
                FindingInspectorCard()
            }

            // Map placeholder
            MapPlaceholder()

            // Review evidence CTA
            if (task.status in REVIEW_STATUSES && onReviewEvidence != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SavannaGold)
                            .clickable { onReviewEvidence() }
                            .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Review Evidence",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatusTimeline(status: TaskStatus) {
    val currentStep = status.timelineStep()
    val isCancelled = status == TaskStatus.CANCELLED || status == TaskStatus.EXPIRED

    DetailCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isCancelled) {
                    Text(
                        text = status.displayLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = AlertCoral,
                    )
                }
            }

            if (!isCancelled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TIMELINE_STEPS.forEachIndexed { index, label ->
                        val isDone = index < currentStep
                        val isCurrent = index == currentStep

                        val dotColor =
                            when {
                                isDone -> SignalGreen
                                isCurrent -> Forest
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(dotColor),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                    if (isCurrent) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDone) 1f else 0.4f)
                                    },
                            )
                        }

                        if (index < TIMELINE_STEPS.lastIndex) {
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(if (isDone) SignalGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        .padding(bottom = 14.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectorCard(name: String) {
    DetailCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar placeholder
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Forest.copy(alpha = 0.12f))
                        .border(1.dp, Forest.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Forest,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Verified Inspector",
                    style = MaterialTheme.typography.labelSmall,
                    color = SignalGreen,
                )
            }
        }
    }
}

@Composable
private fun FindingInspectorCard() {
    DetailCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Stone.copy(alpha = 0.15f))
                        .border(1.dp, Stone.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Stone,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Finding inspector...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Nearby inspectors are being notified",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MapPlaceholder() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    RoundedCornerShape(10.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Map",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Task location",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    RoundedCornerShape(10.dp),
                ).padding(16.dp),
    ) {
        content()
    }
}

private fun TaskStatus.displayLabel(): String =
    when (this) {
        TaskStatus.CANCELLED -> "Cancelled"
        TaskStatus.EXPIRED -> "Expired"
        else -> name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
    }

// ── Previews ──────────────────────────────────────────────────────────────────

private fun previewTask(
    status: TaskStatus,
    inspector: String? = null,
) = ClientTaskSummary(
    id = "task-001",
    type = TaskType.CONSTRUCTION,
    location = "East Legon, Accra",
    description = "Verify floor slab and column placement on floors 3–5",
    status = status,
    bountyUsd = 45,
    createdAtMs = 1_748_000_000_000L,
    inspectorName = inspector,
)

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskDetailPostedPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        TaskDetailScreen(
            task = previewTask(TaskStatus.POSTED),
            onBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskDetailInProgressPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        TaskDetailScreen(
            task = previewTask(TaskStatus.IN_PROGRESS, inspector = "Kwame A."),
            onBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskDetailEvidenceReadyPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = true) {
        TaskDetailScreen(
            task = previewTask(TaskStatus.EVIDENCE_SUBMITTED, inspector = "Abena K."),
            onBack = {},
            onReviewEvidence = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskDetailAcceptedPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        TaskDetailScreen(
            task = previewTask(TaskStatus.ACCEPTED, inspector = "Kwame A."),
            onBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskDetailExpiredPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        TaskDetailScreen(
            task = previewTask(TaskStatus.EXPIRED),
            onBack = {},
        )
    }
}
