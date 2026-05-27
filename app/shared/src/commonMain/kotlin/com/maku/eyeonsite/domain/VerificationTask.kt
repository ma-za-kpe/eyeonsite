package com.maku.eyeonsite.domain

data class VerificationTask(
    val id: String = "",
    val clientUid: String = "",
    val type: TaskType,
    val location: String,
    val locationLat: Double = 0.0,
    val locationLng: Double = 0.0,
    val description: String,
    val bountyUsd: Int = 30,
    val status: TaskStatus = TaskStatus.DRAFT,
)
