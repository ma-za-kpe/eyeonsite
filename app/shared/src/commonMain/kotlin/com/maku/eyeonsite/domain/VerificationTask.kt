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
    val h3Res7: String = "",
    val h3Res9: String = "",
    val h3NeighborRes9: List<String> = emptyList(),
    val escrow: EscrowSnapshot = EscrowSnapshot(),
) {
    fun hasH3Index(): Boolean = h3Res7.isNotBlank() && h3Res9.isNotBlank()
}
