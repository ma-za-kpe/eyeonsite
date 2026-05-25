package com.maku.eyeonsite.domain

data class NearbyTask(
    val id: String,
    val type: TaskType,
    val location: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val bountyUsd: Int,
    val postedMinutesAgo: Int,
)
