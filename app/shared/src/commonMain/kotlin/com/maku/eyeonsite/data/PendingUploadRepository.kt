package com.maku.eyeonsite.data

import com.maku.eyeonsite.domain.UploadStatus

data class PendingUploadEntry(
    val id: String,
    val taskId: String,
    val photoId: String,
    val filePath: String,
    val latitude: Double,
    val longitude: Double,
    val capturedAtMs: Long,
    val retryCount: Long,
    val storageUrl: String?,
    val uploadStatus: UploadStatus = UploadStatus.QUEUED,
)

interface PendingUploadRepository {
    suspend fun enqueue(
        id: String,
        taskId: String,
        photoId: String,
        filePath: String,
        latitude: Double,
        longitude: Double,
        capturedAtMs: Long,
    )

    /** All entries, including already-uploaded ones awaiting evidence commit. */
    suspend fun getAll(): List<PendingUploadEntry>

    /** Only entries that have not yet been uploaded to Firebase Storage. */
    suspend fun getPendingUpload(): List<PendingUploadEntry>

    suspend fun getByTaskId(taskId: String): List<PendingUploadEntry>

    suspend fun setStorageUrl(
        id: String,
        storageUrl: String,
    )

    suspend fun setUploading(id: String)

    suspend fun incrementRetry(id: String)

    suspend fun delete(id: String)

    suspend fun deleteByTaskId(taskId: String)
}

class NoOpPendingUploadRepository : PendingUploadRepository {
    override suspend fun enqueue(
        id: String,
        taskId: String,
        photoId: String,
        filePath: String,
        latitude: Double,
        longitude: Double,
        capturedAtMs: Long,
    ) = Unit

    override suspend fun getAll(): List<PendingUploadEntry> = emptyList()

    override suspend fun getPendingUpload(): List<PendingUploadEntry> = emptyList()

    override suspend fun getByTaskId(taskId: String): List<PendingUploadEntry> = emptyList()

    override suspend fun setStorageUrl(
        id: String,
        storageUrl: String,
    ) = Unit

    override suspend fun setUploading(id: String) = Unit

    override suspend fun incrementRetry(id: String) = Unit

    override suspend fun delete(id: String) = Unit

    override suspend fun deleteByTaskId(taskId: String) = Unit
}
