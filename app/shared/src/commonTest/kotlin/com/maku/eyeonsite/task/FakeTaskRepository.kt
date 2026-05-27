package com.maku.eyeonsite.task

import com.maku.eyeonsite.data.TaskRepository
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.VerificationTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeTaskRepository : TaskRepository {
    private val statusFlow = MutableSharedFlow<TaskStatus>()

    override suspend fun postTask(task: VerificationTask): String = "test-task-id"

    override fun observeTask(taskId: String): Flow<TaskStatus> = statusFlow

    suspend fun emitStatus(status: TaskStatus) {
        statusFlow.emit(status)
    }
}
