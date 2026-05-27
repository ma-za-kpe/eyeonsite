package com.maku.eyeonsite.data

import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.dashboard.ClientTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockClientTaskRepository : ClientTaskRepository {
    override fun observeTasksForClient(clientUid: String): Flow<List<ClientTaskSummary>> = flowOf(MOCK_TASKS)
}

private val MOCK_TASKS =
    listOf(
        ClientTaskSummary(
            id = "task-001",
            type = TaskType.CONSTRUCTION,
            location = "East Legon, Accra",
            description = "Verify floor slab and column placement on floors 3–5",
            status = TaskStatus.EVIDENCE_SUBMITTED,
            bountyUsd = 45,
            createdAtMs = 1_747_900_000_000L,
            inspectorName = "Kwame A.",
        ),
        ClientTaskSummary(
            id = "task-002",
            type = TaskType.PROPERTY,
            location = "Airport Hills, Accra",
            description = "Confirm boundary fencing and gate installation",
            status = TaskStatus.IN_PROGRESS,
            bountyUsd = 30,
            createdAtMs = 1_747_800_000_000L,
            inspectorName = "Ama B.",
        ),
        ClientTaskSummary(
            id = "task-003",
            type = TaskType.FARM,
            location = "Dodowa, Greater Accra",
            description = "Photograph cassava crop coverage and irrigation channels",
            status = TaskStatus.ACCEPTED,
            bountyUsd = 55,
            createdAtMs = 1_747_700_000_000L,
            inspectorName = "Kofi C.",
        ),
        ClientTaskSummary(
            id = "task-004",
            type = TaskType.BUSINESS,
            location = "Osu, Accra",
            description = "Verify shop is open, signage visible, and stock present",
            status = TaskStatus.POSTED,
            bountyUsd = 25,
            createdAtMs = 1_747_600_000_000L,
        ),
        ClientTaskSummary(
            id = "task-005",
            type = TaskType.CONSTRUCTION,
            location = "Tema, Greater Accra",
            description = "Inspect roofing sheets and verify plumbing rough-in",
            status = TaskStatus.INSPECTOR_MATCHED,
            bountyUsd = 60,
            createdAtMs = 1_747_500_000_000L,
            inspectorName = "Yaw D.",
        ),
    )
