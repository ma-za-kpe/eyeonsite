package com.maku.eyeonsite.domain

data class ChecklistAnswer(
    val itemId: String,
    val boolValue: Boolean? = null,
    val textValue: String = "",
) {
    val isAnswered: Boolean
        get() = boolValue != null || textValue.isNotBlank()
}
