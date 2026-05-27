package com.maku.eyeonsite.ui.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.data.EvidenceRepository
import com.maku.eyeonsite.domain.EvidencePackage
import com.maku.eyeonsite.domain.EvidencePhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class EvidenceCaptureUiState(
    val taskId: String = "",
    val photos: List<EvidencePhoto> = emptyList(),
    val pendingBytes: Map<String, ByteArray> = emptyMap(),
    val notes: String = "",
    val isUploading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
    val photoQualityWarning: String? = null,
) {
    val canSubmit: Boolean get() = photos.isNotEmpty() && !isUploading && !isSubmitted
    val canAddMore: Boolean get() = photos.size < EvidencePackage.MAX_PHOTOS && !isUploading
}

@OptIn(ExperimentalUuidApi::class)
class EvidenceCaptureViewModel(
    private val repository: EvidenceRepository,
    private val analyzer: PhotoQualityAnalyzer,
    private val analytics: AnalyticsService,
) : ViewModel() {
    private val _state = MutableStateFlow(EvidenceCaptureUiState())
    val state: StateFlow<EvidenceCaptureUiState> = _state.asStateFlow()

    fun initTask(taskId: String) {
        _state.value = EvidenceCaptureUiState(taskId = taskId)
        analytics.logEvent("capture_started", mapOf("task_id" to taskId))
    }

    fun addPhoto(
        bytes: ByteArray,
        lat: Double,
        lng: Double,
    ) {
        val current = _state.value
        if (!current.canAddMore) return

        val photo =
            EvidencePhoto(
                id = Uuid.random().toString(),
                taskId = current.taskId,
                latitude = lat,
                longitude = lng,
                capturedAtMs =
                    kotlinx.datetime.Clock.System
                        .now()
                        .toEpochMilliseconds(),
            )

        _state.value =
            current.copy(
                photos = current.photos + photo,
                pendingBytes = current.pendingBytes + (photo.id to bytes),
                error = null,
                photoQualityWarning = null,
            )
        analytics.logEvent("photo_captured", mapOf("task_id" to current.taskId, "count" to (current.photos.size + 1).toString()))

        viewModelScope.launch {
            val result = analyzer.analyze(bytes)
            val warning = result.warningMessage
            if (warning != null) {
                _state.value = _state.value.copy(photoQualityWarning = warning)
            }
        }
    }

    fun dismissQualityWarning() {
        _state.value = _state.value.copy(photoQualityWarning = null)
    }

    fun setNotes(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }

    fun removePhoto(photoId: String) {
        val current = _state.value
        _state.value =
            current.copy(
                photos = current.photos.filter { it.id != photoId },
                pendingBytes = current.pendingBytes - photoId,
            )
    }

    fun submitEvidence() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.value = current.copy(isUploading = true, error = null)

        viewModelScope.launch {
            repository
                .submitEvidence(
                    taskId = current.taskId,
                    photos = current.photos,
                    pendingBytes = current.pendingBytes,
                    notes = current.notes,
                ).fold(
                    onSuccess = {
                        _state.value =
                            _state.value.copy(
                                pendingBytes = emptyMap(),
                                isUploading = false,
                                isSubmitted = true,
                            )
                        analytics.logEvent(
                            "evidence_submitted",
                            mapOf("task_id" to current.taskId, "photo_count" to current.photos.size.toString()),
                        )
                    },
                    onFailure = { err ->
                        _state.value =
                            _state.value.copy(
                                isUploading = false,
                                error = err.message ?: "Failed to submit evidence. Please try again.",
                            )
                    },
                )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
