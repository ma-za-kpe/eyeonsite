package com.maku.eyeonsite.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.domain.OnboardingContent
import com.maku.eyeonsite.ui.theme.AlertCoral
import com.maku.eyeonsite.ui.theme.AppVariant
import com.maku.eyeonsite.ui.theme.CautionAmber
import com.maku.eyeonsite.ui.theme.DeepForest
import com.maku.eyeonsite.ui.theme.EyeOnSiteTheme
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.SignalGreen

private const val CAPTURE_BOX_SIZE_DP = 120
private const val PROGRESS_DOT_DP = 8
private const val OPTION_RADIUS_DP = 10
private const val BUTTON_RADIUS_DP = 12

@Composable
fun InspectorOnboardingScreen(
    vm: InspectorOnboardingViewModel,
    uid: String,
    onCaptureId: () -> Unit,
    onCaptureSelfie: () -> Unit,
) {
    val state by vm.state.collectAsState()

    when (val s = state) {
        is InspectorOnboardingUiState.IdCapture ->
            IdCaptureStep(
                state = s,
                onCaptureId = onCaptureId,
                onCaptureSelfie = onCaptureSelfie,
                onProceed = { vm.proceedFromIdCapture(uid) },
                onDismissError = { vm.clearError() },
            )
        is InspectorOnboardingUiState.Training ->
            TrainingStep(
                state = s,
                onNext = { vm.nextTrainingModule() },
                onBack = { vm.previousTrainingModule() },
            )
        is InspectorOnboardingUiState.Quiz ->
            if (s.showResult) {
                QuizResultStep(
                    score = s.score,
                    total = s.questions.size,
                    passed = s.passed,
                    onProceed = { vm.proceedFromQuiz() },
                )
            } else {
                QuizStep(
                    state = s,
                    onSelectAnswer = { vm.selectAnswer(it) },
                    onNext = { vm.nextQuestion() },
                )
            }
        is InspectorOnboardingUiState.ProfileSetup ->
            ProfileSetupStep(
                state = s,
                onNameChange = { vm.setFullName(it) },
                onCityChange = { vm.setCity(it) },
                onRadiusChange = { vm.setServiceRadius(it) },
                onSubmit = { vm.submitProfile(uid) },
                onDismissError = { vm.clearError() },
            )
        InspectorOnboardingUiState.Submitted -> SubmittedStep()
    }
}

// ── Step 1: ID capture ────────────────────────────────────────────────────────

@Composable
private fun IdCaptureStep(
    state: InspectorOnboardingUiState.IdCapture,
    onCaptureId: () -> Unit,
    onCaptureSelfie: () -> Unit,
    onProceed: () -> Unit,
    onDismissError: () -> Unit,
) {
    OnboardingScaffold(
        stepIndex = 0,
        title = "Verify Your Identity",
        subtitle = "We need your government ID and a selfie to verify you are who you say you are.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CaptureSlot(
                modifier = Modifier.weight(1f),
                icon = "🪪",
                label = "Government ID",
                captured = state.hasIdImage,
                onClick = onCaptureId,
            )
            CaptureSlot(
                modifier = Modifier.weight(1f),
                icon = "🤳",
                label = "Selfie",
                captured = state.hasSelfie,
                onClick = onCaptureSelfie,
            )
        }

        state.error?.let { err ->
            ErrorBanner(message = err, onDismiss = onDismissError)
        }

        PrimaryButton(
            text = if (state.isUploading) "Uploading…" else "Continue",
            enabled = state.canProceed,
            onClick = onProceed,
            loading = state.isUploading,
        )
    }
}

