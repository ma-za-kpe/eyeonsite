package com.maku.eyeonsite.data

data class PendingEvidenceEntry(
    val id: String,
    val taskId: String,
    val inspectorUid: String,
    val notes: String,
    val photoCount: Long,
    val createdAtMs: Long,
)

interface PendingEvidenceRepository {
    suspend fun insert(
        id: String,
        taskId: String,
        inspectorUid: String,
        notes: String,
        photoCount: Int,
    )

    suspend fun getAll(): List<PendingEvidenceEntry>

    suspend fun delete(id: String)
}

class NoOpPendingEvidenceRepository : PendingEvidenceRepository {
    override suspend fun insert(
        id: String,
        taskId: String,
        inspectorUid: String,
        notes: String,
        photoCount: Int,
    ) = Unit

    override suspend fun getAll(): List<PendingEvidenceEntry> = emptyList()

    override suspend fun delete(id: String) = Unit
}
