@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.ui.evidence

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.maku.eyeonsite.domain.EvidencePhoto
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseStorageEvidenceUploader : EvidenceUploader {
    private val storage = FirebaseStorage.getInstance()

    override suspend fun upload(
        photo: EvidencePhoto,
        bytes: ByteArray,
    ): Result<String> =
        runCatching {
            val ref = storage.reference.child("evidence/${photo.taskId}/${photo.id}.jpg")
            val metadata =
                StorageMetadata
                    .Builder()
                    .setContentType("image/jpeg")
                    .build()
            suspendCancellableCoroutine { cont ->
                ref
                    .putBytes(bytes, metadata)
                    .addOnSuccessListener {
                        ref.downloadUrl
                            .addOnSuccessListener { uri -> cont.resume(uri.toString()) }
                            .addOnFailureListener { cont.resumeWithException(it) }
                    }.addOnFailureListener { cont.resumeWithException(it) }
            }
        }
}
