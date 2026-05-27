package com.maku.eyeonsite.domain

enum class ChecklistItemType { YES_NO, TEXT }

data class ChecklistItem(
    val id: String,
    val question: String,
    val type: ChecklistItemType,
    val required: Boolean = true,
)
