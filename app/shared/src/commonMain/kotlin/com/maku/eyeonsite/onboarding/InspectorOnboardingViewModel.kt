package com.maku.eyeonsite.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.domain.OnboardingContent
import com.maku.eyeonsite.domain.QuizQuestion
import com.maku.eyeonsite.domain.TrainingModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class InspectorOnboardingUiState {
    // Step 1 — capture government ID + selfie
    data class IdCapture(
        val hasIdImage: Boolean = false,
        val hasSelfie: Boolean = false,
        val isUploading: Boolean = false,
        val error: String? = null,
    ) : InspectorOnboardingUiState() {
        val canProceed: Boolean get() = hasIdImage && hasSelfie && !isUploading
    }

    // Step 2 — read training modules one at a time
    data class Training(
        val moduleIndex: Int = 0,
    ) : InspectorOnboardingUiState() {
        val currentModule: TrainingModule get() = OnboardingContent.trainingModules[moduleIndex]
        val totalModules: Int get() = OnboardingContent.trainingModules.size
        val isLastModule: Boolean get() = moduleIndex == totalModules - 1
        val progress: Float get() = (moduleIndex + 1).toFloat() / totalModules
    }

    // Step 3 — 5-question quiz
    data class Quiz(
        val questions: List<QuizQuestion> = OnboardingContent.quizQuestions,
        val currentIndex: Int = 0,
        val answers: Map<Int, Int> = emptyMap(),
        val showResult: Boolean = false,
    ) : InspectorOnboardingUiState() {
        val currentQuestion: QuizQuestion get() = questions[currentIndex]
        val currentAnswer: Int? get() = answers[currentIndex]
        val isLastQuestion: Boolean get() = currentIndex == questions.size - 1
        val canAdvance: Boolean get() = currentAnswer != null
        val score: Int get() = answers.count { (idx, ans) -> questions[idx].correctIndex == ans }
        val passed: Boolean get() = score >= OnboardingContent.QUIZ_PASS_THRESHOLD
    }

    // Step 4 — name, city, service radius
    data class ProfileSetup(
        val fullName: String = "",
        val city: String = "",
        val serviceRadiusKm: Int = 10,
        val isSubmitting: Boolean = false,
        val error: String? = null,
    ) : InspectorOnboardingUiState() {
        val canSubmit: Boolean get() = fullName.isNotBlank() && city.isNotBlank() && !isSubmitting
    }

    data object Submitted : InspectorOnboardingUiState()
}

