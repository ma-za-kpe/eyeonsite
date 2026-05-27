package com.maku.eyeonsite.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.domain.TaskStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface ClientTaskRepository {
    fun observeTasksForClient(clientUid: String): Flow<List<ClientTaskSummary>>
}

sealed class ClientDashboardUiState {
    data object Loading : ClientDashboardUiState()

    data object Empty : ClientDashboardUiState()

    data class Dashboard(
        val allTasks: List<ClientTaskSummary>,
        val isRefreshing: Boolean = false,
    ) : ClientDashboardUiState() {
        val activeTasks: List<ClientTaskSummary>
            get() = allTasks.filter { it.status in ACTIVE_STATUSES }

        val pendingReview: List<ClientTaskSummary>
            get() = allTasks.filter { it.status in REVIEW_STATUSES }

        val completedTasks: List<ClientTaskSummary>
            get() = allTasks.filter { it.status in COMPLETED_STATUSES }

        val draftTasks: List<ClientTaskSummary>
            get() = allTasks.filter { it.status in DRAFT_STATUSES }

        val totalEscrowHeldUsd: Int
            get() = allTasks.filter { it.status in ESCROW_STATUSES }.sumOf { it.bountyUsd }

        val hasPendingReview: Boolean
            get() = pendingReview.isNotEmpty()

        companion object {
            val ACTIVE_STATUSES = setOf(TaskStatus.IN_PROGRESS, TaskStatus.INSPECTOR_MATCHED, TaskStatus.ESCROW_FUNDED)
            val REVIEW_STATUSES = setOf(TaskStatus.EVIDENCE_SUBMITTED, TaskStatus.UNDER_REVIEW)
            val COMPLETED_STATUSES = setOf(TaskStatus.ACCEPTED, TaskStatus.DISPUTED, TaskStatus.REVISION_REQUESTED)
            val DRAFT_STATUSES = setOf(TaskStatus.DRAFT, TaskStatus.POSTED)
            val ESCROW_STATUSES = ACTIVE_STATUSES + REVIEW_STATUSES
        }
    }

    data class Error(
        val message: String,
    ) : ClientDashboardUiState()
}

class ClientDashboardViewModel(
    private val repository: ClientTaskRepository,
    private val analytics: AnalyticsService,
) : ViewModel() {
    private val _state = MutableStateFlow<ClientDashboardUiState>(ClientDashboardUiState.Loading)
    val state: StateFlow<ClientDashboardUiState> = _state.asStateFlow()

    private var observeJob: Job? = null

    @Suppress("TooGenericExceptionCaught")
    fun loadDashboard(clientUid: String) {
        observeJob?.cancel()
        _state.value = ClientDashboardUiState.Loading
        analytics.logEvent(name = "client_dashboard_opened", params = mapOf("client_uid" to clientUid))
        observeJob =
            viewModelScope.launch {
                try {
                    repository.observeTasksForClient(clientUid).collect { tasks ->
                        _state.value =
                            if (tasks.isEmpty()) {
                                ClientDashboardUiState.Empty
                            } else {
                                ClientDashboardUiState.Dashboard(tasks)
                            }
                    }
                } catch (e: Exception) {
                    _state.value = ClientDashboardUiState.Error(e.message ?: "Failed to load dashboard")
                }
            }
    }

    fun refresh(clientUid: String) = loadDashboard(clientUid)
}
