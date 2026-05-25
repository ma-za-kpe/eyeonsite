package com.maku.eyeonsite.evidence

import com.maku.eyeonsite.domain.EvidencePackage
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.domain.UploadStatus
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvidencePackageTest {
    private fun fakePhoto(id: String = "p1") =
        EvidencePhoto(
            id = id,
            taskId = "task-1",
            latitude = 5.603717,
            longitude = -0.186964,
            capturedAtMs = 0L,
        )

    @Test
    fun `isValid returns false for empty photo list`() {
        val pkg = EvidencePackage(taskId = "t1", photos = emptyList())
        assertFalse(pkg.isValid())
    }

    @Test
    fun `isValid returns true for a single photo`() {
        val pkg = EvidencePackage(taskId = "t1", photos = listOf(fakePhoto()))
        assertTrue(pkg.isValid())
    }

    @Test
    fun `isValid returns true for exactly MAX_PHOTOS`() {
        val photos = (1..EvidencePackage.MAX_PHOTOS).map { fakePhoto("p$it") }
        val pkg = EvidencePackage(taskId = "t1", photos = photos)
        assertTrue(pkg.isValid())
    }

    @Test
    fun `isValid returns false when exceeding MAX_PHOTOS`() {
        val photos = (1..EvidencePackage.MAX_PHOTOS + 1).map { fakePhoto("p$it") }
        val pkg = EvidencePackage(taskId = "t1", photos = photos)
        assertFalse(pkg.isValid())
    }

    @Test
    fun `EvidencePhoto defaults to LOCAL upload status`() {
        val photo = fakePhoto()
        assertTrue(photo.uploadStatus == UploadStatus.LOCAL)
    }

    @Test
    fun `EvidencePhoto with GPS has non-zero coordinates`() {
        val photo = fakePhoto().copy(latitude = 5.603717, longitude = -0.186964)
        assertTrue(photo.latitude != 0.0)
        assertTrue(photo.longitude != 0.0)
    }
}
