@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.maku.eyeonsite.domain.EvidencePhoto
import com.maku.eyeonsite.sync.UploadSyncScheduler
import com.maku.eyeonsite.util.generateUlid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("InjectDispatcher", "IgnoredReturnValue")
class FirestoreEvidenceRepository(
    private val context: Context,
    private val pendingUploadRepo: PendingUploadRepository,
    private val pendingEvidenceRepo: PendingEvidenceRepository,
) : EvidenceRepository {
    override suspend fun submitEvidence(
        taskId: String,
        photos: List<EvidencePhoto>,
        pendingBytes: Map<String, ByteArray>,
        notes: String,
    ): Result<Unit> =
        runCatching {
            val inspectorUid =
                FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?.uid
                    .orEmpty()
            val evidenceId = generateUlid()

            withContext(Dispatchers.IO) {
                for (photo in photos) {
                    val bytes = pendingBytes[photo.id] ?: continue
                    val file = File(context.filesDir, "evidence/$taskId/${photo.id}.jpg")
                    file.parentFile?.mkdirs()
                    file.writeBytes(bytes)

                    pendingUploadRepo.enqueue(
                        id = generateUlid(),
                        taskId = taskId,
                        photoId = photo.id,
                        filePath = file.absolutePath,
                        latitude = photo.latitude,
                        longitude = photo.longitude,
                        capturedAtMs = photo.capturedAtMs,
                    )
                }

                pendingEvidenceRepo.insert(
                    id = evidenceId,
                    taskId = taskId,
                    inspectorUid = inspectorUid,
                    notes = notes,
                    photoCount = photos.size,
                )
            }

            UploadSyncScheduler.triggerImmediateSync(context)
        }
}
