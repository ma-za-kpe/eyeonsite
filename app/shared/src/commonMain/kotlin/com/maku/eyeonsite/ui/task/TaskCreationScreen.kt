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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme

@Composable
fun TaskCreationScreen(
    onBack: () -> Unit,
    onTaskCreated: (VerificationTask) -> Unit,
) {
    var selectedType by remember { mutableStateOf<TaskType?>(null) }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val typeError = submitted && selectedType == null
    val locationError = submitted && location.isBlank()
    val descriptionError = submitted && description.isBlank()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        TopBar(title = "New Verification Task", onBack = onBack)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            // Task type
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel(
                    text = "What needs verification?",
                    isError = typeError,
                )
                if (typeError) {
                    ErrorCaption("Please select a task type")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TaskTypeCard(
                        type = TaskType.CONSTRUCTION,
                        selected = selectedType == TaskType.CONSTRUCTION,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = TaskType.CONSTRUCTION },
                    )
                    TaskTypeCard(
                        type = TaskType.PROPERTY,
                        selected = selectedType == TaskType.PROPERTY,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = TaskType.PROPERTY },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TaskTypeCard(
                        type = TaskType.FARM,
                        selected = selectedType == TaskType.FARM,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = TaskType.FARM },
                    )
                    TaskTypeCard(
                        type = TaskType.BUSINESS,
                        selected = selectedType == TaskType.BUSINESS,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = TaskType.BUSINESS },
                    )
                }
            }

            // Location
            TaskField(
                label = "Location / Address",
                value = location,
                hint = "e.g. East Legon, Accra",
                isError = locationError,
                errorMessage = "Location is required",
                onValueChange = { location = it },
            )

            // Description
            TaskField(
                label = "What should the inspector verify?",
                value = description,
                hint = "e.g. Confirm the third floor slab has been poured and columns are in place",
                isError = descriptionError,
                errorMessage = "Description is required",
                singleLine = false,
                onValueChange = { description = it },
            )

            // Submit
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            submitted = true
                            val type = selectedType
                            if (type != null && location.isNotBlank() && description.isNotBlank()) {
                                onTaskCreated(
                                    VerificationTask(
                                        type = type,
                                        location = location.trim(),
                                        description = description.trim(),
                                    ),
                                )
                            }
                        }.padding(vertical = 18.dp),
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
                .background(if (selected) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
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
                                color = if (isError) AlertCoral else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
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
        if (isError) {
            ErrorCaption(errorMessage)
        }
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
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = AlertCoral,
    )
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
                modifier =
                    Modifier
                        .clickable { onBack() }
                        .padding(8.dp),
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

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskCreationScreenClientLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        TaskCreationScreen(onBack = {}, onTaskCreated = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskCreationScreenClientDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = true) {
        TaskCreationScreen(onBack = {}, onTaskCreated = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskCreationScreenInspectorLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        TaskCreationScreen(onBack = {}, onTaskCreated = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TaskCreationScreenInspectorDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        TaskCreationScreen(onBack = {}, onTaskCreated = {})
    }
}
