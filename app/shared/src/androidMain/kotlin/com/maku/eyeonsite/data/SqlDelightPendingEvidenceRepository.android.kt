@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.data

import com.maku.eyeonsite.db.EyeOnSiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("InjectDispatcher", "IgnoredReturnValue")
class SqlDelightPendingEvidenceRepository(
    private val db: EyeOnSiteDatabase,
) : PendingEvidenceRepository {
    override suspend fun insert(
        id: String,
        taskId: String,
        inspectorUid: String,
        notes: String,
        photoCount: Int,
    ) = withContext(Dispatchers.IO) {
        db.pendingEvidenceQueries.insert(
            id = id,
            task_id = taskId,
            inspector_uid = inspectorUid,
            notes = notes,
            photo_count = photoCount.toLong(),
            created_at_ms = System.currentTimeMillis(),
        )
        Unit
    }

    override suspend fun getAll(): List<PendingEvidenceEntry> =
        withContext(Dispatchers.IO) {
            db.pendingEvidenceQueries
                .getAll()
                .executeAsList()
                .map {
                    PendingEvidenceEntry(
                        id = it.id,
                        taskId = it.task_id,
                        inspectorUid = it.inspector_uid,
                        notes = it.notes,
                        photoCount = it.photo_count,
                        createdAtMs = it.created_at_ms,
                    )
                }
        }

    override suspend fun delete(id: String) =
        withContext(Dispatchers.IO) {
            db.pendingEvidenceQueries.delete(id)
            Unit
        }
}
