package com.maku.eyeonsite.domain

data class ClientTaskSummary(
    val id: String,
    val type: TaskType,
    val location: String,
    val description: String,
    val status: TaskStatus,
    val bountyUsd: Int,
    val createdAtMs: Long,
    val inspectorName: String? = null,
    val escrowStatus: EscrowStatus = EscrowStatus.UNFUNDED,
)
