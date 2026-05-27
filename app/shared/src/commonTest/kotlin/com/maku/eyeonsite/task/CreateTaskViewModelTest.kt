package com.maku.eyeonsite.task

import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.data.NoOpTaskRepository
import com.maku.eyeonsite.data.TaskRepository
import com.maku.eyeonsite.domain.TaskStatus
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.platform.NoOpGeocoderService
import com.maku.eyeonsite.ui.task.CreateTaskUiState
import com.maku.eyeonsite.ui.task.CreateTaskViewModel
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTaskViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeVm(repository: TaskRepository = NoOpTaskRepository()) =
        CreateTaskViewModel(NoOpAnalyticsService, NoOpGeocoderService, repository)

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Form`() {
        assertIs<CreateTaskUiState.Form>(makeVm().state.value)
    }

    @Test
    fun `initial state has null taskType`() {
        val state = assertIs<CreateTaskUiState.Form>(makeVm().state.value)
        assertNull(state.taskType)
    }

    @Test
    fun `initial state has empty location and description`() {
        val state = assertIs<CreateTaskUiState.Form>(makeVm().state.value)
        assertTrue(state.location.isEmpty())
        assertTrue(state.description.isEmpty())
    }

    @Test
    fun `initial bounty is 30`() {
        val state = assertIs<CreateTaskUiState.Form>(makeVm().state.value)
        assertEquals(30, state.bountyUsd)
    }

    // ── field setters ─────────────────────────────────────────────────────────

    @Test
    fun `setTaskType updates taskType`() {
        val vm = makeVm()
        vm.setTaskType(TaskType.CONSTRUCTION)
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertEquals(TaskType.CONSTRUCTION, state.taskType)
    }

    @Test
    fun `setLocation updates location`() {
        val vm = makeVm()
        vm.setLocation("East Legon, Accra")
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertEquals("East Legon, Accra", state.location)
    }

    @Test
    fun `setDescription updates description`() {
        val vm = makeVm()
        vm.setDescription("Verify floor slab")
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertEquals("Verify floor slab", state.description)
    }

    @Test
    fun `setBounty updates bountyUsd`() {
        val vm = makeVm()
        vm.setBounty(75)
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertEquals(75, state.bountyUsd)
    }

    @Test
    fun `setBounty clamps below minimum to BOUNTY_MIN`() {
        val vm = makeVm()
        vm.setBounty(0)
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertEquals(CreateTaskViewModel.BOUNTY_MIN, state.bountyUsd)
    }

    @Test
    fun `setBounty clamps above maximum to BOUNTY_MAX`() {
        val vm = makeVm()
        vm.setBounty(999)
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertEquals(CreateTaskViewModel.BOUNTY_MAX, state.bountyUsd)
    }

    @Test
    fun `setters are no-op when state is FindingInspector`() {
        val vm = makeVm()
        vm.setTaskType(TaskType.FARM)
        vm.setLocation("Dodowa")
        vm.setDescription("Check crops")
        vm.post()
        assertIs<CreateTaskUiState.FindingInspector>(vm.state.value)

        vm.setTaskType(TaskType.CONSTRUCTION)
        vm.setLocation("New location")
        vm.setDescription("New description")
        vm.setBounty(100)

        assertIs<CreateTaskUiState.FindingInspector>(vm.state.value)
    }

    // ── canPost ───────────────────────────────────────────────────────────────

    @Test
    fun `canPost false when taskType is null`() {
        val state =
            CreateTaskUiState.Form(
                taskType = null,
                location = "Accra",
                description = "Verify",
            )
        assertFalse(state.canPost)
    }

    @Test
    fun `canPost false when location is blank`() {
        val state =
            CreateTaskUiState.Form(
                taskType = TaskType.PROPERTY,
                location = "  ",
                description = "Verify",
            )
        assertFalse(state.canPost)
    }

    @Test
    fun `canPost false when description is blank`() {
        val state =
            CreateTaskUiState.Form(
                taskType = TaskType.PROPERTY,
                location = "Accra",
                description = "",
            )
        assertFalse(state.canPost)
    }

    @Test
    fun `canPost true when all fields valid`() {
        val state =
            CreateTaskUiState.Form(
                taskType = TaskType.CONSTRUCTION,
                location = "East Legon",
                description = "Verify floor slab",
            )
        assertTrue(state.canPost)
    }

    // ── validation errors ─────────────────────────────────────────────────────

    @Test
    fun `typeError true when submitted and taskType is null`() {
        val state = CreateTaskUiState.Form(taskType = null, submitted = true)
        assertTrue(state.typeError)
    }

    @Test
    fun `typeError false when not submitted`() {
        val state = CreateTaskUiState.Form(taskType = null, submitted = false)
        assertFalse(state.typeError)
    }

    @Test
    fun `locationError true when submitted and location blank`() {
        val state = CreateTaskUiState.Form(location = "", submitted = true)
        assertTrue(state.locationError)
    }

    @Test
    fun `descriptionError true when submitted and description blank`() {
        val state = CreateTaskUiState.Form(description = "", submitted = true)
        assertTrue(state.descriptionError)
    }

    // ── post ──────────────────────────────────────────────────────────────────

    @Test
    fun `post with missing taskType stays in Form with submitted true`() {
        val vm = makeVm()
        vm.setLocation("Accra")
        vm.setDescription("Verify floor")
        vm.post()
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertTrue(state.submitted)
        assertTrue(state.typeError)
    }

    @Test
    fun `post with blank location stays in Form`() {
        val vm = makeVm()
        vm.setTaskType(TaskType.CONSTRUCTION)
        vm.setDescription("Verify floor")
        vm.post()
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertTrue(state.locationError)
    }

    @Test
    fun `post with blank description stays in Form`() {
        val vm = makeVm()
        vm.setTaskType(TaskType.PROPERTY)
        vm.setLocation("Accra")
        vm.post()
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertTrue(state.descriptionError)
    }

    @Test
    fun `post with all valid fields transitions to FindingInspector`() {
        val vm = makeVm()
        vm.setTaskType(TaskType.FARM)
        vm.setLocation("Dodowa, Greater Accra")
        vm.setDescription("Check cassava crop coverage")
        vm.post()
        assertIs<CreateTaskUiState.FindingInspector>(vm.state.value)
    }

    @Test
    fun `post produces task with correct type and location`() {
        val vm = makeVm()
        vm.setTaskType(TaskType.BUSINESS)
        vm.setLocation("  Osu, Accra  ")
        vm.setDescription("Verify shop is open")
        vm.setBounty(40)
        vm.post()
        val state = assertIs<CreateTaskUiState.FindingInspector>(vm.state.value)
        assertEquals(TaskType.BUSINESS, state.task.type)
        assertEquals("Osu, Accra", state.task.location)
        assertEquals("Verify shop is open", state.task.description)
        assertEquals(40, state.task.bountyUsd)
    }

    @Test
    fun `post sets task status to POSTED`() {
        val vm = makeVm()
        vm.setTaskType(TaskType.CONSTRUCTION)
        vm.setLocation("Tema")
        vm.setDescription("Inspect roofing")
        vm.post()
        val state = assertIs<CreateTaskUiState.FindingInspector>(vm.state.value)
        assertEquals(TaskStatus.POSTED, state.task.status)
    }

    @Test
    fun `post trims whitespace from location and description`() {
        val vm = makeVm()
        vm.setTaskType(TaskType.PROPERTY)
        vm.setLocation("  Airport Hills  ")
        vm.setDescription("  Check boundary fence  ")
        vm.post()
        val state = assertIs<CreateTaskUiState.FindingInspector>(vm.state.value)
        assertEquals("Airport Hills", state.task.location)
        assertEquals("Check boundary fence", state.task.description)
    }

    // ── Firestore status listener ─────────────────────────────────────────────

    @Test
    fun `INSPECTOR_MATCHED status transitions to Matched`() =
        runTest {
            val fakeRepo = FakeTaskRepository()
            val vm = makeVm(fakeRepo)
            vm.setTaskType(TaskType.FARM)
            vm.setLocation("Dodowa")
            vm.setDescription("Check crops")
            vm.post()
            assertIs<CreateTaskUiState.FindingInspector>(vm.state.value)

            fakeRepo.emitStatus(TaskStatus.INSPECTOR_MATCHED)

            val state = assertIs<CreateTaskUiState.Matched>(vm.state.value)
            assertEquals(TaskType.FARM, state.task.type)
        }

    @Test
    fun `EXPIRED status transitions to Error with message`() =
        runTest {
            val fakeRepo = FakeTaskRepository()
            val vm = makeVm(fakeRepo)
            vm.setTaskType(TaskType.CONSTRUCTION)
            vm.setLocation("East Legon")
            vm.setDescription("Verify slab")
            vm.post()

            fakeRepo.emitStatus(TaskStatus.EXPIRED)

            val state = assertIs<CreateTaskUiState.Error>(vm.state.value)
            assertTrue(state.message.isNotBlank())
        }

    @Test
    fun `other statuses do not leave FindingInspector`() =
        runTest {
            val fakeRepo = FakeTaskRepository()
            val vm = makeVm(fakeRepo)
            vm.setTaskType(TaskType.PROPERTY)
            vm.setLocation("Osu")
            vm.setDescription("Inspect property")
            vm.post()

            fakeRepo.emitStatus(TaskStatus.POSTED)

            assertIs<CreateTaskUiState.FindingInspector>(vm.state.value)
        }

    // ── reset ─────────────────────────────────────────────────────────────────

    @Test
    fun `reset from FindingInspector returns to fresh Form`() {
        val vm = makeVm()
        vm.setTaskType(TaskType.FARM)
        vm.setLocation("Dodowa")
        vm.setDescription("Check crops")
        vm.post()
        assertIs<CreateTaskUiState.FindingInspector>(vm.state.value)
        vm.reset()
        val state = assertIs<CreateTaskUiState.Form>(vm.state.value)
        assertNull(state.taskType)
        assertTrue(state.location.isEmpty())
        assertFalse(state.submitted)
    }
}