class InspectorOnboardingViewModel(
    private val repository: InspectorOnboardingRepository,
    private val analytics: AnalyticsService,
) : ViewModel() {
    private val _state =
        MutableStateFlow<InspectorOnboardingUiState>(InspectorOnboardingUiState.IdCapture())
    val state: StateFlow<InspectorOnboardingUiState> = _state.asStateFlow()

    // Transient data collected across steps
    private var uploadedIdDocPath: String = ""
    private var uploadedSelfiePath: String = ""
    private var pendingIdBytes: ByteArray? = null
    private var pendingSelfieBytes: ByteArray? = null
    private var quizScore: Int = 0

    // ── Step 1: ID capture ────────────────────────────────────────────────────

    fun setIdImage(bytes: ByteArray) {
        val current = _state.value as? InspectorOnboardingUiState.IdCapture ?: return
        pendingIdBytes = bytes
        _state.value = current.copy(hasIdImage = true, error = null)
    }

    fun setSelfie(bytes: ByteArray) {
        val current = _state.value as? InspectorOnboardingUiState.IdCapture ?: return
        pendingSelfieBytes = bytes
        _state.value = current.copy(hasSelfie = true, error = null)
    }

    fun proceedFromIdCapture(uid: String) {
        val current = _state.value as? InspectorOnboardingUiState.IdCapture ?: return
        val idBytes = pendingIdBytes
        val selfieBytes = pendingSelfieBytes
        if (!current.canProceed || idBytes == null || selfieBytes == null) return

        _state.value = current.copy(isUploading = true, error = null)
        viewModelScope.launch {
            repository
                .uploadIdDocuments(uid, idBytes, selfieBytes)
                .onSuccess { (idDocPath, selfiePath) ->
                    uploadedIdDocPath = idDocPath
                    uploadedSelfiePath = selfiePath
                    analytics.logEvent("onboarding_id_uploaded", mapOf("uid" to uid))
                    _state.value = InspectorOnboardingUiState.Training()
                }.onFailure { e ->
                    _state.value =
                        current.copy(
                            isUploading = false,
                            error = e.message ?: "Upload failed. Please try again.",
                        )
                }
        }
    }

    // ── Step 2: Training ──────────────────────────────────────────────────────

    fun nextTrainingModule() {
        val current = _state.value as? InspectorOnboardingUiState.Training ?: return
        if (current.isLastModule) {
            analytics.logEvent("onboarding_training_complete", emptyMap())
            _state.value = InspectorOnboardingUiState.Quiz()
        } else {
            _state.value = current.copy(moduleIndex = current.moduleIndex + 1)
        }
    }

    fun previousTrainingModule() {
        val current = _state.value as? InspectorOnboardingUiState.Training ?: return
        if (current.moduleIndex > 0) {
            _state.value = current.copy(moduleIndex = current.moduleIndex - 1)
        }
    }

    // ── Step 3: Quiz ──────────────────────────────────────────────────────────

    fun selectAnswer(optionIndex: Int) {
        val current = _state.value as? InspectorOnboardingUiState.Quiz ?: return
        if (current.showResult) return
        _state.value =
            current.copy(answers = current.answers + (current.currentIndex to optionIndex))
    }

    fun nextQuestion() {
        val current = _state.value as? InspectorOnboardingUiState.Quiz ?: return
        if (!current.canAdvance || current.showResult) return
        if (current.isLastQuestion) {
            _state.value = current.copy(showResult = true)
        } else {
            _state.value = current.copy(currentIndex = current.currentIndex + 1)
        }
    }

    fun proceedFromQuiz() {
        val current = _state.value as? InspectorOnboardingUiState.Quiz ?: return
        if (!current.showResult) return
        if (current.passed) {
            quizScore = current.score
            analytics.logEvent(
                "onboarding_quiz_passed",
                mapOf("score" to current.score.toString()),
            )
            _state.value = InspectorOnboardingUiState.ProfileSetup()
        } else {
            analytics.logEvent(
                "onboarding_quiz_failed",
                mapOf("score" to current.score.toString()),
            )
            _state.value = InspectorOnboardingUiState.Quiz()
        }
    }

    // ── Step 4: Profile setup ─────────────────────────────────────────────────

    fun setFullName(name: String) {
        val current = _state.value as? InspectorOnboardingUiState.ProfileSetup ?: return
        _state.value = current.copy(fullName = name)
    }

    fun setCity(city: String) {
        val current = _state.value as? InspectorOnboardingUiState.ProfileSetup ?: return
        _state.value = current.copy(city = city)
    }

    fun setServiceRadius(radiusKm: Int) {
        val current = _state.value as? InspectorOnboardingUiState.ProfileSetup ?: return
        _state.value = current.copy(serviceRadiusKm = radiusKm)
    }

    fun submitProfile(uid: String) {
        val current = _state.value as? InspectorOnboardingUiState.ProfileSetup ?: return
        if (!current.canSubmit) return

        _state.value = current.copy(isSubmitting = true, error = null)
        viewModelScope.launch {
            repository
                .submitProfile(
                    uid = uid,
                    fullName = current.fullName.trim(),
                    city = current.city.trim(),
                    serviceRadiusKm = current.serviceRadiusKm,
                    testScore = quizScore,
                    idDocPath = uploadedIdDocPath,
                    selfiePath = uploadedSelfiePath,
                ).onSuccess {
                    analytics.logEvent(
                        "onboarding_submitted",
                        mapOf("uid" to uid, "city" to current.city),
                    )
                    _state.value = InspectorOnboardingUiState.Submitted
                }.onFailure { e ->
                    _state.value =
                        current.copy(
                            isSubmitting = false,
                            error = e.message ?: "Submission failed. Please try again.",
                        )
                }
        }
    }

    fun clearError() {
        when (val s = _state.value) {
            is InspectorOnboardingUiState.IdCapture -> _state.value = s.copy(error = null)
            is InspectorOnboardingUiState.ProfileSetup -> _state.value = s.copy(error = null)
            else -> Unit
        }
    }
}
