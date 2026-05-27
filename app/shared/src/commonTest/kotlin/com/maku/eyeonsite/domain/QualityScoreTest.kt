package com.maku.eyeonsite.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualityScoreTest {
    private fun photo(
        id: String,
        status: UploadStatus = UploadStatus.UPLOADED,
    ) = EvidencePhoto(
        id = id,
        taskId = "task-1",
        latitude = 5.55,
        longitude = -0.22,
        capturedAtMs = 1_748_000_000_000L,
        uploadStatus = status,
    )

    private fun answer(
        id: String,
        yes: Boolean,
    ) = ChecklistAnswer(itemId = id, boolValue = yes)

    // ── edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `empty photos returns 0`() {
        assertEquals(0f, computeQualityScore(emptyList()))
    }

    @Test
    fun `result is always in 0-1 range`() {
        val score = computeQualityScore(listOf(photo("p1")), emptyList())
        assertTrue(score in 0f..1f)
    }

    // ── upload ratio component ────────────────────────────────────────────────

    @Test
    fun `all uploaded photos give maximum upload score component`() {
        val photos = (1..5).map { photo("p$it", UploadStatus.UPLOADED) }
        val score = computeQualityScore(photos)
        assertTrue(score > 0.5f, "Expected score > 0.5 but was $score")
    }

    @Test
    fun `failed upload reduces score`() {
        val allUploaded = (1..5).map { photo("p$it", UploadStatus.UPLOADED) }
        val someFailed =
            listOf(
                photo("p1", UploadStatus.UPLOADED),
                photo("p2", UploadStatus.FAILED),
                photo("p3", UploadStatus.FAILED),
                photo("p4", UploadStatus.UPLOADED),
                photo("p5", UploadStatus.UPLOADED),
            )
        assertTrue(
            computeQualityScore(someFailed) < computeQualityScore(allUploaded),
            "Failed uploads should reduce score",
        )
    }

    @Test
    fun `single uploaded photo has lower score than five uploaded`() {
        val one = listOf(photo("p1"))
        val five = (1..5).map { photo("p$it") }
        assertTrue(computeQualityScore(one) < computeQualityScore(five))
    }

    // ── photo count component ─────────────────────────────────────────────────

    @Test
    fun `max photos gives maximum photo count component`() {
        val maxPhotos = (1..EvidencePackage.MAX_PHOTOS).map { photo("p$it") }
        val fewPhotos = listOf(photo("p1"))
        assertTrue(computeQualityScore(maxPhotos) > computeQualityScore(fewPhotos))
    }

    @Test
    fun `more photos than max does not exceed 1`() {
        val overMax = (1..20).map { photo("p$it") }
        assertTrue(computeQualityScore(overMax) <= 1f)
    }

    // ── checklist component ───────────────────────────────────────────────────

    @Test
    fun `empty checklist does not penalize score`() {
        val photos = listOf(photo("p1"))
        val withEmpty = computeQualityScore(photos, emptyList())
        val withNone = computeQualityScore(photos)
        assertEquals(withNone, withEmpty)
    }

    @Test
    fun `fully answered checklist scores higher than partially answered`() {
        val photos = (1..3).map { photo("p$it") }
        val full = (1..4).map { answer("q$it", true) }
        val partial = listOf(answer("q1", true), ChecklistAnswer(itemId = "q2"))
        assertTrue(computeQualityScore(photos, full) > computeQualityScore(photos, partial))
    }

    @Test
    fun `unanswered checklist reduces score`() {
        val photos = listOf(photo("p1", UploadStatus.UPLOADED))
        val answered = listOf(answer("q1", true), answer("q2", false))
        val unanswered = listOf(ChecklistAnswer(itemId = "q1"), ChecklistAnswer(itemId = "q2"))
        assertTrue(computeQualityScore(photos, answered) > computeQualityScore(photos, unanswered))
    }

    // ── known values ──────────────────────────────────────────────────────────

    @Test
    fun `single uploaded photo with no checklist scores around 0_85`() {
        val photos = listOf(photo("p1", UploadStatus.UPLOADED))
        val score = computeQualityScore(photos, emptyList())
        // uploadRatio=1.0*0.6 + photoCountScore=(1/10)*0.25 + checklistScore=1.0*0.15 = 0.6+0.025+0.15 = 0.775
        assertTrue(score > 0.5f && score < 1f, "Score was $score")
    }

    @Test
    fun `perfect score requires all uploads max photos and full checklist`() {
        val photos = (1..EvidencePackage.MAX_PHOTOS).map { photo("p$it", UploadStatus.UPLOADED) }
        val answers = (1..5).map { answer("q$it", true) }
        val score = computeQualityScore(photos, answers)
        assertEquals(1f, score)
    }
}