@Composable
private fun CaptureSlot(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    captured: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(CAPTURE_BOX_SIZE_DP.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (captured) SignalGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                    ).border(
                        2.dp,
                        if (captured) SignalGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp),
                    ).clickable { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (captured) "✓" else icon,
                    style = MaterialTheme.typography.displaySmall,
                    color = if (captured) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (captured) "Captured" else "Tap to capture",
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        if (captured) {
                            SignalGreen
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Step 2: Training ──────────────────────────────────────────────────────────

@Composable
private fun TrainingStep(
    state: InspectorOnboardingUiState.Training,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    OnboardingScaffold(
        stepIndex = 1,
        title = "Inspector Training",
        subtitle = "Read all ${state.totalModules} modules to continue.",
    ) {
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
            color = Forest,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp),
                    ).padding(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = state.currentModule.icon,
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = state.currentModule.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = state.currentModule.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.moduleIndex > 0) {
                SecondaryButton(
                    text = "← Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Box(modifier = Modifier.weight(1f))
            }
            PrimaryButton(
                text = if (state.isLastModule) "Take Quiz →" else "Next →",
                enabled = true,
                onClick = onNext,
                modifier = Modifier.weight(1f),
            )
        }

        // Module dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(state.totalModules) { index ->
                    Box(
                        modifier =
                            Modifier
                                .size(if (index == state.moduleIndex) PROGRESS_DOT_DP.dp else (PROGRESS_DOT_DP - 2).dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        index < state.moduleIndex -> Forest
                                        index == state.moduleIndex -> SavannaGold
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                    },
                                ),
                    )
                }
            }
        }
    }
}

// ── Step 3: Quiz ──────────────────────────────────────────────────────────────

@Composable
private fun QuizStep(
    state: InspectorOnboardingUiState.Quiz,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
) {
    OnboardingScaffold(
        stepIndex = 2,
        title = "Knowledge Check",
        subtitle =
            "Question ${state.currentIndex + 1} of ${state.questions.size} — " +
                "need ${OnboardingContent.QUIZ_PASS_THRESHOLD}/${state.questions.size} to pass.",
    ) {
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
            color = SavannaGold,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Text(
            text = state.currentQuestion.text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.currentQuestion.options.forEachIndexed { index, option ->
                val selected = state.currentAnswer == index
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(OPTION_RADIUS_DP.dp))
                            .background(
                                if (selected) {
                                    SavannaGold.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                            ).border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) SavannaGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(OPTION_RADIUS_DP.dp),
                            ).clickable { onSelectAnswer(index) }
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (selected) SavannaGold else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (selected) SavannaGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                            )
                        }
                    }
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) SavannaGold else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        PrimaryButton(
            text = if (state.isLastQuestion) "See Results" else "Next →",
            enabled = state.canAdvance,
            onClick = onNext,
        )
    }
}

@Composable
private fun QuizResultStep(
    score: Int,
    total: Int,
    passed: Boolean,
    onProceed: () -> Unit,
) {
    val resultIcon = if (passed) "🎉" else "📖"
    val resultColor = if (passed) SignalGreen else CautionAmber
    val resultHeadline = if (passed) "Quiz Passed!" else "Not Quite"
    val resultBody =
        if (passed) {
            "You scored $score/$total. You're ready to inspect."
        } else {
            "You scored $score/$total. You need ${OnboardingContent.QUIZ_PASS_THRESHOLD}/$total. " +
                "Review the training and try again."
        }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(resultColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = resultIcon, style = MaterialTheme.typography.displaySmall)
            }
            Text(
                text = resultHeadline,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = resultBody,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(total) { index ->
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp)))
                }
            }

            PrimaryButton(
                text = if (passed) "Set Up Profile →" else "Retry Quiz",
                enabled = true,
                onClick = onProceed,
            )
        }
    }
}

// ── Step 4: Profile setup ─────────────────────────────────────────────────────

@Composable
private fun ProfileSetupStep(
    state: InspectorOnboardingUiState.ProfileSetup,
    onNameChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onRadiusChange: (Int) -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
) {
    OnboardingScaffold(
        stepIndex = 3,
        title = "Your Inspector Profile",
        subtitle = "Tell us a bit about yourself so clients know who they're working with.",
    ) {
        OnboardingTextField(
            value = state.fullName,
            placeholder = "Full name",
            onValueChange = onNameChange,
        )
        OnboardingTextField(
            value = state.city,
            placeholder = "City (e.g. Accra, Lagos, Nairobi)",
            onValueChange = onCityChange,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Service radius — ${state.serviceRadiusKm}km",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OnboardingContent.SERVICE_RADIUS_OPTIONS.forEach { radius ->
                    val selected = radius == state.serviceRadiusKm
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) {
                                        Forest.copy(
                                            alpha = 0.1f,
                                        )
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    },
                                ).border(
                                    if (selected) 2.dp else 1.dp,
                                    if (selected) Forest else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp),
                                ).clickable { onRadiusChange(radius) }
                                .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${radius}km",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Forest else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        state.error?.let { err ->
            ErrorBanner(message = err, onDismiss = onDismissError)
        }

        PrimaryButton(
            text = if (state.isSubmitting) "Submitting…" else "Submit Application",
            enabled = state.canSubmit,
            onClick = onSubmit,
            loading = state.isSubmitting,
        )
    }
}

