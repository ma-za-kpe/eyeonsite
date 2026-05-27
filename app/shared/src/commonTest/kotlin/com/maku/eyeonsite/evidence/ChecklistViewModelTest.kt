package com.maku.eyeonsite.evidence

import com.maku.eyeonsite.analytics.NoOpAnalyticsService
import com.maku.eyeonsite.domain.ChecklistItemType
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.VerificationTask
import com.maku.eyeonsite.ui.evidence.ChecklistUiState
import com.maku.eyeonsite.ui.evidence.ChecklistViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChecklistViewModelTest {
    private fun makeVm() = ChecklistViewModel(NoOpAnalyticsService)

    private fun makeTask(type: TaskType = TaskType.CONSTRUCTION) =
        VerificationTask(
            id = "task-1",
            type = type,
            location = "East Legon, Accra",
            description = "Verify floor slab",
        )

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        assertIs<ChecklistUiState.Loading>(makeVm().state.value)
    }

    // ── load ──────────────────────────────────────────────────────────────────

    @Test
    fun `load transitions to Filling`() {
        val vm = makeVm()
        vm.load(makeTask())
        assertIs<ChecklistUiState.Filling>(vm.state.value)
    }

    @Test
    fun `load populates items for CONSTRUCTION`() {
        val vm = makeVm()
        vm.load(makeTask(TaskType.CONSTRUCTION))
        val state = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertTrue(state.items.isNotEmpty())
        assertTrue(state.items.any { it.type == ChecklistItemType.YES_NO })
    }

    @Test
    fun `load produces items for each task type`() {
        TaskType.entries.forEach { type ->
            val vm = makeVm()
            vm.load(makeTask(type))
            val state = assertIs<ChecklistUiState.Filling>(vm.state.value)
            assertTrue(state.items.isNotEmpty(), "Expected items for $type")
        }
    }

    @Test
    fun `load starts with empty answers`() {
        val vm = makeVm()
        vm.load(makeTask())
        val state = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertTrue(state.answers.isEmpty())
    }

    // ── answerYesNo ───────────────────────────────────────────────────────────

    @Test
    fun `answerYesNo stores true answer`() {
        val vm = makeVm()
        vm.load(makeTask())
        val filling = assertIs<ChecklistUiState.Filling>(vm.state.value)
        val itemId = filling.items.first { it.type == ChecklistItemType.YES_NO }.id
        vm.answerYesNo(itemId, true)
        val updated = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertEquals(true, updated.answers[itemId]?.boolValue)
    }

    @Test
    fun `answerYesNo stores false answer`() {
        val vm = makeVm()
        vm.load(makeTask())
        val filling = assertIs<ChecklistUiState.Filling>(vm.state.value)
        val itemId = filling.items.first { it.type == ChecklistItemType.YES_NO }.id
        vm.answerYesNo(itemId, false)
        val updated = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertEquals(false, updated.answers[itemId]?.boolValue)
    }

    @Test
    fun `answerYesNo overwrites previous answer`() {
        val vm = makeVm()
        vm.load(makeTask())
        val filling = assertIs<ChecklistUiState.Filling>(vm.state.value)
        val itemId = filling.items.first { it.type == ChecklistItemType.YES_NO }.id
        vm.answerYesNo(itemId, true)
        vm.answerYesNo(itemId, false)
        val updated = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertEquals(false, updated.answers[itemId]?.boolValue)
    }

    @Test
    fun `answerYesNo on Loading is no-op`() {
        val vm = makeVm()
        vm.answerYesNo("q1", true)
        assertIs<ChecklistUiState.Loading>(vm.state.value)
    }

    // ── answerText ────────────────────────────────────────────────────────────

    @Test
    fun `answerText stores text value`() {
        val vm = makeVm()
        vm.load(makeTask())
        vm.answerText("notes", "The columns look slightly off-center")
        val updated = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertEquals("The columns look slightly off-center", updated.answers["notes"]?.textValue)
    }

    // ── canSubmit ─────────────────────────────────────────────────────────────

    @Test
    fun `canSubmit false when required items unanswered`() {
        val vm = makeVm()
        vm.load(makeTask())
        val state = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertFalse(state.canSubmit)
    }

    @Test
    fun `canSubmit true when all required items answered`() {
        val vm = makeVm()
        vm.load(makeTask())
        val filling = assertIs<ChecklistUiState.Filling>(vm.state.value)
        filling.items
            .filter { it.required && it.type == ChecklistItemType.YES_NO }
            .forEach { vm.answerYesNo(it.id, true) }
        val updated = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertTrue(updated.canSubmit)
    }

    @Test
    fun `optional text item does not block canSubmit`() {
        val vm = makeVm()
        vm.load(makeTask())
        val filling = assertIs<ChecklistUiState.Filling>(vm.state.value)
        filling.items.filter { it.required }.forEach { item ->
            when (item.type) {
                ChecklistItemType.YES_NO -> vm.answerYesNo(item.id, true)
                ChecklistItemType.TEXT -> vm.answerText(item.id, "some text")
            }
        }
        val updated = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertTrue(updated.canSubmit)
    }

    // ── submit ────────────────────────────────────────────────────────────────

    @Test
    fun `submit with unanswered required items stays Filling with showValidation true`() {
        val vm = makeVm()
        vm.load(makeTask())
        vm.submit()
        val state = assertIs<ChecklistUiState.Filling>(vm.state.value)
        assertTrue(state.showValidation)
    }

    @Test
    fun `submit with all required answered transitions to Done`() {
        val vm = makeVm()
        vm.load(makeTask())
        val filling = assertIs<ChecklistUiState.Filling>(vm.state.value)
        filling.items
            .filter { it.required && it.type == ChecklistItemType.YES_NO }
            .forEach { vm.answerYesNo(it.id, true) }
        vm.submit()
        assertIs<ChecklistUiState.Done>(vm.state.value)
    }

    @Test
    fun `Done state contains submitted answers`() {
        val vm = makeVm()
        vm.load(makeTask())
        val filling = assertIs<ChecklistUiState.Filling>(vm.state.value)
        filling.items
            .filter { it.required && it.type == ChecklistItemType.YES_NO }
            .forEach { vm.answerYesNo(it.id, true) }
        vm.answerText("notes", "All clear")
        vm.submit()
        val done = assertIs<ChecklistUiState.Done>(vm.state.value)
        assertNotNull(done.answers.find { it.textValue == "All clear" })
    }

    @Test
    fun `unansweredCount reflects remaining required items`() {
        val vm = makeVm()
        vm.load(makeTask())
        val filling = assertIs<ChecklistUiState.Filling>(vm.state.value)
        val requiredCount = filling.items.count { it.required }
        assertEquals(requiredCount, filling.unansweredCount)
    }

    // ── reset ─────────────────────────────────────────────────────────────────

    @Test
    fun `reset returns to Loading`() {
        val vm = makeVm()
        vm.load(makeTask())
        vm.reset()
        assertIs<ChecklistUiState.Loading>(vm.state.value)
    }

    @Test
    fun `reset after Done clears state`() {
        val vm = makeVm()
        vm.load(makeTask())
        val filling = assertIs<ChecklistUiState.Filling>(vm.state.value)
        filling.items
            .filter { it.required && it.type == ChecklistItemType.YES_NO }
            .forEach { vm.answerYesNo(it.id, true) }
        vm.submit()
        assertIs<ChecklistUiState.Done>(vm.state.value)
        vm.reset()
        assertIs<ChecklistUiState.Loading>(vm.state.value)
    }

    // ── ChecklistAnswer.isAnswered ────────────────────────────────────────────

    @Test
    fun `ChecklistAnswer isAnswered true when boolValue set`() {
        val answer =
            com.maku.eyeonsite.domain
                .ChecklistAnswer(itemId = "q1", boolValue = true)
        assertTrue(answer.isAnswered)
    }

    @Test
    fun `ChecklistAnswer isAnswered true when textValue non-blank`() {
        val answer =
            com.maku.eyeonsite.domain
                .ChecklistAnswer(itemId = "notes", textValue = "some text")
        assertTrue(answer.isAnswered)
    }

    @Test
    fun `ChecklistAnswer isAnswered false when no value`() {
        val answer =
            com.maku.eyeonsite.domain
                .ChecklistAnswer(itemId = "q1")
        assertFalse(answer.isAnswered)
    }

    @Test
    fun `ChecklistAnswer isAnswered false when textValue blank`() {
        val answer =
            com.maku.eyeonsite.domain
                .ChecklistAnswer(itemId = "notes", textValue = "   ")
        assertFalse(answer.isAnswered)
    }
}
