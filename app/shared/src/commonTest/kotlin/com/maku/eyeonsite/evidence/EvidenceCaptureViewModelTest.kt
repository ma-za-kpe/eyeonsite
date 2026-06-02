package com.maku.eyeonsite.evidence

import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.domain.EvidencePackage
import com.maku.eyeonsite.domain.UploadStatus
import com.maku.eyeonsite.ui.evidence.EvidenceCaptureViewModel
import com.maku.eyeonsite.ui.evidence.NoOpPhotoQualityAnalyzer
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EvidenceCaptureViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeEvidenceRepository
    private lateinit var vm: EvidenceCaptureViewModel

    private val fakeBytes = ByteArray(4) { it.toByte() }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeEvidenceRepository()
        vm = EvidenceCaptureViewModel(repository, NoOpPhotoQualityAnalyzer, NoOpAnalyticsService)
        vm.initTask("task-001")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── initTask ─────────────────────────────────────────────────────────────────

    @Test
    fun `initTask sets taskId and clears photos`() {
        vm.addPhoto(fakeBytes, 5.6, -0.18)
        vm.initTask("task-002")

        val state = vm.state.value
        assertEquals("task-002", state.taskId)
        assertTrue(state.photos.isEmpty())
    }

    // ── addPhoto ─────────────────────────────────────────────────────────────────

    @Test
    fun `addPhoto appends a photo to state`() {
        vm.addPhoto(fakeBytes, 5.6, -0.18)

        assertEquals(1, vm.state.value.photos.size)
    }

    @Test
    fun `addPhoto stores GPS coordinates`() {
        vm.addPhoto(fakeBytes, 5.603717, -0.186964)

        val photo =
            vm.state.value.photos
                .first()
        assertEquals(5.603717, photo.latitude)
        assertEquals(-0.186964, photo.longitude)
    }

    @Test
    fun `addPhoto sets taskId on each photo`() {
        vm.addPhoto(fakeBytes, 0.0, 0.0)

        assertEquals(
            "task-001",
            vm.state.value.photos
                .first()
                .taskId,
        )
    }

    @Test
    fun `addPhoto does not exceed MAX_PHOTOS`() {
        repeat(EvidencePackage.MAX_PHOTOS + 5) {
            vm.addPhoto(fakeBytes, 0.0, 0.0)
        }

        assertEquals(EvidencePackage.MAX_PHOTOS, vm.state.value.photos.size)
    }

    @Test
    fun `canAddMore is false when MAX_PHOTOS reached`() {
        repeat(EvidencePackage.MAX_PHOTOS) { vm.addPhoto(fakeBytes, 0.0, 0.0) }

        assertFalse(vm.state.value.canAddMore)
    }

    // ── removePhoto ───────────────────────────────────────────────────────────────

    @Test
    fun `removePhoto removes the correct photo by id`() {
        vm.addPhoto(fakeBytes, 1.0, 1.0)
        vm.addPhoto(fakeBytes, 2.0, 2.0)

        val idToRemove =
            vm.state.value.photos
                .first()
                .id
        vm.removePhoto(idToRemove)

        assertEquals(1, vm.state.value.photos.size)
        assertFalse(
            vm.state.value.photos
                .any { it.id == idToRemove },
        )
    }

    @Test
    fun `removePhoto with unknown id is a no-op`() {
        vm.addPhoto(fakeBytes, 0.0, 0.0)
        vm.removePhoto("does-not-exist")

        assertEquals(1, vm.state.value.photos.size)
    }

    // ── submitEvidence ────────────────────────────────────────────────────────────

    @Test
    fun `submitEvidence with no photos is a no-op`() =
        runTest {
            vm.submitEvidence()

            assertFalse(vm.state.value.isSubmitted)
            assertFalse(vm.state.value.isUploading)
        }

    @Test
    fun `submitEvidence queues photos and sets isSubmitted`() =
        runTest {
            vm.addPhoto(fakeBytes, 5.6, -0.18)
            vm.addPhoto(fakeBytes, 5.7, -0.19)

            vm.submitEvidence()

            val state = vm.state.value
            assertTrue(state.isSubmitted)
            assertFalse(state.isUploading)
            assertEquals(1, repository.submissions.size)
            assertEquals(
                2,
                repository.submissions
                    .first()
                    .photos.size,
            )
        }

    @Test
    fun `submitEvidence clears pendingBytes on success`() =
        runTest {
            vm.addPhoto(fakeBytes, 0.0, 0.0)
            vm.submitEvidence()

            assertTrue(
                vm.state.value.pendingBytes
                    .isEmpty(),
            )
        }

    @Test
    fun `submitEvidence sets error and stops uploading on failure`() =
        runTest {
            repository = FakeEvidenceRepository(shouldFail = true)
            vm = EvidenceCaptureViewModel(repository, NoOpPhotoQualityAnalyzer, NoOpAnalyticsService)
            vm.initTask("task-001")

            vm.addPhoto(fakeBytes, 0.0, 0.0)
            vm.submitEvidence()

            val state = vm.state.value
            assertFalse(state.isSubmitted)
            assertFalse(state.isUploading)
            assertNotNull(state.error)
        }

    @Test
    fun `submitEvidence called twice does not double-submit`() =
        runTest {
            vm.addPhoto(fakeBytes, 0.0, 0.0)
            vm.submitEvidence()

            assertTrue(vm.state.value.isSubmitted)

            vm.submitEvidence()
            assertEquals(1, repository.submissions.size)
        }

    // ── clearError ────────────────────────────────────────────────────────────────

    @Test
    fun `clearError removes error message`() =
        runTest {
            repository = FakeEvidenceRepository(shouldFail = true)
            vm = EvidenceCaptureViewModel(repository, NoOpPhotoQualityAnalyzer, NoOpAnalyticsService)
            vm.initTask("task-001")
            vm.addPhoto(fakeBytes, 0.0, 0.0)
            vm.submitEvidence()

            assertNotNull(vm.state.value.error)
            vm.clearError()

            assertNull(vm.state.value.error)
        }

    // ── canSubmit ────────────────────────────────────────────────────────────────

    @Test
    fun `canSubmit is false with no photos`() {
        assertFalse(vm.state.value.canSubmit)
    }

    @Test
    fun `canSubmit is true with at least one photo`() {
        vm.addPhoto(fakeBytes, 0.0, 0.0)
        assertTrue(vm.state.value.canSubmit)
    }

    // ── UploadStatus transitions ──────────────────────────────────────────────

    @Test
    fun `photos have LOCAL status after addPhoto`() {
        vm.addPhoto(fakeBytes, 5.6, -0.18)

        assertEquals(
            UploadStatus.LOCAL,
            vm.state.value.photos
                .first()
                .uploadStatus,
        )
    }

    @Test
    fun `submitEvidence transitions all photos from LOCAL to QUEUED`() =
        runTest {
            vm.addPhoto(fakeBytes, 5.6, -0.18)
            vm.addPhoto(fakeBytes, 5.7, -0.19)

            vm.submitEvidence()

            vm.state.value.photos.forEach { photo ->
                assertEquals(UploadStatus.QUEUED, photo.uploadStatus)
            }
        }
}
