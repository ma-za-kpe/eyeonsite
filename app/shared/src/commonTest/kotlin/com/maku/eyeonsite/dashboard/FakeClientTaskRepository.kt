package com.maku.eyeonsite.dashboard

import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.ui.dashboard.ClientTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeClientTaskRepository(
    private val tasks: List<ClientTaskSummary> = emptyList(),
    private val shouldFail: Boolean = false,
) : ClientTaskRepository {
    var observeCallCount = 0
        private set

    override fun observeTasksForClient(clientUid: String): Flow<List<ClientTaskSummary>> {
        observeCallCount++
        if (shouldFail) return flow { throw RuntimeException("Network error") }
        return flowOf(tasks)
    }
}
