package com.maku.eyeonsite.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.domain.NearbyTask
import com.maku.eyeonsite.domain.TaskType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

sealed class TaskMapUiState {
    data object Loading : TaskMapUiState()

    data object Empty : TaskMapUiState()

    data class HasTasks(
        val tasks: List<NearbyTask>,
        val selectedTaskId: String? = null,
        val activeFilter: TaskType? = null,
    ) : TaskMapUiState() {
        val visibleTasks: List<NearbyTask>
            get() = if (activeFilter == null) tasks else tasks.filter { it.type == activeFilter }

        val selectedTask: NearbyTask?
            get() = selectedTaskId?.let { id -> tasks.find { it.id == id } }

        val isClustered: Boolean get() = tasks.size > CLUSTER_THRESHOLD

        companion object {
            const val CLUSTER_THRESHOLD = 10
        }
    }
}

interface NearbyTaskRepository {
    fun observeNearby(
        lat: Double,
        lng: Double,
    ): Flow<List<NearbyTask>>
}

class TaskMapViewModel(
    private val repository: NearbyTaskRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<TaskMapUiState>(TaskMapUiState.Loading)
    val state: StateFlow<TaskMapUiState> = _state.asStateFlow()

    private var observeJob: Job? = null

    fun loadNearbyTasks(
        lat: Double,
        lng: Double,
    ) {
        observeJob?.cancel()
        _state.value = TaskMapUiState.Loading
        observeJob =
            viewModelScope.launch {
                repository.observeNearby(lat, lng).collect { tasks ->
                    _state.value = if (tasks.isEmpty()) TaskMapUiState.Empty else TaskMapUiState.HasTasks(tasks)
                }
            }
    }

    fun selectTask(taskId: String?) {
        val current = _state.value as? TaskMapUiState.HasTasks ?: return
        _state.value = current.copy(selectedTaskId = taskId)
    }

    fun setFilter(type: TaskType?) {
        val current = _state.value as? TaskMapUiState.HasTasks ?: return
        _state.value = current.copy(activeFilter = type, selectedTaskId = null)
    }

    fun refresh(
        lat: Double,
        lng: Double,
    ) = loadNearbyTasks(lat, lng)
}
