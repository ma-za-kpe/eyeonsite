@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.data

import com.maku.eyeonsite.db.EyeOnSiteDatabase
import com.maku.eyeonsite.domain.AttestationProvider
import com.maku.eyeonsite.domain.AttestationVerdict
import com.maku.eyeonsite.domain.DeviceAttestation
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
        attestation: DeviceAttestation,
    ) = withContext(Dispatchers.IO) {
        db.pendingEvidenceQueries.insert(
            id = id,
            task_id = taskId,
            inspector_uid = inspectorUid,
            notes = notes,
            photo_count = photoCount.toLong(),
            created_at_ms = System.currentTimeMillis(),
            attestation_provider = attestation.provider.name,
            attestation_verdict = attestation.verdict.name,
            attestation_checked_at_ms = attestation.checkedAtMs,
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
                        attestation =
                            DeviceAttestation(
                                provider =
                                    runCatching { AttestationProvider.valueOf(it.attestation_provider) }
                                        .getOrDefault(AttestationProvider.NONE),
                                verdict =
                                    runCatching { AttestationVerdict.valueOf(it.attestation_verdict) }
                                        .getOrDefault(AttestationVerdict.UNKNOWN),
                                checkedAtMs = it.attestation_checked_at_ms,
                            ),
                    )
                }
        }

    override suspend fun delete(id: String) =
        withContext(Dispatchers.IO) {
            db.pendingEvidenceQueries.delete(id)
            Unit
        }
}
