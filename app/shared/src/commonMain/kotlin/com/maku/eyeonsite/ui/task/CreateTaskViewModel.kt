package com.maku.eyeonsite.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.data.TaskRepository
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.platform.GeocoderService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private const val DEFAULT_TASK_LAT = 5.5502 // Accra CBD
private const val DEFAULT_TASK_LNG = -0.2174

sealed class CreateTaskUiState {
    data class Form(
        val taskType: TaskType? = null,
        val location: String = "",
        val locationLat: Double = DEFAULT_TASK_LAT,
        val locationLng: Double = DEFAULT_TASK_LNG,
        val description: String = "",
        val bountyUsd: Int = 30,
        val submitted: Boolean = false,
    ) : CreateTaskUiState() {
        val typeError: Boolean get() = submitted && taskType == null
        val locationError: Boolean get() = submitted && location.isBlank()
        val descriptionError: Boolean get() = submitted && description.isBlank()
        val canPost: Boolean
            get() = taskType != null && location.isNotBlank() && description.isNotBlank()
    }

    data class FindingInspector(
        val task: VerificationTask,
    ) : CreateTaskUiState()

    data class Matched(
        val task: VerificationTask,
    ) : CreateTaskUiState()

    data class Error(
        val message: String,
    ) : CreateTaskUiState()
}

class CreateTaskViewModel(
    private val analytics: AnalyticsService,
    private val geocoder: GeocoderService,
    private val repository: TaskRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<CreateTaskUiState>(CreateTaskUiState.Form())
    val state: StateFlow<CreateTaskUiState> = _state.asStateFlow()

    fun setTaskType(type: TaskType) {
        val s = _state.value as? CreateTaskUiState.Form ?: return
        _state.value = s.copy(taskType = type)
    }

    fun setLocation(location: String) {
        val s = _state.value as? CreateTaskUiState.Form ?: return
        _state.value = s.copy(location = location)
    }

    fun setLocationCoords(
        lat: Double,
        lng: Double,
    ) {
        val s = _state.value as? CreateTaskUiState.Form ?: return
        _state.value = s.copy(locationLat = lat, locationLng = lng)
        // Auto-fill location text from geocoding only when the field is blank
        if (s.location.isBlank()) {
            viewModelScope.launch {
                val address = geocoder.reverseGeocode(lat, lng) ?: return@launch
                val current = _state.value as? CreateTaskUiState.Form ?: return@launch
                if (current.location.isBlank()) {
                    _state.value = current.copy(location = address)
                }
            }
        }
    }

    fun setDescription(description: String) {
        val s = _state.value as? CreateTaskUiState.Form ?: return
        _state.value = s.copy(description = description)
    }

    fun setBounty(bountyUsd: Int) {
        val s = _state.value as? CreateTaskUiState.Form ?: return
        _state.value = s.copy(bountyUsd = bountyUsd.coerceIn(BOUNTY_MIN, BOUNTY_MAX))
    }

    @Suppress("TooGenericExceptionCaught")
    fun post() {
        val s = _state.value as? CreateTaskUiState.Form ?: return
        val validated = s.copy(submitted = true)
        _state.value = validated
        if (!validated.canPost) return

        analytics.logEvent(
            name = "task_posted",
            params =
                mapOf(
                    "type" to s.taskType!!.name,
                    "bounty_usd" to s.bountyUsd.toString(),
                ),
        )

        val task =
            VerificationTask(
                type = s.taskType,
                location = s.location.trim(),
                locationLat = s.locationLat,
                locationLng = s.locationLng,
                description = s.description.trim(),
                bountyUsd = s.bountyUsd,
                status = TaskStatus.POSTED,
            )

        _state.value = CreateTaskUiState.FindingInspector(task)

        viewModelScope.launch {
            try {
                val taskId = repository.postTask(task)
                repository
                    .observeTask(taskId)
                    .catch { _state.value = CreateTaskUiState.Error("Connection error. Please try again.") }
                    .collect { status ->
                        when (status) {
                            TaskStatus.INSPECTOR_MATCHED ->
                                _state.value = CreateTaskUiState.Matched(task)
                            TaskStatus.EXPIRED ->
                                _state.value =
                                    CreateTaskUiState.Error("No inspectors available in your area. Please try again.")
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                _state.value =
                    CreateTaskUiState.Error(e.message ?: "Failed to post task. Please check your connection.")
            }
        }
    }

    fun reset() {
        _state.value = CreateTaskUiState.Form()
    }

    companion object {
        const val BOUNTY_MIN = 5
        const val BOUNTY_MAX = 200
        const val BOUNTY_STEP = 5
        const val DEFAULT_LAT = 5.5502 // Accra CBD
        const val DEFAULT_LNG = -0.2174
    }
}
