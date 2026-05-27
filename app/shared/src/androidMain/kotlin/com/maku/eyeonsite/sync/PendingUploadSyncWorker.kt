package com.maku.eyeonsite.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.maku.eyeonsite.data.PendingEvidenceEntry
import com.maku.eyeonsite.data.PendingEvidenceRepository
import com.maku.eyeonsite.data.PendingUploadEntry
import com.maku.eyeonsite.data.PendingUploadRepository
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.ui.evidence.EvidenceUploader
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("InjectDispatcher", "IgnoredReturnValue")
class PendingUploadSyncWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params),
    KoinComponent {
    private val uploadRepo: PendingUploadRepository by inject()
    private val evidenceRepo: PendingEvidenceRepository by inject()
    private val uploader: EvidenceUploader by inject()

    override suspend fun doWork(): Result {
        // Phase 1 — upload any photos not yet on Firebase Storage.
        val toUpload = uploadRepo.getPendingUpload()
        var hasFailure = false
        toUpload.forEach { entry ->
            if (!processUpload(entry)) hasFailure = true
        }
        if (hasFailure) return Result.retry()

        // Phase 2 — commit evidence documents whose photos are all uploaded.
        val pendingEvidence = evidenceRepo.getAll()
        for (evidence in pendingEvidence) {
            val photos = uploadRepo.getByTaskId(evidence.taskId)
            if (photos.any { it.storageUrl == null }) continue // still uploading
            if (!commitEvidenceToFirestore(evidence, photos)) return Result.retry()
            evidenceRepo.delete(evidence.id)
            uploadRepo.deleteByTaskId(evidence.taskId)
        }

        return Result.success()
    }

    private suspend fun processUpload(entry: PendingUploadEntry): Boolean {
        val file = File(entry.filePath)
        if (!file.exists()) {
            uploadRepo.delete(entry.id)
            return true
        }
        val photo =
            EvidencePhoto(
                id = entry.photoId,
                taskId = entry.taskId,
                latitude = entry.latitude,
                longitude = entry.longitude,
                capturedAtMs = entry.capturedAtMs,
            )
        val result = uploader.upload(photo, file.readBytes())
        return if (result.isSuccess) {
            uploadRepo.setStorageUrl(entry.id, result.getOrThrow())
            true
        } else {
            if (entry.retryCount >= MAX_RETRIES) {
                uploadRepo.delete(entry.id)
            } else {
                uploadRepo.incrementRetry(entry.id)
            }
            false
        }
    }

    @Suppress("ReturnCount")
    private suspend fun commitEvidenceToFirestore(
        evidence: PendingEvidenceEntry,
        photos: List<PendingUploadEntry>,
    ): Boolean {
        val db = FirebaseFirestore.getInstance()
        val photoMaps =
            photos.map { p ->
                mapOf(
                    "id" to p.photoId,
                    "latitude" to p.latitude,
                    "longitude" to p.longitude,
                    "capturedAtMs" to p.capturedAtMs,
                    "storageUrl" to (p.storageUrl ?: ""),
                )
            }
        val evidenceData: Map<String, Any> =
            mapOf(
                "taskId" to evidence.taskId,
                "inspectorUid" to evidence.inspectorUid,
                "photos" to photoMaps,
                "notes" to evidence.notes,
                "submittedAtMs" to FieldValue.serverTimestamp(),
                "status" to "PENDING",
            )
        return runCatching {
            suspendCancellableCoroutine { cont ->
                val batch = db.batch()
                batch.set(db.collection("evidence").document(evidence.id), evidenceData)
                batch.update(
                    db.document("tasks/${evidence.taskId}"),
                    "status",
                    "EVIDENCE_SUBMITTED",
                    "evidenceId",
                    evidence.id,
                )
                batch
                    .commit()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
        }.isSuccess
    }

    companion object {
        const val WORK_NAME = "pending_upload_sync"
        private const val MAX_RETRIES = 5L
    }
}
