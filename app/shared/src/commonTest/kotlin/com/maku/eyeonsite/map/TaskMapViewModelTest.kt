package com.maku.eyeonsite.map

import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.map.TaskMapUiState
import com.maku.eyeonsite.ui.map.TaskMapViewModel
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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TaskMapViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeNearbyTaskRepository
    private lateinit var vm: TaskMapViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeNearbyTaskRepository()
        vm = TaskMapViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── loadNearbyTasks ───────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        assertIs<TaskMapUiState.Loading>(vm.state.value)
    }

    @Test
    fun `empty repository results in Empty state`() =
        runTest {
            vm.loadNearbyTasks(5.55, -0.21)
            assertIs<TaskMapUiState.Empty>(vm.state.value)
        }

    @Test
    fun `single task produces HasTasks with one task`() =
        runTest {
            repo = FakeNearbyTaskRepository(listOf(makeNearbyTask()))
            vm = TaskMapViewModel(repo)

            vm.loadNearbyTasks(5.55, -0.21)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertEquals(1, state.tasks.size)
        }

    @Test
    fun `three tasks all visible in HasTasks`() =
        runTest {
            val tasks = (1..3).map { makeNearbyTask("t$it") }
            repo = FakeNearbyTaskRepository(tasks)
            vm = TaskMapViewModel(repo)

            vm.loadNearbyTasks(5.55, -0.21)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertEquals(3, state.visibleTasks.size)
        }

    @Test
    fun `ten tasks not yet clustered`() =
        runTest {
            val tasks = (1..10).map { makeNearbyTask("t$it") }
            repo = FakeNearbyTaskRepository(tasks)
            vm = TaskMapViewModel(repo)

            vm.loadNearbyTasks(5.55, -0.21)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertEquals(10, state.tasks.size)
            assertTrue(!state.isClustered)
        }

    @Test
    fun `eleven tasks triggers isClustered`() =
        runTest {
            val tasks = (1..11).map { makeNearbyTask("t$it") }
            repo = FakeNearbyTaskRepository(tasks)
            vm = TaskMapViewModel(repo)

            vm.loadNearbyTasks(5.55, -0.21)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertTrue(state.isClustered)
        }

    @Test
    fun `fifty tasks all loaded and clustered`() =
        runTest {
            val tasks = (1..50).map { makeNearbyTask("t$it") }
            repo = FakeNearbyTaskRepository(tasks)
            vm = TaskMapViewModel(repo)

            vm.loadNearbyTasks(5.55, -0.21)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertEquals(50, state.tasks.size)
            assertTrue(state.isClustered)
        }

    @Test
    fun `loadNearbyTasks resets state to Loading then HasTasks on second call`() =
        runTest {
            val tasks = listOf(makeNearbyTask())
            repo = FakeNearbyTaskRepository(tasks)
            vm = TaskMapViewModel(repo)

            vm.loadNearbyTasks(5.55, -0.21)
            assertIs<TaskMapUiState.HasTasks>(vm.state.value)

            vm.loadNearbyTasks(5.55, -0.21)
            assertEquals(2, repo.fetchCallCount)
        }

    // ── selectTask ────────────────────────────────────────────────────────────

    @Test
    fun `selectTask updates selectedTaskId`() =
        runTest {
            repo = FakeNearbyTaskRepository(listOf(makeNearbyTask("t1")))
            vm = TaskMapViewModel(repo)
            vm.loadNearbyTasks(5.55, -0.21)

            vm.selectTask("t1")

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertEquals("t1", state.selectedTaskId)
        }

    @Test
    fun `selectTask null clears selection`() =
        runTest {
            repo = FakeNearbyTaskRepository(listOf(makeNearbyTask("t1")))
            vm = TaskMapViewModel(repo)
            vm.loadNearbyTasks(5.55, -0.21)
            vm.selectTask("t1")

            vm.selectTask(null)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertNull(state.selectedTaskId)
        }

    @Test
    fun `selectTask on Loading state is no-op`() =
        runTest {
            vm.selectTask("t1")
            assertIs<TaskMapUiState.Loading>(vm.state.value)
        }

    @Test
    fun `selectedTask returns the correct NearbyTask`() =
        runTest {
            val task = makeNearbyTask("t99", bountyUsd = 75)
            repo = FakeNearbyTaskRepository(listOf(task))
            vm = TaskMapViewModel(repo)
            vm.loadNearbyTasks(5.55, -0.21)

            vm.selectTask("t99")

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertEquals(75, state.selectedTask?.bountyUsd)
        }

    // ── setFilter ─────────────────────────────────────────────────────────────

    @Test
    fun `setFilter hides tasks of other types`() =
        runTest {
            val tasks =
                listOf(
                    makeNearbyTask("c1", type = TaskType.CONSTRUCTION),
                    makeNearbyTask("p1", type = TaskType.PROPERTY),
                    makeNearbyTask("f1", type = TaskType.FARM),
                )
            repo = FakeNearbyTaskRepository(tasks)
            vm = TaskMapViewModel(repo)
            vm.loadNearbyTasks(5.55, -0.21)

            vm.setFilter(TaskType.CONSTRUCTION)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertEquals(1, state.visibleTasks.size)
            assertTrue(state.visibleTasks.all { it.type == TaskType.CONSTRUCTION })
        }

    @Test
    fun `setFilter null shows all tasks`() =
        runTest {
            val tasks = (1..4).map { makeNearbyTask("t$it", type = TaskType.entries[it % 4]) }
            repo = FakeNearbyTaskRepository(tasks)
            vm = TaskMapViewModel(repo)
            vm.loadNearbyTasks(5.55, -0.21)

            vm.setFilter(TaskType.FARM)
            vm.setFilter(null)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertEquals(4, state.visibleTasks.size)
        }

    @Test
    fun `setFilter clears selectedTaskId`() =
        runTest {
            val tasks =
                listOf(
                    makeNearbyTask("c1", type = TaskType.CONSTRUCTION),
                    makeNearbyTask("p1", type = TaskType.PROPERTY),
                )
            repo = FakeNearbyTaskRepository(tasks)
            vm = TaskMapViewModel(repo)
            vm.loadNearbyTasks(5.55, -0.21)
            vm.selectTask("c1")

            vm.setFilter(TaskType.PROPERTY)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertNull(state.selectedTaskId)
        }

    @Test
    fun `setFilter with no matching tasks results in empty visibleTasks`() =
        runTest {
            repo = FakeNearbyTaskRepository(listOf(makeNearbyTask("c1", type = TaskType.CONSTRUCTION)))
            vm = TaskMapViewModel(repo)
            vm.loadNearbyTasks(5.55, -0.21)

            vm.setFilter(TaskType.FARM)

            val state = assertIs<TaskMapUiState.HasTasks>(vm.state.value)
            assertTrue(state.visibleTasks.isEmpty())
        }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Test
    fun `refresh calls repository again`() =
        runTest {
            repo = FakeNearbyTaskRepository(listOf(makeNearbyTask()))
            vm = TaskMapViewModel(repo)
            vm.loadNearbyTasks(5.55, -0.21)

            vm.refresh(5.55, -0.21)

            assertEquals(2, repo.fetchCallCount)
        }
}
