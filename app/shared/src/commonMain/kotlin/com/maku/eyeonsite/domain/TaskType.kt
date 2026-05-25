package com.maku.eyeonsite.domain

enum class TaskType(
    val label: String,
    val shortDescription: String,
) {
    CONSTRUCTION("Construction", "Build progress & materials"),
    PROPERTY("Property", "Condition, occupancy & boundaries"),
    FARM("Farm", "Crop status, acreage & livestock"),
    BUSINESS("Business", "Location, operations & collateral"),
}
