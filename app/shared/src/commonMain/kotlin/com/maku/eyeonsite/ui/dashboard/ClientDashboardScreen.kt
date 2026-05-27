package com.maku.eyeonsite.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.data.MockClientTaskRepository
import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.CautionAmber
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen
import com.maku.eyeonsite.ui.theme.Stone

private val REVIEW_STATUSES = setOf(TaskStatus.EVIDENCE_SUBMITTED, TaskStatus.UNDER_REVIEW)

@Composable
fun ClientDashboardScreen(
    vm: ClientDashboardViewModel,
    clientUid: String,
    onCreateTask: () -> Unit = {},
    onTaskDetail: (ClientTaskSummary) -> Unit = {},
    onReviewEvidence: (ClientTaskSummary) -> Unit = {},
    onBack: () -> Unit = {},
) {
    LaunchedEffect(clientUid) { vm.loadDashboard(clientUid) }
    val state by vm.state.collectAsState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardTopBar(onBack = onBack, onCreateTask = onCreateTask)

            when (val s = state) {
                is ClientDashboardUiState.Loading -> DashboardLoading()
                is ClientDashboardUiState.Empty -> DashboardEmpty(onCreateTask = onCreateTask)
                is ClientDashboardUiState.Dashboard ->
                    DashboardContent(
                        dashboard = s,
                        onTaskDetail = onTaskDetail,
                        onReviewEvidence = onReviewEvidence,
                    )
                is ClientDashboardUiState.Error ->
                    DashboardError(message = s.message, onRetry = { vm.loadDashboard(clientUid) })
            }
        }

        NewTaskFab(
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            onClick = onCreateTask,
        )
    }
}

@Composable
private fun NewTaskFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Forest)
                .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun DashboardTopBar(
    onBack: () -> Unit,
    onCreateTask: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Back",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onBack() }.padding(4.dp),
        )
        Text(
            text = "My Tasks",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "+ New",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onCreateTask() }.padding(4.dp),
        )
    }
}

@Composable
private fun DashboardLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Loading tasks...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DashboardEmpty(onCreateTask: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp),
        ) {
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Create your first verification task to get started",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onCreateTask() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "Create Task",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun DashboardError(
    message: String,
    onRetry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(40.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AlertCoral,
            )
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { onRetry() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "Retry",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    dashboard: ClientDashboardUiState.Dashboard,
    onTaskDetail: (ClientTaskSummary) -> Unit,
    onReviewEvidence: (ClientTaskSummary) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(bottom = 96.dp),
    ) {
        if (dashboard.totalEscrowHeldUsd > 0) {
            item { EscrowSummaryCard(totalUsd = dashboard.totalEscrowHeldUsd) }
        }

        taskSection(
            title = "Active",
            tasks = dashboard.activeTasks,
            badge = null,
            onTaskDetail = onTaskDetail,
            onReviewEvidence = onReviewEvidence,
        )

        taskSection(
            title = "Pending Review",
            tasks = dashboard.pendingReview,
            badge = if (dashboard.hasPendingReview) dashboard.pendingReview.size else null,
            onTaskDetail = onTaskDetail,
            onReviewEvidence = onReviewEvidence,
        )

        taskSection(
            title = "Drafts & Posted",
            tasks = dashboard.draftTasks,
            badge = null,
            onTaskDetail = onTaskDetail,
            onReviewEvidence = onReviewEvidence,
        )

        taskSection(
            title = "Completed",
            tasks = dashboard.completedTasks,
            badge = null,
            onTaskDetail = onTaskDetail,
            onReviewEvidence = onReviewEvidence,
        )
    }
}

private fun LazyListScope.taskSection(
    title: String,
    tasks: List<ClientTaskSummary>,
    badge: Int?,
    onTaskDetail: (ClientTaskSummary) -> Unit,
    onReviewEvidence: (ClientTaskSummary) -> Unit,
) {
    if (tasks.isEmpty()) return

    item {
        SectionHeader(title = title, badge = badge)
    }

    items(tasks, key = { it.id }) { task ->
        TaskCard(
            task = task,
            onTap = { onTaskDetail(task) },
            onReviewEvidence =
                if (task.status in REVIEW_STATUSES) {
                    { onReviewEvidence(task) }
                } else {
                    null
                },
        )
    }

    item { Spacer(Modifier.height(8.dp)) }
}

