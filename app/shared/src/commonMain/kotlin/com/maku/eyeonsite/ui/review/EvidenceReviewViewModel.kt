package com.maku.eyeonsite.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.domain.ReviewDecision
import com.maku.eyeonsite.domain.TaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EvidenceReviewUiState {
    data object Loading : EvidenceReviewUiState()

    data class Reviewing(
        val taskId: String,
        val taskDescription: String,
        val taskType: TaskType,
        val photos: List<EvidencePhoto>,
        val decision: ReviewDecision? = null,
        val selectedPhotoIndex: Int = 0,
        val isSubmitting: Boolean = false,
        val notes: String = "",
    ) : EvidenceReviewUiState() {
        val hasPhotos: Boolean get() = photos.isNotEmpty()
        val canSubmit: Boolean get() = decision != null && !isSubmitting
        val selectedPhoto: EvidencePhoto? get() = photos.getOrNull(selectedPhotoIndex)
        val photoCount: Int get() = photos.size
    }

    data class Submitted(
        val decision: ReviewDecision,
    ) : EvidenceReviewUiState()

    data class Error(
        val message: String,
        val reviewing: Reviewing,
    ) : EvidenceReviewUiState()
}

class EvidenceReviewViewModel(
    private val reviewer: EvidenceReviewer,
) : ViewModel() {
    private val _state = MutableStateFlow<EvidenceReviewUiState>(EvidenceReviewUiState.Loading)
    val state: StateFlow<EvidenceReviewUiState> = _state.asStateFlow()

    fun loadEvidence(
        taskId: String,
        taskDescription: String,
        taskType: TaskType,
        photos: List<EvidencePhoto>,
    ) {
        _state.value =
            EvidenceReviewUiState.Reviewing(
                taskId = taskId,
                taskDescription = taskDescription,
                taskType = taskType,
                photos = photos,
            )
    }

    fun selectPhoto(index: Int) {
        val current = _state.value as? EvidenceReviewUiState.Reviewing ?: return
        val clamped = index.coerceIn(0, (current.photos.size - 1).coerceAtLeast(0))
        _state.value = current.copy(selectedPhotoIndex = clamped)
    }

    fun setDecision(decision: ReviewDecision?) {
        val current = _state.value as? EvidenceReviewUiState.Reviewing ?: return
        _state.value = current.copy(decision = decision)
    }

    fun setNotes(notes: String) {
        val current = _state.value as? EvidenceReviewUiState.Reviewing ?: return
        _state.value = current.copy(notes = notes)
    }

    fun submitReview() {
        val current =
            (_state.value as? EvidenceReviewUiState.Reviewing)?.takeIf { !it.isSubmitting }
                ?: return
        val decision = current.decision ?: return

        _state.value = current.copy(isSubmitting = true)
        viewModelScope.launch {
            reviewer
                .submitDecision(current.taskId, decision, current.notes)
                .onSuccess { _state.value = EvidenceReviewUiState.Submitted(decision) }
                .onFailure { e ->
                    _state.value =
                        EvidenceReviewUiState.Error(
                            message = e.message ?: "Failed to submit review",
                            reviewing = current,
                        )
                }
        }
    }

    fun clearError() {
        val error = _state.value as? EvidenceReviewUiState.Error ?: return
        _state.value = error.reviewing.copy(isSubmitting = false)
    }
}
