package com.maku.eyeonsite.ui.task

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.data.NoOpTaskRepository
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.platform.MapLocationPicker
import com.maku.eyeonsite.platform.NoOpGeocoderService
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold

@Composable
fun TaskCreationScreen(
    vm: CreateTaskViewModel,
    onBack: () -> Unit,
    onTaskCreated: (VerificationTask) -> Unit,
) {
    LaunchedEffect(Unit) { vm.reset() }

    val state by vm.state.collectAsState()

    LaunchedEffect(state) {
        val s = state
        if (s is CreateTaskUiState.Matched) {
            onTaskCreated(s.task)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        TopBar(title = "New Verification Task", onBack = onBack)

        when (val s = state) {
            is CreateTaskUiState.Form ->
                FormContent(
                    form = s,
                    onTypeSelected = { vm.setTaskType(it) },
                    onLocationChange = { vm.setLocation(it) },
                    onLocationCoordsChange = { lat, lng -> vm.setLocationCoords(lat, lng) },
                    onDescriptionChange = { vm.setDescription(it) },
                    onBountyChange = { vm.setBounty(it) },
                    onPost = { vm.post() },
                )

            is CreateTaskUiState.FindingInspector ->
                FindingInspectorContent(task = s.task)

            is CreateTaskUiState.Matched ->
                FindingInspectorContent(task = s.task)

            is CreateTaskUiState.Error ->
                ErrorContent(message = s.message, onRetry = { vm.reset() })
        }
    }
}

@Composable
private fun FormContent(
    form: CreateTaskUiState.Form,
    onTypeSelected: (TaskType) -> Unit,
    onLocationChange: (String) -> Unit,
    onLocationCoordsChange: (Double, Double) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBountyChange: (Int) -> Unit,
    onPost: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        TaskTypeSection(
            selected = form.taskType,
            isError = form.typeError,
            onTypeSelected = onTypeSelected,
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TaskField(
                label = "Location / Address",
                value = form.location,
                hint = "e.g. East Legon, Accra",
                isError = form.locationError,
                errorMessage = "Location is required",
                onValueChange = onLocationChange,
            )

            MapLocationPicker(
                initialLat = form.locationLat,
                initialLng = form.locationLng,
                onLocationPicked = { lat, lng -> onLocationCoordsChange(lat, lng) },
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )
        }

        TaskField(
            label = "What should the inspector verify?",
            value = form.description,
            hint = "e.g. Confirm the third floor slab has been poured and columns are in place",
            isError = form.descriptionError,
            errorMessage = "Description is required",
            singleLine = false,
            onValueChange = onDescriptionChange,
        )

        BountySection(
            bountyUsd = form.bountyUsd,
            onDecrease = { onBountyChange(form.bountyUsd - CreateTaskViewModel.BOUNTY_STEP) },
            onIncrease = { onBountyChange(form.bountyUsd + CreateTaskViewModel.BOUNTY_STEP) },
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onPost() }
                    .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Find Inspectors",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TaskTypeSection(
    selected: TaskType?,
    isError: Boolean,
    onTypeSelected: (TaskType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(text = "What needs verification?", isError = isError)
        if (isError) ErrorCaption("Please select a task type")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TaskTypeCard(
                type = TaskType.CONSTRUCTION,
                selected = selected == TaskType.CONSTRUCTION,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(TaskType.CONSTRUCTION) },
            )
            TaskTypeCard(
                type = TaskType.PROPERTY,
                selected = selected == TaskType.PROPERTY,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(TaskType.PROPERTY) },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TaskTypeCard(
                type = TaskType.FARM,
                selected = selected == TaskType.FARM,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(TaskType.FARM) },
            )
            TaskTypeCard(
                type = TaskType.BUSINESS,
                selected = selected == TaskType.BUSINESS,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected(TaskType.BUSINESS) },
            )
        }
    }
}

@Composable
private fun BountySection(
    bountyUsd: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(text = "Bounty (USD)")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BountyStepButton(
                label = "−",
                enabled = bountyUsd > CreateTaskViewModel.BOUNTY_MIN,
                onClick = onDecrease,
            )
            Text(
                text = "$$bountyUsd",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            BountyStepButton(
                label = "+",
                enabled = bountyUsd < CreateTaskViewModel.BOUNTY_MAX,
                onClick = onIncrease,
            )
        }
        val min = CreateTaskViewModel.BOUNTY_MIN
        val max = CreateTaskViewModel.BOUNTY_MAX
        val step = CreateTaskViewModel.BOUNTY_STEP
        Text(
            text = "Min \$$min · Max \$$max · Step \$$step",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BountyStepButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) {
                        SavannaGold.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                ).then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) SavannaGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
    }
}

@Composable
private fun FindingInspectorContent(task: VerificationTask) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Forest.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text =
                        task.type.label
                            .first()
                            .toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Forest,
                )
            }
            Text(
                text = "Finding Inspectors...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Pinging nearby inspectors for ${task.type.label.lowercase()} in ${task.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(40.dp),
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = AlertCoral)
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { onRetry() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "Try Again",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun TaskTypeCard(
    type: TaskType,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val accentColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (selected) {
                        accentColor.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                ).border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color =
                        if (selected) {
                            accentColor
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                    shape = RoundedCornerShape(10.dp),
                ).clickable { onClick() }
                .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = type.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = type.shortDescription,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TaskField(
    label: String,
    value: String,
    hint: String,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel(text = label, isError = isError)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
            decorationBox = { inner ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color =
                                    if (isError) {
                                        AlertCoral
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    },
                                shape = RoundedCornerShape(8.dp),
                            ).padding(horizontal = 14.dp, vertical = 14.dp),
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                    inner()
                }
            },
        )
        if (isError) ErrorCaption(errorMessage)
    }
}

@Composable
private fun SectionLabel(
    text: String,
    isError: Boolean = false,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (isError) AlertCoral else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ErrorCaption(message: String) {
    Text(text = message, style = MaterialTheme.typography.bodySmall, color = AlertCoral)
}

@Composable
internal fun TopBar(
    title: String,
    onBack: () -> Unit,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { onBack() }.padding(8.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        )
    }
}

private fun makePreviewVm() = CreateTaskViewModel(NoOpAnalyticsService, NoOpGeocoderService, NoOpTaskRepository())

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskCreationFormLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        TaskCreationScreen(vm = makePreviewVm(), onBack = {}, onTaskCreated = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskCreationFormDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = true) {
        TaskCreationScreen(vm = makePreviewVm(), onBack = {}, onTaskCreated = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskCreationFindingInspectorPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        FindingInspectorContent(
            task =
                VerificationTask(
                    type = TaskType.CONSTRUCTION,
                    location = "East Legon, Accra",
                    description = "Verify floor slab",
                    bountyUsd = 45,
                ),
        )
    }
}
