package com.maku.eyeonsite.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.domain.TaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InspectorHomeState(
    val isAvailable: Boolean = true,
    val earningsThisWeekUsd: Int = 185,
    val completedTasksCount: Int = 12,
)

interface InspectorAvailabilityRepository {
    suspend fun setAvailability(
        inspectorUid: String,
        available: Boolean,
    )
}

class InspectorHomeViewModel(
    private val analytics: AnalyticsService,
    private val availabilityRepo: InspectorAvailabilityRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(InspectorHomeState())
    val state: StateFlow<InspectorHomeState> = _state.asStateFlow()

    fun toggleAvailability(inspectorUid: String) {
        val newValue = !_state.value.isAvailable
        _state.value = _state.value.copy(isAvailable = newValue)
        viewModelScope.launch {
            availabilityRepo.setAvailability(inspectorUid, newValue)
        }
    }

    fun logTaskAccepted(
        taskId: String,
        type: TaskType,
        bountyUsd: Int,
    ) {
        analytics.logEvent(
            name = "task_accepted",
            params =
                mapOf(
                    "task_id" to taskId,
                    "task_type" to type.name,
                    "bounty_usd" to bountyUsd.toString(),
                ),
        )
    }
}
