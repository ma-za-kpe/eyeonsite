package com.maku.eyeonsite.dashboard

import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.ui.dashboard.ClientTaskRepository

class FakeClientTaskRepository(
    private val tasks: List<ClientTaskSummary> = emptyList(),
    private val shouldFail: Boolean = false,
) : ClientTaskRepository {
    var fetchCallCount = 0
        private set

    override suspend fun fetchTasksForClient(clientUid: String): List<ClientTaskSummary> {
        fetchCallCount++
        if (shouldFail) throw RuntimeException("Network error")
        return tasks
    }
}
