package com.maku.eyeonsite.payment

import com.maku.eyeonsite.domain.EscrowLedgerEntry
import com.maku.eyeonsite.domain.EscrowLedgerEntryStatus
import com.maku.eyeonsite.domain.EscrowLedgerEntryType
import com.maku.eyeonsite.domain.EscrowSnapshot
import com.maku.eyeonsite.domain.EscrowStatus
import com.maku.eyeonsite.domain.MoneyProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EscrowLedgerTest {
    private val holdEntry =
        EscrowLedgerEntry(
            id = "e1",
            taskId = "task-001",
            type = EscrowLedgerEntryType.ESCROW_HOLD,
            status = EscrowLedgerEntryStatus.POSTED,
            provider = MoneyProvider.STRIPE,
            amountUsdCents = 4500L,
            createdAtMs = 1_748_000_000_000L,
        )
    private val releaseEntry =
        EscrowLedgerEntry(
            id = "e2",
            taskId = "task-001",
            type = EscrowLedgerEntryType.ESCROW_RELEASE,
            status = EscrowLedgerEntryStatus.POSTED,
            provider = MoneyProvider.STRIPE,
            amountUsdCents = 4500L,
            createdAtMs = 1_748_000_001_000L,
        )

    // ── Domain invariants ─────────────────────────────────────────────────────

    @Test
    fun `EscrowSnapshot with zero amount always has balanced fees`() {
        val snap = EscrowSnapshot(amountUsdCents = 0L)
        assertTrue(snap.hasBalancedFeeBreakdown())
    }

    @Test
    fun `EscrowSnapshot fees that sum to total amount pass balance check`() {
        val snap =
            EscrowSnapshot(
                amountUsdCents = 4500L,
                inspectorPayoutUsdCents = 3600L,
                platformFeeUsdCents = 675L,
                escrowFeeUsdCents = 225L,
            )
        assertTrue(snap.hasBalancedFeeBreakdown())
    }

    @Test
    fun `EscrowSnapshot fees that do not sum to total fail balance check`() {
        val snap =
            EscrowSnapshot(
                amountUsdCents = 4500L,
                inspectorPayoutUsdCents = 3600L,
                platformFeeUsdCents = 600L,
                escrowFeeUsdCents = 200L,
            )
        assertFalse(snap.hasBalancedFeeBreakdown())
    }

    // ── Repository contract ───────────────────────────────────────────────────

    @Test
    fun `NoOpPaymentRepository getLedger returns empty list`() =
        runTest {
            val entries = NoOpPaymentRepository().getLedger("task-001").first()
            assertTrue(entries.isEmpty())
        }

    @Test
    fun `FakeFundTaskRepository getLedger with seeded entries returns entries for taskId`() =
        runTest {
            val repo = FakeFundTaskRepository(ledgerEntries = listOf(holdEntry, releaseEntry))

            val entries = repo.getLedger("task-001").first()

            assertEquals(2, entries.size)
            assertTrue(entries.all { it.taskId == "task-001" })
        }

    @Test
    fun `FakeFundTaskRepository getLedger for unknown taskId returns empty list`() =
        runTest {
            val repo = FakeFundTaskRepository(ledgerEntries = listOf(holdEntry, releaseEntry))

            val entries = repo.getLedger("task-999").first()

            assertTrue(entries.isEmpty())
        }

    @Test
    fun `FakeFundTaskRepository getLedger entries are ordered by createdAtMs ascending`() =
        runTest {
            val repo = FakeFundTaskRepository(ledgerEntries = listOf(releaseEntry, holdEntry))

            val entries = repo.getLedger("task-001").first()

            assertEquals(listOf(1_748_000_000_000L, 1_748_000_001_000L), entries.map { it.createdAtMs })
        }

    // ── EscrowStatus progression ──────────────────────────────────────────────

    @Test
    fun `HELD escrow snapshot is funded`() {
        val snap = EscrowSnapshot(status = EscrowStatus.HELD, amountUsdCents = 4500L)
        assertEquals(EscrowStatus.HELD, snap.status)
        assertEquals(4500L, snap.amountUsdCents)
    }
}
