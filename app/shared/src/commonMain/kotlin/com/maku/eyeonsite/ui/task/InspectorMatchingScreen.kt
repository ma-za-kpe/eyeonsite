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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen

private const val AVATAR_SIZE_DP = 48
private const val TASK_DESCRIPTION_MAX_LINES = 2

private data class MockInspector(
    val name: String,
    val initials: String,
    val rating: String,
    val distance: String,
    val responseTime: String,
    val tasksCompleted: Int,
)

@Suppress("MagicNumber")
private val MOCK_INSPECTORS =
    listOf(
        MockInspector("Kofi Asante", "KA", "4.9", "0.8 km", "< 30 min", 47),
        MockInspector("Ama Owusu", "AO", "4.7", "1.4 km", "< 1 hr", 31),
        MockInspector("Kweku Mensah", "KM", "4.8", "2.1 km", "< 2 hr", 58),
    )

@Composable
fun InspectorMatchingScreen(
    task: VerificationTask,
    onBack: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        TopBar(title = "Available Inspectors", onBack = onBack)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TaskSummaryCard(task)

            Text(
                text = "Inspectors near ${task.location}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MOCK_INSPECTORS.forEach { inspector ->
                InspectorCard(inspector)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TaskSummaryCard(task: VerificationTask) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SavannaGold.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = task.type.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SavannaGold,
                )
            }
            Text(
                text = task.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = task.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = TASK_DESCRIPTION_MAX_LINES,
        )
    }
}

@Composable
private fun InspectorCard(inspector: MockInspector) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(AVATAR_SIZE_DP.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = inspector.initials,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = inspector.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SignalGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "Verified",
                        style = MaterialTheme.typography.labelSmall,
                        color = SignalGreen,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "★ ${inspector.rating}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SavannaGold,
                )
                Text(
                    text = inspector.distance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = inspector.responseTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${inspector.tasksCompleted} tasks completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Assign",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun InspectorMatchingScreenClientLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = false) {
        InspectorMatchingScreen(
            task =
                VerificationTask(
                    type = TaskType.CONSTRUCTION,
                    location = "East Legon, Accra",
                    description = "Confirm the third floor slab has been poured and columns are in place",
                ),
            onBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun InspectorMatchingScreenClientDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.CLIENT, darkTheme = true) {
        InspectorMatchingScreen(
            task =
                VerificationTask(
                    type = TaskType.CONSTRUCTION,
                    location = "East Legon, Accra",
                    description = "Confirm the third floor slab has been poured and columns are in place",
                ),
            onBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun InspectorMatchingScreenInspectorDarkPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        InspectorMatchingScreen(
            task =
                VerificationTask(
                    type = TaskType.FARM,
                    location = "Kumasi, Ghana",
                    description = "Verify 5-acre cocoa farm — confirm maturity stage and tree count",
                ),
            onBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun InspectorMatchingScreenInspectorLightPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        InspectorMatchingScreen(
            task =
                VerificationTask(
                    type = TaskType.FARM,
                    location = "Kumasi, Ghana",
                    description = "Verify 5-acre cocoa farm — confirm maturity stage and tree count",
                ),
            onBack = {},
        )
    }
}
