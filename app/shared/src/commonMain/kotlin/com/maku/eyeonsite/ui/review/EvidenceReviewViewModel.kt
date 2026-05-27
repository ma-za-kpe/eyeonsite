package com.maku.eyeonsite.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.domain.ChecklistResponse
import com.maku.eyeonsite.domain.DisputeReason
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
        val disputeReason: DisputeReason? = null,
        val selectedPhotoIndex: Int = 0,
        val isSubmitting: Boolean = false,
        val notes: String = "",
        val secondsRemaining: Long? = null,
        val qualityScore: Float? = null,
        val checklistResponses: List<ChecklistResponse> = emptyList(),
    ) : EvidenceReviewUiState() {
        val hasPhotos: Boolean get() = photos.isNotEmpty()
        val canSubmit: Boolean
            get() =
                decision != null &&
                    !isSubmitting &&
                    (decision != ReviewDecision.DISPUTED || disputeReason != null)
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
    private val analytics: AnalyticsService,
) : ViewModel() {
    private val _state = MutableStateFlow<EvidenceReviewUiState>(EvidenceReviewUiState.Loading)
    val state: StateFlow<EvidenceReviewUiState> = _state.asStateFlow()

    fun loadEvidence(
        taskId: String,
        taskDescription: String,
        taskType: TaskType,
        photos: List<EvidencePhoto>,
        qualityScore: Float? = null,
        checklistResponses: List<ChecklistResponse> = emptyList(),
    ) {
        _state.value =
            EvidenceReviewUiState.Reviewing(
                taskId = taskId,
                taskDescription = taskDescription,
                taskType = taskType,
                photos = photos,
                qualityScore = qualityScore,
                checklistResponses = checklistResponses,
            )
        analytics.logEvent(
            name = "evidence_review_opened",
            params = mapOf("task_id" to taskId, "photo_count" to photos.size.toString()),
        )
    }

    fun updateCountdown(secondsRemaining: Long) {
        val current = _state.value as? EvidenceReviewUiState.Reviewing ?: return
        _state.value = current.copy(secondsRemaining = secondsRemaining)
    }

    fun selectPhoto(index: Int) {
        val current = _state.value as? EvidenceReviewUiState.Reviewing ?: return
        val clamped = index.coerceIn(0, (current.photos.size - 1).coerceAtLeast(0))
        _state.value = current.copy(selectedPhotoIndex = clamped)
    }

    fun setDecision(decision: ReviewDecision?) {
        val current = _state.value as? EvidenceReviewUiState.Reviewing ?: return
        _state.value =
            current.copy(
                decision = decision,
                // Clear reason whenever decision changes away from DISPUTED
                disputeReason = if (decision == ReviewDecision.DISPUTED) current.disputeReason else null,
            )
    }

    fun setDisputeReason(reason: DisputeReason?) {
        val current = _state.value as? EvidenceReviewUiState.Reviewing ?: return
        _state.value = current.copy(disputeReason = reason)
    }

    fun setNotes(notes: String) {
        val current = _state.value as? EvidenceReviewUiState.Reviewing ?: return
        _state.value = current.copy(notes = notes)
    }

    fun submitReview() {
        val current =
            (_state.value as? EvidenceReviewUiState.Reviewing)?.takeIf { it.canSubmit }
                ?: return
        val decision = current.decision ?: return

        _state.value = current.copy(isSubmitting = true)
        viewModelScope.launch {
            reviewer
                .submitDecision(current.taskId, decision, current.notes, current.disputeReason)
                .onSuccess {
                    _state.value = EvidenceReviewUiState.Submitted(decision)
                    val eventName =
                        when (decision) {
                            ReviewDecision.ACCEPTED -> "evidence_accepted"
                            ReviewDecision.DISPUTED -> "evidence_disputed"
                            ReviewDecision.REVISION_REQUESTED -> "evidence_revision_requested"
                        }
                    analytics.logEvent(
                        name = eventName,
                        params = mapOf("task_id" to current.taskId),
                    )
                }.onFailure { e ->
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
