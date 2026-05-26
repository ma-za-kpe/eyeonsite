package com.maku.eyeonsite.map

import com.maku.eyeonsite.domain.NearbyTask
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.map.NearbyTaskRepository

class FakeNearbyTaskRepository(
    private val tasks: List<NearbyTask> = emptyList(),
    private val delay: Boolean = false,
) : NearbyTaskRepository {
    var fetchCallCount = 0
        private set

    override suspend fun fetchNearby(
        lat: Double,
        lng: Double,
    ): List<NearbyTask> {
        fetchCallCount++
        return tasks
    }
}

fun makeNearbyTask(
    id: String = "task-1",
    type: TaskType = TaskType.CONSTRUCTION,
    distanceKm: Double = 1.0,
    bountyUsd: Int = 30,
) = NearbyTask(
    id = id,
    type = type,
    location = "East Legon, Accra",
    description = "Verify floor progress",
    latitude = 5.5502,
    longitude = -0.2174,
    distanceKm = distanceKm,
    bountyUsd = bountyUsd,
    postedMinutesAgo = 5,
)
