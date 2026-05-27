package com.maku.eyeonsite.review

import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.domain.DisputeReason
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.domain.ReviewDecision
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.review.EvidenceReviewUiState
import com.maku.eyeonsite.ui.review.EvidenceReviewViewModel
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
class EvidenceReviewViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var reviewer: FakeEvidenceReviewer
    private lateinit var vm: EvidenceReviewViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        reviewer = FakeEvidenceReviewer()
        vm = EvidenceReviewViewModel(reviewer, NoOpAnalyticsService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makePhoto(id: String = "p1") =
        EvidencePhoto(
            id = id,
            taskId = "task-1",
            latitude = 5.5502,
            longitude = -0.2174,
            capturedAtMs = 1_700_000_000_000L,
        )

    private fun load(photos: List<EvidencePhoto> = listOf(makePhoto())) =
        vm.loadEvidence("task-1", "Verify floor progress", TaskType.CONSTRUCTION, photos)

    // ── loadEvidence ──────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        assertIs<EvidenceReviewUiState.Loading>(vm.state.value)
    }

    @Test
    fun `loadEvidence transitions to Reviewing`() =
        runTest {
            load()
            assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
        }

    @Test
    fun `loadEvidence with empty photos produces hasPhotos false`() =
        runTest {
            load(emptyList())
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertFalse(state.hasPhotos)
        }

    @Test
    fun `loadEvidence with photos produces hasPhotos true`() =
        runTest {
            load(listOf(makePhoto("p1"), makePhoto("p2")))
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertTrue(state.hasPhotos)
            assertEquals(2, state.photoCount)
        }

    // ── canSubmit ─────────────────────────────────────────────────────────────

    @Test
    fun `canSubmit false when decision is null`() =
        runTest {
            load()
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertFalse(state.canSubmit)
        }

    @Test
    fun `canSubmit true when ACCEPTED decision is set`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.ACCEPTED)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertTrue(state.canSubmit)
        }

    @Test
    fun `canSubmit true when REVISION_REQUESTED decision is set`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.REVISION_REQUESTED)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertTrue(state.canSubmit)
        }

    @Test
    fun `canSubmit false when DISPUTED but no dispute reason`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.DISPUTED)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertFalse(state.canSubmit)
        }

    @Test
    fun `canSubmit true when DISPUTED with dispute reason`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.DISPUTED)
            vm.setDisputeReason(DisputeReason.WRONG_LOCATION)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertTrue(state.canSubmit)
        }

    @Test
    fun `canSubmit false while isSubmitting`() {
        val state =
            EvidenceReviewUiState.Reviewing(
                taskId = "t1",
                taskDescription = "desc",
                taskType = TaskType.CONSTRUCTION,
                photos = listOf(makePhoto()),
                decision = ReviewDecision.ACCEPTED,
                isSubmitting = true,
            )
        assertFalse(state.canSubmit)
    }

    // ── setDecision ───────────────────────────────────────────────────────────

    @Test
    fun `setDecision updates decision in Reviewing state`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.REVISION_REQUESTED)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals(ReviewDecision.REVISION_REQUESTED, state.decision)
        }

    @Test
    fun `setDecision null clears decision`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.ACCEPTED)
            vm.setDecision(null)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertNull(state.decision)
        }

    @Test
    fun `setDecision clears disputeReason when switching away from DISPUTED`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.DISPUTED)
            vm.setDisputeReason(DisputeReason.FRAUDULENT)
            vm.setDecision(ReviewDecision.ACCEPTED)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertNull(state.disputeReason)
        }

    @Test
    fun `setDecision on Loading state is no-op`() =
        runTest {
            vm.setDecision(ReviewDecision.ACCEPTED)
            assertIs<EvidenceReviewUiState.Loading>(vm.state.value)
        }

    // ── setDisputeReason ──────────────────────────────────────────────────────

    @Test
    fun `setDisputeReason updates disputeReason in Reviewing state`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.DISPUTED)
            vm.setDisputeReason(DisputeReason.WRONG_PROPERTY)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals(DisputeReason.WRONG_PROPERTY, state.disputeReason)
        }

    @Test
    fun `setDisputeReason null clears reason`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.DISPUTED)
            vm.setDisputeReason(DisputeReason.POOR_QUALITY)
            vm.setDisputeReason(null)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertNull(state.disputeReason)
        }

    @Test
    fun `setDisputeReason on Loading state is no-op`() =
        runTest {
            vm.setDisputeReason(DisputeReason.OTHER)
            assertIs<EvidenceReviewUiState.Loading>(vm.state.value)
        }

    // ── setNotes ──────────────────────────────────────────────────────────────

    @Test
    fun `setNotes updates notes in Reviewing state`() =
        runTest {
            load()
            vm.setNotes("The columns look off-center")
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals("The columns look off-center", state.notes)
        }

    // ── selectPhoto ───────────────────────────────────────────────────────────

    @Test
    fun `selectPhoto updates selectedPhotoIndex`() =
        runTest {
            load(listOf(makePhoto("p1"), makePhoto("p2"), makePhoto("p3")))
            vm.selectPhoto(2)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals(2, state.selectedPhotoIndex)
        }

    @Test
    fun `selectPhoto negative index is clamped to zero`() =
        runTest {
            load(listOf(makePhoto("p1"), makePhoto("p2")))
            vm.selectPhoto(-5)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals(0, state.selectedPhotoIndex)
        }

    @Test
    fun `selectPhoto beyond last is clamped to last index`() =
        runTest {
            load(listOf(makePhoto("p1"), makePhoto("p2"), makePhoto("p3")))
            vm.selectPhoto(99)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals(2, state.selectedPhotoIndex)
        }

    @Test
    fun `selectPhoto on empty photos list stays at zero`() =
        runTest {
            load(emptyList())
            vm.selectPhoto(5)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals(0, state.selectedPhotoIndex)
        }

    // ── submitReview ──────────────────────────────────────────────────────────

    @Test
    fun `submitReview when no decision selected is no-op`() =
        runTest {
            load()
            vm.submitReview()
            assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals(0, reviewer.submissions.size)
        }

    @Test
    fun `submitReview when DISPUTED but no reason is no-op`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.DISPUTED)
            vm.submitReview()
            assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals(0, reviewer.submissions.size)
        }

    @Test
    fun `submitReview transitions to Submitted with correct decision`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.ACCEPTED)
            vm.submitReview()
            val state = assertIs<EvidenceReviewUiState.Submitted>(vm.state.value)
            assertEquals(ReviewDecision.ACCEPTED, state.decision)
        }

    @Test
    fun `submitReview with DISPUTED and reason passes reason to reviewer`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.DISPUTED)
            vm.setDisputeReason(DisputeReason.FRAUDULENT)
            vm.submitReview()
            assertIs<EvidenceReviewUiState.Submitted>(vm.state.value)
            assertEquals(1, reviewer.submissions.size)
            assertEquals(DisputeReason.FRAUDULENT, reviewer.submissions[0].disputeReason)
        }

    @Test
    fun `submitReview calls reviewer exactly once`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.DISPUTED)
            vm.setDisputeReason(DisputeReason.WRONG_LOCATION)
            vm.submitReview()
            assertEquals(1, reviewer.submissions.size)
        }

    @Test
    fun `submitReview when already Submitted is no-op`() =
        runTest {
            load()
            vm.setDecision(ReviewDecision.ACCEPTED)
            vm.submitReview()
            assertIs<EvidenceReviewUiState.Submitted>(vm.state.value)
            vm.submitReview()
            assertEquals(1, reviewer.submissions.size)
        }

    @Test
    fun `reviewer failure produces Error state with message`() =
        runTest {
            reviewer = FakeEvidenceReviewer(shouldFail = true)
            vm = EvidenceReviewViewModel(reviewer, NoOpAnalyticsService)
            load()
            vm.setDecision(ReviewDecision.ACCEPTED)
            vm.submitReview()
            val state = assertIs<EvidenceReviewUiState.Error>(vm.state.value)
            assertEquals("Server error", state.message)
        }

    @Test
    fun `updateCountdown sets secondsRemaining`() =
        runTest {
            load()
            vm.updateCountdown(172800L)
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertEquals(172800L, state.secondsRemaining)
        }

    @Test
    fun `updateCountdown on Loading state is no-op`() =
        runTest {
            vm.updateCountdown(100L)
            assertIs<EvidenceReviewUiState.Loading>(vm.state.value)
        }

    @Test
    fun `clearError restores Reviewing state with isSubmitting false`() =
        runTest {
            reviewer = FakeEvidenceReviewer(shouldFail = true)
            vm = EvidenceReviewViewModel(reviewer, NoOpAnalyticsService)
            load()
            vm.setDecision(ReviewDecision.REVISION_REQUESTED)
            vm.submitReview()
            assertIs<EvidenceReviewUiState.Error>(vm.state.value)
            vm.clearError()
            val state = assertIs<EvidenceReviewUiState.Reviewing>(vm.state.value)
            assertFalse(state.isSubmitting)
        }
}
