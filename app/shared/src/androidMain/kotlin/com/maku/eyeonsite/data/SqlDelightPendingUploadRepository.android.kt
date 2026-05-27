@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.data

import com.maku.eyeonsite.db.EyeOnSiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("InjectDispatcher")
class SqlDelightPendingUploadRepository(
    private val db: EyeOnSiteDatabase,
) : PendingUploadRepository {
    override suspend fun enqueue(
        id: String,
        taskId: String,
        photoId: String,
        filePath: String,
        latitude: Double,
        longitude: Double,
        capturedAtMs: Long,
    ) = withContext(Dispatchers.IO) {
        db.pendingUploadQueries.insert(
            id = id,
            task_id = taskId,
            photo_id = photoId,
            file_path = filePath,
            latitude = latitude,
            longitude = longitude,
            captured_at_ms = capturedAtMs,
            created_at_ms = System.currentTimeMillis(),
        )
        Unit
    }

    override suspend fun getAll(): List<PendingUploadEntry> =
        withContext(Dispatchers.IO) {
            db.pendingUploadQueries
                .getAll()
                .executeAsList()
                .map { it.toEntry() }
        }

    override suspend fun getPendingUpload(): List<PendingUploadEntry> =
        withContext(Dispatchers.IO) {
            db.pendingUploadQueries
                .getPendingUpload()
                .executeAsList()
                .map { it.toEntry() }
        }

    override suspend fun getByTaskId(taskId: String): List<PendingUploadEntry> =
        withContext(Dispatchers.IO) {
            db.pendingUploadQueries
                .getByTaskId(taskId)
                .executeAsList()
                .map { it.toEntry() }
        }

    override suspend fun setStorageUrl(
        id: String,
        storageUrl: String,
    ) = withContext(Dispatchers.IO) {
        db.pendingUploadQueries.setStorageUrl(storage_url = storageUrl, id = id)
        Unit
    }

    override suspend fun incrementRetry(id: String) =
        withContext(Dispatchers.IO) {
            db.pendingUploadQueries.incrementRetry(id)
            Unit
        }

    override suspend fun delete(id: String) =
        withContext(Dispatchers.IO) {
            db.pendingUploadQueries.delete(id)
            Unit
        }

    override suspend fun deleteByTaskId(taskId: String) =
        withContext(Dispatchers.IO) {
            db.pendingUploadQueries.deleteByTaskId(taskId)
            Unit
        }
}

private fun com.maku.eyeonsite.db.Pending_upload.toEntry() =
    PendingUploadEntry(
        id = id,
        taskId = task_id,
        photoId = photo_id,
        filePath = file_path,
        latitude = latitude,
        longitude = longitude,
        capturedAtMs = captured_at_ms,
        retryCount = retry_count,
        storageUrl = storage_url,
    )
