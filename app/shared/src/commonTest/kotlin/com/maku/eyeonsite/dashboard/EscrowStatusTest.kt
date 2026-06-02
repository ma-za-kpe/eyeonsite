package com.maku.eyeonsite.dashboard

import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.domain.EscrowStatus
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.TaskType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EscrowStatusTest {
    private fun makeTask(
        id: String,
        taskStatus: TaskStatus = TaskStatus.POSTED,
        escrowStatus: EscrowStatus = EscrowStatus.UNFUNDED,
        bountyUsd: Int = 30,
    ) = ClientTaskSummary(
        id = id,
        type = TaskType.CONSTRUCTION,
        location = "Accra",
        description = "test",
        status = taskStatus,
        bountyUsd = bountyUsd,
        createdAtMs = 0L,
        escrowStatus = escrowStatus,
    )

    @Test
    fun `ClientTaskSummary defaults to UNFUNDED escrow status`() {
        val task =
            ClientTaskSummary(
                id = "t1",
                type = TaskType.CONSTRUCTION,
                location = "Accra",
                description = "test",
                status = TaskStatus.POSTED,
                bountyUsd = 30,
                createdAtMs = 0L,
            )
        assertEquals(EscrowStatus.UNFUNDED, task.escrowStatus)
    }

    @Test
    fun `HELD escrow status indicates funds are secured`() {
        val task = makeTask("t1", TaskStatus.IN_PROGRESS, EscrowStatus.HELD)
        assertEquals(EscrowStatus.HELD, task.escrowStatus)
    }

    @Test
    fun `DISPUTED escrow status reflects active dispute`() {
        val task = makeTask("t1", TaskStatus.DISPUTED, EscrowStatus.DISPUTED)
        assertEquals(EscrowStatus.DISPUTED, task.escrowStatus)
    }

    @Test
    fun `RELEASED escrow status reflects payout complete`() {
        val task = makeTask("t1", TaskStatus.ACCEPTED, EscrowStatus.RELEASED)
        assertEquals(EscrowStatus.RELEASED, task.escrowStatus)
    }

    @Test
    fun `FROZEN escrow status distinguishes from DISPUTED`() {
        val frozen = makeTask("t1", TaskStatus.DISPUTED, EscrowStatus.FROZEN)
        val disputed = makeTask("t2", TaskStatus.DISPUTED, EscrowStatus.DISPUTED)
        assertEquals(EscrowStatus.FROZEN, frozen.escrowStatus)
        assertEquals(EscrowStatus.DISPUTED, disputed.escrowStatus)
        assertFalse(frozen.escrowStatus == disputed.escrowStatus)
    }

    @Test
    fun `all EscrowStatus values can be assigned`() {
        EscrowStatus.entries.forEach { status ->
            val task = makeTask("t1", escrowStatus = status)
            assertEquals(status, task.escrowStatus)
        }
    }

    @Test
    fun `RELEASE_PENDING indicates server is processing payout`() {
        val task = makeTask("t1", TaskStatus.ACCEPTED, EscrowStatus.RELEASE_PENDING)
        assertEquals(EscrowStatus.RELEASE_PENDING, task.escrowStatus)
        assertTrue(task.escrowStatus != EscrowStatus.RELEASED)
    }
}
