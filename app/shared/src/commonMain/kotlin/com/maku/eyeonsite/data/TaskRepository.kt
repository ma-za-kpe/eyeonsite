package com.maku.eyeonsite.data

import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.VerificationTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface TaskRepository {
    suspend fun postTask(task: VerificationTask): String

    fun observeTask(taskId: String): Flow<TaskStatus>
}

class NoOpTaskRepository : TaskRepository {
    override suspend fun postTask(task: VerificationTask): String = "stub-${task.type.name}"

    override fun observeTask(taskId: String): Flow<TaskStatus> = emptyFlow()
}
