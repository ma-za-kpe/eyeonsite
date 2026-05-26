package com.maku.eyeonsite.data

import com.maku.eyeonsite.domain.NearbyTask
import com.maku.eyeonsite.domain.TaskType
import com.maku.eyeonsite.ui.map.NearbyTaskRepository

class MockNearbyTaskRepository : NearbyTaskRepository {
    override suspend fun fetchNearby(
        lat: Double,
        lng: Double,
    ): List<NearbyTask> = MOCK_TASKS
}

private val MOCK_TASKS =
    listOf(
        NearbyTask(
            id = "mock-001",
            type = TaskType.CONSTRUCTION,
            location = "East Legon, Accra",
            description = "Verify floor slab and column placement on floors 3–5",
            latitude = 5.6037,
            longitude = -0.1870,
            distanceKm = 0.8,
            bountyUsd = 45,
            postedMinutesAgo = 12,
        ),
        NearbyTask(
            id = "mock-002",
            type = TaskType.PROPERTY,
            location = "Airport Hills, Accra",
            description = "Confirm boundary fencing and gate installation are complete",
            latitude = 5.6057,
            longitude = -0.1765,
            distanceKm = 1.4,
            bountyUsd = 30,
            postedMinutesAgo = 35,
        ),
        NearbyTask(
            id = "mock-003",
            type = TaskType.FARM,
            location = "Dodowa, Greater Accra",
            description = "Photograph cassava crop coverage and irrigation channels",
            latitude = 5.8921,
            longitude = -0.1283,
            distanceKm = 3.2,
            bountyUsd = 55,
            postedMinutesAgo = 7,
        ),
        NearbyTask(
            id = "mock-004",
            type = TaskType.BUSINESS,
            location = "Osu, Accra",
            description = "Verify shop is open, signage visible, and stock present",
            latitude = 5.5515,
            longitude = -0.1764,
            distanceKm = 2.1,
            bountyUsd = 25,
            postedMinutesAgo = 58,
        ),
        NearbyTask(
            id = "mock-005",
            type = TaskType.CONSTRUCTION,
            location = "Tema, Greater Accra",
            description = "Inspect roofing sheets and verify plumbing rough-in",
            latitude = 5.6698,
            longitude = -0.0166,
            distanceKm = 4.7,
            bountyUsd = 60,
            postedMinutesAgo = 20,
        ),
    )
