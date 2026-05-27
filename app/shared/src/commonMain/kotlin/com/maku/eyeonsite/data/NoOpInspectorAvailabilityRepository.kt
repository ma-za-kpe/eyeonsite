package com.maku.eyeonsite.data

import com.maku.eyeonsite.ui.map.InspectorAvailabilityRepository

class NoOpInspectorAvailabilityRepository : InspectorAvailabilityRepository {
    override suspend fun setAvailability(
        inspectorUid: String,
        available: Boolean,
    ) = Unit
}
