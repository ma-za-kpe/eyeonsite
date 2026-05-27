package com.maku.eyeonsite.domain

enum class DisputeReason(
    val label: String,
) {
    WRONG_LOCATION("Wrong location"),
    INSUFFICIENT_PHOTOS("Insufficient photos"),
    WRONG_PROPERTY("Wrong property"),
    POOR_QUALITY("Poor quality evidence"),
    FRAUDULENT("Potentially fraudulent"),
    OTHER("Other"),
}
