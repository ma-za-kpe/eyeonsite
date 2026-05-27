package com.maku.eyeonsite.data

data class PendingUploadEntry(
    val id: String,
    val taskId: String,
    val photoId: String,
    val filePath: String,
    val latitude: Double,
    val longitude: Double,
    val capturedAtMs: Long,
    val retryCount: Long,
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

    suspend fun getAll(): List<PendingUploadEntry>

    suspend fun getByTaskId(taskId: String): List<PendingUploadEntry>

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

    override suspend fun getByTaskId(taskId: String): List<PendingUploadEntry> = emptyList()

    override suspend fun incrementRetry(id: String) = Unit

    override suspend fun delete(id: String) = Unit

    override suspend fun deleteByTaskId(taskId: String) = Unit
}
