package com.maku.eyeonsite.domain

data class VerificationTask(
    val id: String = "",
    val clientUid: String = "",
    val type: TaskType,
    val location: String,
    val description: String,
    val status: TaskStatus = TaskStatus.DRAFT,
)
