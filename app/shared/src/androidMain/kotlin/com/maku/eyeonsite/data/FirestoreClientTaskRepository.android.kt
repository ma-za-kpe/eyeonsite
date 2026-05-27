@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.maku.eyeonsite.db.EyeOnSiteDatabase
import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.dashboard.ClientTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private data class TaskUpsertArgs(
    val id: String,
    val type: String,
    val location: String,
    val description: String,
    val status: String,
    val bountyUsd: Long,
    val createdAtMs: Long,
    val inspectorName: String?,
)

@Suppress("ReturnCount")
private fun DocumentSnapshot.toUpsertArgs(fallbackNow: Long): TaskUpsertArgs? {
    val type = getString("type") ?: return null
    val location = getString("locationLabel") ?: getString("location") ?: return null
    val description = getString("description") ?: return null
    val status = getString("status") ?: return null
    val bounty = getDouble("bountyUsd")?.toLong() ?: return null
    return TaskUpsertArgs(
        id = id,
        type = type,
        location = location,
        description = description,
        status = status,
        bountyUsd = bounty,
        createdAtMs = getLong("createdAtMs") ?: fallbackNow,
        inspectorName = getString("inspectorName"),
    )
}

@Suppress("InjectDispatcher", "IgnoredReturnValue")
class FirestoreClientTaskRepository(
    private val db: EyeOnSiteDatabase,
) : ClientTaskRepository {
    private val fs = FirebaseFirestore.getInstance()

    override fun observeTasksForClient(clientUid: String): Flow<List<ClientTaskSummary>> =
        callbackFlow {
            val now = System.currentTimeMillis()

            val registration =
                fs
                    .collection("tasks")
                    .whereEqualTo("clientUid", clientUid)
                    .addSnapshotListener { snap, err ->
                        if (err != null || snap == null) return@addSnapshotListener
                        val args = snap.documents.mapNotNull { it.toUpsertArgs(now) }
                        db.cachedTaskQueries.transaction {
                            db.cachedTaskQueries.deleteAll()
                            for (a in args) {
                                db.cachedTaskQueries.upsert(
                                    id = a.id,
                                    type = a.type,
                                    location = a.location,
                                    description = a.description,
                                    status = a.status,
                                    bounty_usd = a.bountyUsd,
                                    created_at_ms = a.createdAtMs,
                                    inspector_name = a.inspectorName,
                                    updated_at_ms = now,
                                )
                            }
                        }
                    }

            launch {
                db.cachedTaskQueries
                    .getAllForClient()
                    .asFlow()
                    .mapToList(Dispatchers.IO)
                    .map { rows -> rows.map { it.toDomain() } }
                    .collect { trySend(it) }
            }

            awaitClose { registration.remove() }
        }
}

private fun com.maku.eyeonsite.db.Cached_task.toDomain(): ClientTaskSummary =
    ClientTaskSummary(
        id = id,
        type = runCatching { TaskType.valueOf(type) }.getOrDefault(TaskType.CONSTRUCTION),
        location = location,
        description = description,
        status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.POSTED),
        bountyUsd = bounty_usd.toInt(),
        createdAtMs = created_at_ms,
        inspectorName = inspector_name,
    )
