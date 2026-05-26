package com.maku.eyeonsite.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.maku.eyeonsite.domain.NearbyTask
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.platform.TaskMapView
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.CautionAmber
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen

private const val MAP_HEIGHT_DP = 320
private const val INSPECTOR_LAT = 5.5502
private const val INSPECTOR_LNG = -0.2174

@Composable
fun InspectorTaskMapScreen(
    vm: TaskMapViewModel,
    onAcceptTask: (NearbyTask) -> Unit,
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.loadNearbyTasks(INSPECTOR_LAT, INSPECTOR_LNG) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Task Map",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                when (val s = state) {
                    is TaskMapUiState.HasTasks ->
                        Text(
                            text = "${s.visibleTasks.size} task${if (s.visibleTasks.size == 1) "" else "s"} near you",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    else -> Unit
                }
            }

            // Refresh button
            Box(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { vm.refresh(INSPECTOR_LAT, INSPECTOR_LNG) }
                        .padding(10.dp),
            ) {
                Text("↻", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        when (val s = state) {
            TaskMapUiState.Loading -> LoadingContent()
            TaskMapUiState.Empty -> EmptyContent(onRefresh = { vm.refresh(INSPECTOR_LAT, INSPECTOR_LNG) })
            is TaskMapUiState.HasTasks ->
                HasTasksContent(
                    state = s,
                    onMarkerClick = { vm.selectTask(it.id) },
                    onFilterChange = { vm.setFilter(it) },
                    onAcceptTask = onAcceptTask,
                    onDismissSelection = { vm.selectTask(null) },
                )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(color = CautionAmber)
            Text(
                text = "Finding tasks near you…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyContent(onRefresh: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "📍",
                style = MaterialTheme.typography.displayMedium,
            )
            Text(
                text = "No tasks nearby",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "New tasks are posted regularly. Check back in a few minutes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onRefresh() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "Refresh",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun HasTasksContent(
    state: TaskMapUiState.HasTasks,
    onMarkerClick: (NearbyTask) -> Unit,
    onFilterChange: (TaskType?) -> Unit,
    onAcceptTask: (NearbyTask) -> Unit,
    onDismissSelection: () -> Unit,
) {
    Column {
        // Map
        TaskMapView(
            tasks = state.visibleTasks,
            selectedTaskId = state.selectedTaskId,
            isClustered = state.isClustered,
            modifier = Modifier.fillMaxWidth().height(MAP_HEIGHT_DP.dp),
            onMarkerClick = onMarkerClick,
        )

        // Type filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    label = "All",
                    selected = state.activeFilter == null,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { onFilterChange(null) },
                )
            }
            items(TaskType.entries) { type ->
                FilterChip(
                    label = type.label,
                    selected = state.activeFilter == type,
                    accentColor = typeAccent(type),
                    onClick = { onFilterChange(type) },
                )
            }
        }

        // Selected task detail card
        state.selectedTask?.let { task ->
            SelectedTaskCard(
                task = task,
                onAccept = { onAcceptTask(task) },
                onDismiss = onDismissSelection,
            )
        }

        // Task list
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.visibleTasks, key = { it.id }) { task ->
                TaskFeedCard(
                    task = task,
                    isSelected = task.id == state.selectedTaskId,
                    onTap = { onMarkerClick(task) },
                    onAccept = { onAcceptTask(task) },
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (selected) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    1.dp,
                    if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    RoundedCornerShape(20.dp),
                ).clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SelectedTaskCard(
    task: NearbyTask,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .border(1.5.dp, typeAccent(task.type), RoundedCornerShape(12.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TypeBadge(task.type)
            Text(
                text = "×",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onDismiss() }.padding(4.dp),
            )
        }
        Text(
            text = task.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${task.distanceKm} km away",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$${ task.bountyUsd}",
                style = MaterialTheme.typography.labelMedium,
                color = SignalGreen,
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onAccept() }
                    .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Accept Task",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun TaskFeedCard(
    task: NearbyTask,
    isSelected: Boolean,
    onTap: () -> Unit,
    onAccept: () -> Unit,
) {
    val accent = typeAccent(task.type)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(10.dp),
                ).clickable { onTap() }
                .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Type indicator dot
        Box(
            modifier =
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent),
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TypeBadge(task.type)
                Text(
                    text = "${task.distanceKm} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = task.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${task.postedMinutesAgo}m ago",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.End) {
            Text(
                text = "$${ task.bountyUsd}",
                style = MaterialTheme.typography.labelMedium,
                color = SignalGreen,
            )
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAccept() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Accept",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun TypeBadge(type: TaskType) {
    val accent = typeAccent(type)
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(accent.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = type.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = accent,
        )
    }
}

@Composable
private fun typeAccent(type: TaskType) =
    when (type) {
        TaskType.CONSTRUCTION -> CautionAmber
        TaskType.PROPERTY ->
            androidx.compose.ui.graphics
                .Color(0xFF4A90D9)
        TaskType.FARM -> Forest
        TaskType.BUSINESS -> SavannaGold
    }

// ── Previews ─────────────────────────────────────────────────────────────────

private fun makeTasks(
    count: Int,
    type: TaskType = TaskType.CONSTRUCTION,
): List<NearbyTask> =
    (1..count).map { i ->
        NearbyTask(
            id = "task-$i",
            type =
                if (i % 4 == 0) {
                    TaskType.FARM
                } else if (i % 3 == 0) {
                    TaskType.PROPERTY
                } else if (i % 2 == 0) {
                    TaskType.BUSINESS
                } else {
                    type
                },
            location = "East Legon, Accra",
            description = "Verify floor $i progress and confirm columns are in place",
            latitude = 5.5502 + i * 0.002,
            longitude = -0.2174 + i * 0.002,
            distanceKm = (i * 0.4),
            bountyUsd = 20 + i * 5,
            postedMinutesAgo = i * 3,
        )
    }

private class FakeRepo(
    private val tasks: List<NearbyTask>,
) : NearbyTaskRepository {
    override suspend fun fetchNearby(
        lat: Double,
        lng: Double,
    ) = tasks
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskMapScreenEmptyPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        InspectorTaskMapScreen(
            vm = TaskMapViewModel(FakeRepo(emptyList())).also { it.loadNearbyTasks(0.0, 0.0) },
            onAcceptTask = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskMapScreenFewTasksPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        InspectorTaskMapScreen(
            vm = TaskMapViewModel(FakeRepo(makeTasks(3))).also { it.loadNearbyTasks(0.0, 0.0) },
            onAcceptTask = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskMapScreenManyTasksPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        InspectorTaskMapScreen(
            vm = TaskMapViewModel(FakeRepo(makeTasks(15))).also { it.loadNearbyTasks(0.0, 0.0) },
            onAcceptTask = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskMapScreenLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        InspectorTaskMapScreen(
            vm = TaskMapViewModel(FakeRepo(makeTasks(5))).also { it.loadNearbyTasks(0.0, 0.0) },
            onAcceptTask = {},
        )
    }
}
