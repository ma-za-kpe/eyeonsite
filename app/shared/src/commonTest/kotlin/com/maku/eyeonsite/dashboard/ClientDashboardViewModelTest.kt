package com.maku.eyeonsite.dashboard

import com.maku.eyeonsite.domain.ClientTaskSummary
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.dashboard.ClientDashboardUiState
import com.maku.eyeonsite.ui.dashboard.ClientDashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ClientDashboardViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeClientTaskRepository
    private lateinit var vm: ClientDashboardViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeClientTaskRepository()
        vm = ClientDashboardViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeTask(
        id: String,
        status: TaskStatus,
        bountyUsd: Int = 30,
        type: TaskType = TaskType.CONSTRUCTION,
    ) = ClientTaskSummary(
        id = id,
        type = type,
        location = "Accra",
        description = "Test task",
        status = status,
        bountyUsd = bountyUsd,
        createdAtMs = 1_748_000_000_000L,
    )

    // ── loadDashboard ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        assertIs<ClientDashboardUiState.Loading>(vm.state.value)
    }

    @Test
    fun `empty repository produces Empty state`() =
        runTest {
            vm.loadDashboard("uid-1")
            assertIs<ClientDashboardUiState.Empty>(vm.state.value)
        }

    @Test
    fun `tasks produce Dashboard state`() =
        runTest {
            repo = FakeClientTaskRepository(listOf(makeTask("t1", TaskStatus.IN_PROGRESS)))
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
        }

    @Test
    fun `repository failure produces Error state`() =
        runTest {
            repo = FakeClientTaskRepository(shouldFail = true)
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Error>(vm.state.value)
            assertEquals("Network error", state.message)
        }

    // ── task categorisation ───────────────────────────────────────────────────

    @Test
    fun `activeTasks contains IN_PROGRESS INSPECTOR_MATCHED ESCROW_FUNDED`() =
        runTest {
            val tasks =
                listOf(
                    makeTask("t1", TaskStatus.IN_PROGRESS),
                    makeTask("t2", TaskStatus.INSPECTOR_MATCHED),
                    makeTask("t3", TaskStatus.ESCROW_FUNDED),
                    makeTask("t4", TaskStatus.POSTED),
                )
            repo = FakeClientTaskRepository(tasks)
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(3, state.activeTasks.size)
        }

    @Test
    fun `pendingReview contains EVIDENCE_SUBMITTED and UNDER_REVIEW`() =
        runTest {
            val tasks =
                listOf(
                    makeTask("t1", TaskStatus.EVIDENCE_SUBMITTED),
                    makeTask("t2", TaskStatus.UNDER_REVIEW),
                    makeTask("t3", TaskStatus.IN_PROGRESS),
                )
            repo = FakeClientTaskRepository(tasks)
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(2, state.pendingReview.size)
        }

    @Test
    fun `completedTasks contains ACCEPTED DISPUTED REVISION_REQUESTED`() =
        runTest {
            val tasks =
                listOf(
                    makeTask("t1", TaskStatus.ACCEPTED),
                    makeTask("t2", TaskStatus.DISPUTED),
                    makeTask("t3", TaskStatus.REVISION_REQUESTED),
                    makeTask("t4", TaskStatus.IN_PROGRESS),
                )
            repo = FakeClientTaskRepository(tasks)
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(3, state.completedTasks.size)
        }

    @Test
    fun `draftTasks contains DRAFT and POSTED`() =
        runTest {
            val tasks =
                listOf(
                    makeTask("t1", TaskStatus.DRAFT),
                    makeTask("t2", TaskStatus.POSTED),
                    makeTask("t3", TaskStatus.ACCEPTED),
                )
            repo = FakeClientTaskRepository(tasks)
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(2, state.draftTasks.size)
        }

    @Test
    fun `single task appears in exactly one category`() =
        runTest {
            repo = FakeClientTaskRepository(listOf(makeTask("t1", TaskStatus.IN_PROGRESS)))
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            val total =
                state.activeTasks.size + state.pendingReview.size +
                    state.completedTasks.size + state.draftTasks.size
            assertEquals(1, total)
        }

    // ── escrow held ───────────────────────────────────────────────────────────

    @Test
    fun `totalEscrowHeldUsd sums active and review task bounties`() =
        runTest {
            val tasks =
                listOf(
                    makeTask("t1", TaskStatus.ESCROW_FUNDED, bountyUsd = 40),
                    makeTask("t2", TaskStatus.IN_PROGRESS, bountyUsd = 30),
                    makeTask("t3", TaskStatus.EVIDENCE_SUBMITTED, bountyUsd = 50),
                    makeTask("t4", TaskStatus.ACCEPTED, bountyUsd = 100),
                )
            repo = FakeClientTaskRepository(tasks)
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(120, state.totalEscrowHeldUsd)
        }

    @Test
    fun `ACCEPTED tasks do not count toward escrow held`() =
        runTest {
            repo = FakeClientTaskRepository(listOf(makeTask("t1", TaskStatus.ACCEPTED, bountyUsd = 200)))
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(0, state.totalEscrowHeldUsd)
        }

    @Test
    fun `totalEscrowHeldUsd is zero when only draft tasks`() =
        runTest {
            repo = FakeClientTaskRepository(listOf(makeTask("t1", TaskStatus.DRAFT, bountyUsd = 50)))
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(0, state.totalEscrowHeldUsd)
        }

    // ── hasPendingReview ──────────────────────────────────────────────────────

    @Test
    fun `hasPendingReview true when evidence submitted`() =
        runTest {
            repo = FakeClientTaskRepository(listOf(makeTask("t1", TaskStatus.EVIDENCE_SUBMITTED)))
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertTrue(state.hasPendingReview)
        }

    @Test
    fun `hasPendingReview false when no review tasks`() =
        runTest {
            repo = FakeClientTaskRepository(listOf(makeTask("t1", TaskStatus.IN_PROGRESS)))
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertFalse(state.hasPendingReview)
        }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Test
    fun `refresh calls repository again`() =
        runTest {
            repo = FakeClientTaskRepository(listOf(makeTask("t1", TaskStatus.IN_PROGRESS)))
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            vm.refresh("uid-1")
            assertEquals(2, repo.fetchCallCount)
        }

    @Test
    fun `second load resets state to Loading then Dashboard`() =
        runTest {
            val tasks = listOf(makeTask("t1", TaskStatus.IN_PROGRESS))
            repo = FakeClientTaskRepository(tasks)
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            vm.loadDashboard("uid-1")
            assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(2, repo.fetchCallCount)
        }

    // ── mixed data ────────────────────────────────────────────────────────────

    @Test
    fun `five tasks across all categories are all loaded`() =
        runTest {
            val tasks =
                listOf(
                    makeTask("t1", TaskStatus.IN_PROGRESS),
                    makeTask("t2", TaskStatus.EVIDENCE_SUBMITTED),
                    makeTask("t3", TaskStatus.ACCEPTED),
                    makeTask("t4", TaskStatus.POSTED),
                    makeTask("t5", TaskStatus.INSPECTOR_MATCHED),
                )
            repo = FakeClientTaskRepository(tasks)
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(5, state.allTasks.size)
        }

    @Test
    fun `UNDER_REVIEW is included in pending review`() =
        runTest {
            repo = FakeClientTaskRepository(listOf(makeTask("t1", TaskStatus.UNDER_REVIEW)))
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertTrue(state.pendingReview.any { it.id == "t1" })
        }

    @Test
    fun `UNDER_REVIEW counts toward escrow held`() =
        runTest {
            repo = FakeClientTaskRepository(listOf(makeTask("t1", TaskStatus.UNDER_REVIEW, bountyUsd = 75)))
            vm = ClientDashboardViewModel(repo)
            vm.loadDashboard("uid-1")
            val state = assertIs<ClientDashboardUiState.Dashboard>(vm.state.value)
            assertEquals(75, state.totalEscrowHeldUsd)
        }
}
