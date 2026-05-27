package com.maku.eyeonsite.ui.evidence

import androidx.lifecycle.ViewModel
import com.maku.eyeonsite.analytics.AnalyticsService
import com.maku.eyeonsite.domain.ChecklistAnswer
import com.maku.eyeonsite.domain.ChecklistItem
import com.maku.eyeonsite.domain.ChecklistItemType
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.domain.VerificationTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ChecklistUiState {
    data object Loading : ChecklistUiState()

    data class Filling(
        val task: VerificationTask,
        val items: List<ChecklistItem>,
        val answers: Map<String, ChecklistAnswer> = emptyMap(),
        val showValidation: Boolean = false,
    ) : ChecklistUiState() {
        val canSubmit: Boolean
            get() = items.filter { it.required }.all { answers[it.id]?.isAnswered == true }

        val unansweredCount: Int
            get() = items.count { it.required && answers[it.id]?.isAnswered != true }
    }

    data class Done(
        val answers: List<ChecklistAnswer>,
    ) : ChecklistUiState()
}

class ChecklistViewModel(
    private val analytics: AnalyticsService,
) : ViewModel() {
    private val _state = MutableStateFlow<ChecklistUiState>(ChecklistUiState.Loading)
    val state: StateFlow<ChecklistUiState> = _state.asStateFlow()

    fun load(task: VerificationTask) {
        _state.value =
            ChecklistUiState.Filling(
                task = task,
                items = checklistForType(task.type),
            )
        analytics.logEvent("checklist_opened", mapOf("task_type" to task.type.name))
    }

    fun answerYesNo(
        itemId: String,
        yes: Boolean,
    ) {
        val s = _state.value as? ChecklistUiState.Filling ?: return
        _state.value =
            s.copy(
                answers = s.answers + (itemId to ChecklistAnswer(itemId = itemId, boolValue = yes)),
            )
    }

    fun answerText(
        itemId: String,
        text: String,
    ) {
        val s = _state.value as? ChecklistUiState.Filling ?: return
        _state.value =
            s.copy(
                answers = s.answers + (itemId to ChecklistAnswer(itemId = itemId, textValue = text)),
            )
    }

    fun submit() {
        val s = _state.value as? ChecklistUiState.Filling ?: return
        val withValidation = s.copy(showValidation = true)
        _state.value = withValidation
        if (!withValidation.canSubmit) return
        analytics.logEvent(
            name = "checklist_completed",
            params =
                mapOf(
                    "task_type" to s.task.type.name,
                    "answered" to s.answers.size.toString(),
                ),
        )
        _state.value = ChecklistUiState.Done(s.answers.values.toList())
    }

    fun reset() {
        _state.value = ChecklistUiState.Loading
    }
}

internal fun checklistForType(type: TaskType): List<ChecklistItem> =
    when (type) {
        TaskType.CONSTRUCTION ->
            listOf(
                ChecklistItem("q1", "Is the foundation work visibly complete?", ChecklistItemType.YES_NO),
                ChecklistItem("q2", "Are columns and beams in the correct position?", ChecklistItemType.YES_NO),
                ChecklistItem("q3", "Is there any visible structural damage or cracking?", ChecklistItemType.YES_NO),
                ChecklistItem("q4", "Is the work site accessible and active?", ChecklistItemType.YES_NO),
                ChecklistItem("notes", "Additional observations", ChecklistItemType.TEXT, required = false),
            )

        TaskType.PROPERTY ->
            listOf(
                ChecklistItem("q1", "Is the boundary fence intact and complete?", ChecklistItemType.YES_NO),
                ChecklistItem("q2", "Is the property currently occupied?", ChecklistItemType.YES_NO),
                ChecklistItem("q3", "Is there visible damage to the structure?", ChecklistItemType.YES_NO),
                ChecklistItem("q4", "Is the gate and access point functional?", ChecklistItemType.YES_NO),
                ChecklistItem("notes", "Additional observations", ChecklistItemType.TEXT, required = false),
            )

        TaskType.FARM ->
            listOf(
                ChecklistItem("q1", "Is the crop visibly growing and healthy?", ChecklistItemType.YES_NO),
                ChecklistItem("q2", "Is the irrigation system operational?", ChecklistItemType.YES_NO),
                ChecklistItem("q3", "Are there signs of pest damage or disease?", ChecklistItemType.YES_NO),
                ChecklistItem("q4", "Is the farm area as described in the task?", ChecklistItemType.YES_NO),
                ChecklistItem("notes", "Additional observations", ChecklistItemType.TEXT, required = false),
            )

        TaskType.BUSINESS ->
            listOf(
                ChecklistItem("q1", "Is the business currently open and operating?", ChecklistItemType.YES_NO),
                ChecklistItem("q2", "Is the signage and branding clearly visible?", ChecklistItemType.YES_NO),
                ChecklistItem("q3", "Is there visible stock or inventory?", ChecklistItemType.YES_NO),
                ChecklistItem("q4", "Is the location as described in the task?", ChecklistItemType.YES_NO),
                ChecklistItem("notes", "Additional observations", ChecklistItemType.TEXT, required = false),
            )
    }