// ── Submitted ─────────────────────────────────────────────────────────────────

@Composable
private fun SubmittedStep() {
    Box(
        modifier = Modifier.fillMaxSize().background(DeepForest),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("🌍", style = MaterialTheme.typography.displayMedium)
            Text(
                text = "Application Submitted",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Text(
                text =
                    "Our team will review your ID and profile within 24–48 hours. " +
                        "You'll receive a notification once you're approved to start accepting tasks.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Forest)
                        .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Got it",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
        }
    }
}

// ── Shared primitives ─────────────────────────────────────────────────────────

@Composable
private fun OnboardingScaffold(
    stepIndex: Int,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    val totalSteps = 4
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Step indicator
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(totalSteps) { index ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .size(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    index < stepIndex -> Forest
                                    index == stepIndex -> SavannaGold
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                            ),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        content()
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BUTTON_RADIUS_DP.dp))
                .background(
                    if (enabled) Forest else MaterialTheme.colorScheme.surfaceVariant,
                ).clickable(enabled = enabled) { onClick() }
                .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BUTTON_RADIUS_DP.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(BUTTON_RADIUS_DP.dp),
                ).clickable { onClick() }
                .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun OnboardingTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    RoundedCornerShape(10.dp),
                ).padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
        )
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AlertCoral.copy(alpha = 0.1f))
                .border(1.dp, AlertCoral.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = AlertCoral,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "✕",
            style = MaterialTheme.typography.labelMedium,
            color = AlertCoral,
            modifier = Modifier.clickable { onDismiss() }.padding(4.dp),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private class FakeOnboardingRepo : InspectorOnboardingRepository {
    override suspend fun uploadIdDocuments(
        uid: String,
        idBytes: ByteArray,
        selfieBytes: ByteArray,
    ): Result<Pair<String, String>> = Result.success("" to "")

    override suspend fun submitProfile(
        uid: String,
        fullName: String,
        city: String,
        serviceRadiusKm: Int,
        testScore: Int,
        idDocPath: String,
        selfiePath: String,
    ): Result<Unit> = Result.success(Unit)
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OnboardingIdCapturePreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        InspectorOnboardingScreen(
            vm = InspectorOnboardingViewModel(FakeOnboardingRepo(), NoOpAnalyticsService),
            uid = "preview-uid",
            onCaptureId = {},
            onCaptureSelfie = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OnboardingIdCapturedPreview() {
    val vm = InspectorOnboardingViewModel(FakeOnboardingRepo(), NoOpAnalyticsService)
    vm.setIdImage(ByteArray(0))
    vm.setSelfie(ByteArray(0))
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        InspectorOnboardingScreen(
            vm = vm,
            uid = "preview-uid",
            onCaptureId = {},
            onCaptureSelfie = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OnboardingTrainingPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        TrainingStep(
            state = InspectorOnboardingUiState.Training(moduleIndex = 2),
            onNext = {},
            onBack = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OnboardingQuizPreview() {
    val state =
        InspectorOnboardingUiState.Quiz(
            currentIndex = 0,
            answers = mapOf(0 to 1),
        )
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        QuizStep(state = state, onSelectAnswer = {}, onNext = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OnboardingQuizPassedPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        QuizResultStep(score = 4, total = 5, passed = true, onProceed = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OnboardingProfileSetupPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = false) {
        ProfileSetupStep(
            state =
                InspectorOnboardingUiState.ProfileSetup(
                    fullName = "Kwame Mensah",
                    city = "Accra",
                    serviceRadiusKm = 15,
                ),
            onNameChange = {},
            onCityChange = {},
            onRadiusChange = {},
            onSubmit = {},
            onDismissError = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun OnboardingSubmittedPreview() {
    EyeOnSiteTheme(variant = AppVariant.INSPECTOR, darkTheme = true) {
        SubmittedStep()
    }
}
