@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.VerificationTask
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val DEFAULT_RADIUS_KM = 10.0

class FirestoreTaskRepository : TaskRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun postTask(task: VerificationTask): String {
        val doc = db.collection("tasks").document()
        val uid = auth.currentUser?.uid.orEmpty()
        val data =
            mapOf(
                "status" to "POSTED",
                "type" to task.type.name,
                "location" to GeoPoint(task.locationLat, task.locationLng),
                "locationLabel" to task.location,
                "locationRadiusKm" to DEFAULT_RADIUS_KM,
                "bountyUsd" to task.bountyUsd.toDouble(),
                "clientUid" to uid,
                "description" to task.description,
                "createdAtMs" to System.currentTimeMillis(),
            )
        return suspendCancellableCoroutine { cont ->
            doc
                .set(data)
                .addOnSuccessListener { cont.resume(doc.id) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    @Suppress("UnreachableCode")
    override fun observeTask(taskId: String): Flow<TaskStatus> =
        callbackFlow {
            val reg =
                db.document("tasks/$taskId").addSnapshotListener { snap, err ->
                    if (err != null) {
                        close(err)
                    } else {
                        val statusStr = snap?.getString("status") ?: return@addSnapshotListener
                        val status =
                            runCatching { TaskStatus.valueOf(statusStr) }.getOrNull()
                                ?: return@addSnapshotListener
                        trySend(status)
                    }
                }
            awaitClose { reg.remove() }
        }
}
