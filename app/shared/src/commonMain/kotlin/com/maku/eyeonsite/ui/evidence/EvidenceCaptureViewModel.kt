package com.maku.eyeonsite.ui.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.data.EvidenceRepository
import com.maku.eyeonsite.domain.EvidencePackage
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.domain.UploadStatus
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
) {
    val canSubmit: Boolean get() = photos.isNotEmpty() && !isUploading && !isSubmitted
    val canAddMore: Boolean get() = photos.size < EvidencePackage.MAX_PHOTOS && !isUploading
    val uploadedCount: Int get() = photos.count { it.uploadStatus == UploadStatus.UPLOADED }
}

@OptIn(ExperimentalUuidApi::class)
class EvidenceCaptureViewModel(
    private val uploader: EvidenceUploader,
    private val repository: EvidenceRepository,
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
            )
        analytics.logEvent("photo_captured", mapOf("task_id" to current.taskId, "count" to (current.photos.size + 1).toString()))
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
            val updatedPhotos = current.photos.toMutableList()
            var failed = false

            for ((index, photo) in current.photos.withIndex()) {
                val bytes = current.pendingBytes[photo.id] ?: continue
                val uploading = photo.copy(uploadStatus = UploadStatus.UPLOADING)
                updatedPhotos[index] = uploading
                _state.value = _state.value.copy(photos = updatedPhotos.toList())

                uploader.upload(uploading, bytes).fold(
                    onSuccess = { url ->
                        updatedPhotos[index] = uploading.copy(uploadStatus = UploadStatus.UPLOADED, storageUrl = url)
                    },
                    onFailure = { err ->
                        updatedPhotos[index] = uploading.copy(uploadStatus = UploadStatus.FAILED)
                        failed = true
                        _state.value =
                            _state.value.copy(
                                photos = updatedPhotos.toList(),
                                isUploading = false,
                                error = err.message ?: "Upload failed",
                            )
                    },
                )

                if (failed) return@launch
            }

            val uploadedPhotos = updatedPhotos.toList()
            _state.value = _state.value.copy(photos = uploadedPhotos)

            repository
                .submitEvidence(current.taskId, uploadedPhotos, current.notes)
                .fold(
                    onSuccess = {
                        _state.value =
                            _state.value.copy(
                                pendingBytes = emptyMap(),
                                isUploading = false,
                                isSubmitted = true,
                            )
                        analytics.logEvent(
                            "evidence_submitted",
                            mapOf("task_id" to current.taskId, "photo_count" to uploadedPhotos.size.toString()),
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
