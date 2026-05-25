package com.maku.eyeonsite.domain

enum class ReviewDecision(
    val label: String,
    val description: String,
) {
    ACCEPTED("Accept Evidence", "Evidence is valid — release payment to inspector"),
    REVISION_REQUESTED("Request Revision", "Need additional photos or clarification"),
    DISPUTED("Dispute", "Evidence appears fraudulent or location is incorrect"),
}
