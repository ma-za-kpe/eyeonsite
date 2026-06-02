package com.maku.eyeonsite.onboarding

import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.domain.OnboardingContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InspectorOnboardingViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeInspectorOnboardingRepository
    private lateinit var vm: InspectorOnboardingViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeInspectorOnboardingRepository()
        vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is IdCapture`() {
        assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
    }

    @Test
    fun `initial IdCapture has no images`() {
        val s = assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
        assertFalse(s.hasIdImage)
        assertFalse(s.hasSelfie)
    }

    // ── ID capture ────────────────────────────────────────────────────────────

    @Test
    fun `setIdImage sets hasIdImage true`() {
        vm.setIdImage(ByteArray(10))
        val s = assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
        assertTrue(s.hasIdImage)
    }

    @Test
    fun `setSelfie sets hasSelfie true`() {
        vm.setSelfie(ByteArray(10))
        val s = assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
        assertTrue(s.hasSelfie)
    }

    @Test
    fun `canProceed false until both images set`() {
        vm.setIdImage(ByteArray(10))
        val s = assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
        assertFalse(s.canProceed)
    }

    @Test
    fun `canProceed true when both images set`() {
        vm.setIdImage(ByteArray(10))
        vm.setSelfie(ByteArray(10))
        val s = assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
        assertTrue(s.canProceed)
    }

    @Test
    fun `proceedFromIdCapture uploads and transitions to Training`() =
        runTest {
            vm.setIdImage(ByteArray(10))
            vm.setSelfie(ByteArray(10))
            vm.proceedFromIdCapture("uid-123")
            assertIs<InspectorOnboardingUiState.Training>(vm.state.value)
            assertEquals(1, repo.uploadCalls.size)
        }

    @Test
    fun `proceedFromIdCapture when canProceed false is no-op`() =
        runTest {
            vm.proceedFromIdCapture("uid-123")
            assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
            assertEquals(0, repo.uploadCalls.size)
        }

    @Test
    fun `upload failure shows error and stays on IdCapture`() =
        runTest {
            repo = FakeInspectorOnboardingRepository(uploadShouldFail = true)
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)
            vm.setIdImage(ByteArray(1))
            vm.setSelfie(ByteArray(1))
            vm.proceedFromIdCapture("uid-fail")
            val s = assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
            assertEquals("Upload failed", s.error)
        }

    @Test
    fun `clearError clears IdCapture error`() =
        runTest {
            repo = FakeInspectorOnboardingRepository(uploadShouldFail = true)
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)
            vm.setIdImage(ByteArray(1))
            vm.setSelfie(ByteArray(1))
            vm.proceedFromIdCapture("uid-fail")
            vm.clearError()
            val s = assertIs<InspectorOnboardingUiState.IdCapture>(vm.state.value)
            assertNull(s.error)
        }

    // ── Training ──────────────────────────────────────────────────────────────

    private fun advanceToTraining() {
        vm.setIdImage(ByteArray(1))
        vm.setSelfie(ByteArray(1))
        vm.proceedFromIdCapture("uid-test")
    }

    @Test
    fun `training starts at module 0`() =
        runTest {
            advanceToTraining()
            val s = assertIs<InspectorOnboardingUiState.Training>(vm.state.value)
            assertEquals(0, s.moduleIndex)
        }

    @Test
    fun `nextTrainingModule advances module index`() =
        runTest {
            advanceToTraining()
            vm.nextTrainingModule()
            val s = assertIs<InspectorOnboardingUiState.Training>(vm.state.value)
            assertEquals(1, s.moduleIndex)
        }

    @Test
    fun `previousTrainingModule goes back`() =
        runTest {
            advanceToTraining()
            vm.nextTrainingModule()
            vm.previousTrainingModule()
            val s = assertIs<InspectorOnboardingUiState.Training>(vm.state.value)
            assertEquals(0, s.moduleIndex)
        }

    @Test
    fun `previousTrainingModule at index 0 is no-op`() =
        runTest {
            advanceToTraining()
            vm.previousTrainingModule()
            val s = assertIs<InspectorOnboardingUiState.Training>(vm.state.value)
            assertEquals(0, s.moduleIndex)
        }

    @Test
    fun `next on last training module transitions to Quiz`() =
        runTest {
            advanceToTraining()
            repeat(OnboardingContent.trainingModules.size) { vm.nextTrainingModule() }
            assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
        }

    // ── Quiz ──────────────────────────────────────────────────────────────────

    private fun advanceToQuiz() {
        advanceToTraining()
        repeat(OnboardingContent.trainingModules.size) { vm.nextTrainingModule() }
    }

    @Test
    fun `quiz starts at question 0`() =
        runTest {
            advanceToQuiz()
            val s = assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
            assertEquals(0, s.currentIndex)
            assertFalse(s.showResult)
        }

    @Test
    fun `selectAnswer records answer`() =
        runTest {
            advanceToQuiz()
            vm.selectAnswer(1)
            val s = assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
            assertEquals(1, s.currentAnswer)
        }

    @Test
    fun `nextQuestion without answer is no-op`() =
        runTest {
            advanceToQuiz()
            vm.nextQuestion()
            val s = assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
            assertEquals(0, s.currentIndex)
        }

    @Test
    fun `nextQuestion with answer advances`() =
        runTest {
            advanceToQuiz()
            vm.selectAnswer(0)
            vm.nextQuestion()
            val s = assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
            assertEquals(1, s.currentIndex)
        }

    @Test
    fun `answering all questions and next shows result`() =
        runTest {
            advanceToQuiz()
            repeat(OnboardingContent.quizQuestions.size) {
                vm.selectAnswer(0)
                vm.nextQuestion()
            }
            val s = assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
            assertTrue(s.showResult)
        }

    @Test
    fun `score correct when all answers correct`() =
        runTest {
            advanceToQuiz()
            OnboardingContent.quizQuestions.forEachIndexed { i, q ->
                vm.selectAnswer(q.correctIndex)
                vm.nextQuestion()
                // After last question nextQuestion sets showResult; quiz index stays
                if (i < OnboardingContent.quizQuestions.size - 1) {
                    val s = assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
                    assertEquals(i + 1, s.currentIndex)
                }
            }
            val s = assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
            assertEquals(OnboardingContent.quizQuestions.size, s.score)
            assertTrue(s.passed)
        }

    @Test
    fun `proceedFromQuiz when passed transitions to ProfileSetup`() =
        runTest {
            advanceToQuiz()
            OnboardingContent.quizQuestions.forEach { q ->
                vm.selectAnswer(q.correctIndex)
                vm.nextQuestion()
            }
            vm.proceedFromQuiz()
            assertIs<InspectorOnboardingUiState.ProfileSetup>(vm.state.value)
        }

    @Test
    fun `proceedFromQuiz when failed retries quiz`() =
        runTest {
            advanceToQuiz()
            repeat(OnboardingContent.quizQuestions.size) {
                vm.selectAnswer(0)
                vm.nextQuestion()
            }
            val resultState = assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
            if (!resultState.passed) {
                vm.proceedFromQuiz()
                val s = assertIs<InspectorOnboardingUiState.Quiz>(vm.state.value)
                assertFalse(s.showResult)
                assertEquals(0, s.currentIndex)
            }
        }

    // ── Profile setup ─────────────────────────────────────────────────────────

    private fun advanceToProfile() {
        advanceToQuiz()
        OnboardingContent.quizQuestions.forEach { q ->
            vm.selectAnswer(q.correctIndex)
            vm.nextQuestion()
        }
        vm.proceedFromQuiz()
    }

    @Test
    fun `setFullName updates state`() =
        runTest {
            advanceToProfile()
            vm.setFullName("Kwame Mensah")
            val s = assertIs<InspectorOnboardingUiState.ProfileSetup>(vm.state.value)
            assertEquals("Kwame Mensah", s.fullName)
        }

    @Test
    fun `setCity updates state`() =
        runTest {
            advanceToProfile()
            vm.setCity("Accra")
            val s = assertIs<InspectorOnboardingUiState.ProfileSetup>(vm.state.value)
            assertEquals("Accra", s.city)
        }

    @Test
    fun `setServiceRadius updates state`() =
        runTest {
            advanceToProfile()
            vm.setServiceRadius(20)
            val s = assertIs<InspectorOnboardingUiState.ProfileSetup>(vm.state.value)
            assertEquals(20, s.serviceRadiusKm)
        }

    @Test
    fun `canSubmit false when name or city blank`() =
        runTest {
            advanceToProfile()
            vm.setFullName("Kwame")
            val s = assertIs<InspectorOnboardingUiState.ProfileSetup>(vm.state.value)
            assertFalse(s.canSubmit)
        }

    @Test
    fun `canSubmit true when name and city filled`() =
        runTest {
            advanceToProfile()
            vm.setFullName("Kwame Mensah")
            vm.setCity("Accra")
            val s = assertIs<InspectorOnboardingUiState.ProfileSetup>(vm.state.value)
            assertTrue(s.canSubmit)
        }

    @Test
    fun `submitProfile transitions to Submitted`() =
        runTest {
            advanceToProfile()
            vm.setFullName("Kwame Mensah")
            vm.setCity("Accra")
            vm.submitProfile("uid-test")
            assertIs<InspectorOnboardingUiState.Submitted>(vm.state.value)
            assertEquals(1, repo.submissions.size)
        }

    @Test
    fun `submitProfile passes correct data`() =
        runTest {
            advanceToProfile()
            vm.setFullName("Kwame Mensah")
            vm.setCity("Accra")
            vm.setServiceRadius(15)
            vm.submitProfile("uid-test")
            val sub = repo.submissions.first()
            assertEquals("Kwame Mensah", sub.fullName)
            assertEquals("Accra", sub.city)
            assertEquals(15, sub.serviceRadiusKm)
        }

    @Test
    fun `submitProfile passes private KYC storage paths instead of download urls`() =
        runTest {
            advanceToProfile()
            vm.setFullName("Kwame Mensah")
            vm.setCity("Accra")
            vm.submitProfile("uid-test")
            val sub = repo.submissions.first()
            assertEquals("id_docs/uid-test/govt_id.jpg", sub.idDocPath)
            assertEquals("id_docs/uid-test/selfie.jpg", sub.selfiePath)
            assertFalse(sub.idDocPath.startsWith("http"))
            assertFalse(sub.selfiePath.startsWith("http"))
        }

    @Test
    fun `submit failure shows error`() =
        runTest {
            repo = FakeInspectorOnboardingRepository(submitShouldFail = true)
            vm = InspectorOnboardingViewModel(repo, NoOpAnalyticsService)
            advanceToProfile()
            vm.setFullName("Kwame")
            vm.setCity("Kumasi")
            vm.submitProfile("uid-fail")
            val s = assertIs<InspectorOnboardingUiState.ProfileSetup>(vm.state.value)
            assertEquals("Submit failed", s.error)
        }
}
