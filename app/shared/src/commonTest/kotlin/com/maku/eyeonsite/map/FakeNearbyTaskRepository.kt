package com.maku.eyeonsite.map

import com.maku.eyeonsite.domain.NearbyTask
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.map.NearbyTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeNearbyTaskRepository(
    private val tasks: List<NearbyTask> = emptyList(),
) : NearbyTaskRepository {
    var fetchCallCount = 0
        private set

    override fun observeNearby(
        lat: Double,
        lng: Double,
    ): Flow<List<NearbyTask>> {
        fetchCallCount++
        return flowOf(tasks)
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
