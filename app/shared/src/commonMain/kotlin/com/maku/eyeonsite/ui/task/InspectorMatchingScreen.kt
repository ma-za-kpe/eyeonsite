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
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White

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
                .background(DeepForest),
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
                color = Stone,
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
                .background(Forest.copy(alpha = 0.2f))
                .border(1.dp, Forest, RoundedCornerShape(10.dp))
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
                color = Stone,
            )
        }
        Text(
            text = task.description,
            style = MaterialTheme.typography.bodyMedium,
            color = White,
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
                .background(White.copy(alpha = 0.05f))
                .border(1.dp, White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(AVATAR_SIZE_DP.dp)
                    .clip(CircleShape)
                    .background(Forest),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = inspector.initials,
                style = MaterialTheme.typography.labelMedium,
                color = White,
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
                    color = White,
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
                    color = Stone,
                )
                Text(
                    text = inspector.responseTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Stone,
                )
            }
            Text(
                text = "${inspector.tasksCompleted} tasks completed",
                style = MaterialTheme.typography.bodySmall,
                color = Stone,
            )
        }

        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Forest)
                    .clickable { }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Assign",
                style = MaterialTheme.typography.labelMedium,
                color = White,
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun InspectorMatchingScreenPreview() {
    EyeOnSiteTheme {
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
fun InspectorMatchingScreenFarmPreview() {
    EyeOnSiteTheme {
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