@Composable
private fun EscrowSummaryCard(totalUsd: Int) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Forest.copy(alpha = 0.12f))
                .border(1.dp, Forest.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Escrow Held",
                    style = MaterialTheme.typography.labelMedium,
                    color = Forest,
                )
                Text(
                    text = "$$totalUsd",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "Protected",
                style = MaterialTheme.typography.labelSmall,
                color = Forest,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    badge: Int?,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (badge != null) {
            Box(
                modifier =
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(SavannaGold),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$badge",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.background,
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: ClientTaskSummary,
    onTap: () -> Unit = {},
    onReviewEvidence: (() -> Unit)? = null,
) {
    val statusColor = task.status.statusColor()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    RoundedCornerShape(10.dp),
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onTap() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = task.type.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                )
                Text(
                    text = task.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusPill(label = task.status.displayLabel(), color = statusColor)
                Text(
                    text = "$${task.bountyUsd}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (onReviewEvidence != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SavannaGold)
                        .clickable { onReviewEvidence() }
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Review Evidence",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    color: Color,
) {
    val animatedColor by animateColorAsState(targetValue = color, animationSpec = tween(400))
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(animatedColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = animatedColor,
        )
    }
}

private fun TaskStatus.displayLabel(): String =
    when (this) {
        TaskStatus.DRAFT -> "Draft"
        TaskStatus.POSTED -> "Posted"
        TaskStatus.ESCROW_FUNDED -> "Escrow Funded"
        TaskStatus.INSPECTOR_MATCHED -> "Inspector Matched"
        TaskStatus.IN_PROGRESS -> "In Progress"
        TaskStatus.EVIDENCE_SUBMITTED -> "Evidence Submitted"
        TaskStatus.UNDER_REVIEW -> "Under Review"
        TaskStatus.ACCEPTED -> "Accepted"
        TaskStatus.DISPUTED -> "Disputed"
        TaskStatus.REVISION_REQUESTED -> "Revision Needed"
        TaskStatus.CANCELLED -> "Cancelled"
        TaskStatus.EXPIRED -> "Expired"
        TaskStatus.AUTO_ACCEPTED -> "Auto-Accepted"
    }

@Composable
private fun TaskStatus.statusColor(): Color =
    when (this) {
        TaskStatus.IN_PROGRESS,
        TaskStatus.INSPECTOR_MATCHED,
        TaskStatus.ESCROW_FUNDED,
        -> SignalGreen
        TaskStatus.EVIDENCE_SUBMITTED,
        TaskStatus.UNDER_REVIEW,
        TaskStatus.REVISION_REQUESTED,
        -> CautionAmber
        TaskStatus.ACCEPTED -> Forest
        TaskStatus.DISPUTED -> AlertCoral
        TaskStatus.DRAFT,
        TaskStatus.POSTED,
        TaskStatus.CANCELLED,
        TaskStatus.EXPIRED,
        TaskStatus.AUTO_ACCEPTED,
        -> Stone
    }

private fun makePreviewVm() = ClientDashboardViewModel(MockClientTaskRepository(), NoOpAnalyticsService)

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun ClientDashboardScreenDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = true) {
        ClientDashboardScreen(
            vm = makePreviewVm(),
            clientUid = "preview-uid",
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun ClientDashboardScreenLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        ClientDashboardScreen(
            vm = makePreviewVm(),
            clientUid = "preview-uid",
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskCardWithReviewButtonPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            TaskCard(
                task =
                    com.maku.eyeonsite.domain.ClientTaskSummary(
                        id = "t1",
                        type = com.maku.eyeonsite.domain.TaskType.CONSTRUCTION,
                        location = "East Legon, Accra",
                        description = "Verify floor slab and column placement on floors 3–5",
                        status = TaskStatus.EVIDENCE_SUBMITTED,
                        bountyUsd = 45,
                        createdAtMs = 1_748_000_000_000L,
                        inspectorName = "Kwame A.",
                    ),
                onReviewEvidence = {},
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun DashboardEmptyPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        DashboardEmpty(onCreateTask = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun DashboardErrorPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        DashboardError(message = "Failed to load dashboard", onRetry = {})
    }
}
