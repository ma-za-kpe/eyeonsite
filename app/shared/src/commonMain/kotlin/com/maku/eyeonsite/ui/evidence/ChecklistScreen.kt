package com.maku.eyeonsite.ui.evidence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.domain.ChecklistAnswer
import com.maku.eyeonsite.domain.ChecklistItem
import com.maku.eyeonsite.domain.ChecklistItemType
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.ui.task.TopBar
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SignalGreen

@Composable
fun ChecklistScreen(
    vm: ChecklistViewModel,
    task: VerificationTask,
    onBack: () -> Unit,
    onDone: (List<ChecklistAnswer>) -> Unit,
) {
    LaunchedEffect(task.id) { vm.load(task) }

    val state by vm.state.collectAsState()

    LaunchedEffect(state) {
        val s = state
        if (s is ChecklistUiState.Done) onDone(s.answers)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        TopBar(title = "Checklist — ${task.type.label}", onBack = onBack)

        when (val s = state) {
            is ChecklistUiState.Loading ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Preparing checklist...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

            is ChecklistUiState.Filling ->
                FillingContent(
                    state = s,
                    onYesNo = { id, yes -> vm.answerYesNo(id, yes) },
                    onText = { id, text -> vm.answerText(id, text) },
                    onSubmit = { vm.submit() },
                )

            is ChecklistUiState.Done ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Checklist complete",
                        style = MaterialTheme.typography.titleMedium,
                        color = SignalGreen,
                    )
                }
        }
    }
}

@Composable
private fun FillingContent(
    state: ChecklistUiState.Filling,
    onYesNo: (String, Boolean) -> Unit,
    onText: (String, String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = state.task.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        state.items.forEachIndexed { index, item ->
            val answer = state.answers[item.id]
            val showError = state.showValidation && item.required && answer?.isAnswered != true

            ChecklistItemRow(
                index = index + 1,
                item = item,
                answer = answer,
                showError = showError,
                onYesNo = { yes -> onYesNo(item.id, yes) },
                onText = { text -> onText(item.id, text) },
            )
        }

        if (state.showValidation && !state.canSubmit) {
            Text(
                text = "${state.unansweredCount} required question(s) remaining",
                style = MaterialTheme.typography.bodySmall,
                color = AlertCoral,
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Forest)
                    .clickable { onSubmit() }
                    .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Submit Checklist",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ChecklistItemRow(
    index: Int,
    item: ChecklistItem,
    answer: ChecklistAnswer?,
    showError: Boolean,
    onYesNo: (Boolean) -> Unit,
    onText: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    if (showError) AlertCoral else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    RoundedCornerShape(10.dp),
                ).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!item.required) {
                    Text(
                        text = "Optional",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        when (item.type) {
            ChecklistItemType.YES_NO ->
                YesNoToggle(
                    selected = answer?.boolValue,
                    onSelect = onYesNo,
                )

            ChecklistItemType.TEXT ->
                TextAnswerField(
                    value = answer?.textValue ?: "",
                    onValueChange = onText,
                )
        }
    }
}

@Composable
private fun YesNoToggle(
    selected: Boolean?,
    onSelect: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ToggleButton(
            label = "Yes",
            active = selected == true,
            activeColor = SignalGreen,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(true) },
        )
        ToggleButton(
            label = "No",
            active = selected == false,
            activeColor = AlertCoral,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(false) },
        )
    }
}

@Composable
private fun ToggleButton(
    label: String,
    active: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (active) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ).border(
                    1.dp,
                    if (active) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(6.dp),
                ).clickable { onClick() }
                .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TextAnswerField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = false,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { inner ->
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            RoundedCornerShape(6.dp),
                        ).padding(12.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Write your observations here...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
                inner()
            }
        },
    )
}

private fun makePreviewVm() = ChecklistViewModel(NoOpAnalyticsService)

private fun makePreviewTask(type: TaskType) =
    VerificationTask(
        id = "preview-task",
        type = type,
        location = "East Legon, Accra",
        description = "Verify floor slab and column placement on floors 3–5",
    )

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun ChecklistScreenConstructionPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        ChecklistScreen(
            vm = makePreviewVm(),
            task = makePreviewTask(TaskType.CONSTRUCTION),
            onBack = {},
            onDone = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun ChecklistScreenFarmDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        ChecklistScreen(
            vm = makePreviewVm(),
            task = makePreviewTask(TaskType.FARM),
            onBack = {},
            onDone = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun ChecklistYesNoTogglePreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        Column(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YesNoToggle(selected = null, onSelect = {})
            YesNoToggle(selected = true, onSelect = {})
            YesNoToggle(selected = false, onSelect = {})
        }
    }
}
