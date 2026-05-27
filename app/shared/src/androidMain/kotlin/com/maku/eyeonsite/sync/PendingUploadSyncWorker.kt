package com.maku.eyeonsite.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maku.eyeonsite.data.PendingUploadEntry
import com.maku.eyeonsite.data.PendingUploadRepository
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.ui.evidence.EvidenceUploader
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class PendingUploadSyncWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params),
    KoinComponent {
    private val repo: PendingUploadRepository by inject()
    private val uploader: EvidenceUploader by inject()

    override suspend fun doWork(): Result {
        val pending = repo.getAll()
        if (pending.isEmpty()) return Result.success()

        var hasFailure = false
        pending.forEach { entry ->
            val succeeded = processEntry(entry)
            if (!succeeded) hasFailure = true
        }
        return if (hasFailure) Result.retry() else Result.success()
    }

    private suspend fun processEntry(entry: PendingUploadEntry): Boolean {
        val file = File(entry.filePath)
        if (!file.exists()) {
            repo.delete(entry.id)
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
            repo.delete(entry.id)
            true
        } else {
            if (entry.retryCount >= MAX_RETRIES) repo.delete(entry.id) else repo.incrementRetry(entry.id)
            false
        }
    }

    companion object {
        const val WORK_NAME = "pending_upload_sync"
        private const val MAX_RETRIES = 5L
    }
}
