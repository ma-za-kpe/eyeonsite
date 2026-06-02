package com.maku.eyeonsite.domain

data class InspectorProfile(
    val uid: String,
    val fullName: String,
    val phoneNumber: String = "",
    val city: String,
    val serviceRadiusKm: Int,
    val idDocUrl: String = "",
    val selfieUrl: String = "",
    val idDocPath: String = "",
    val selfiePath: String = "",
    val onboardingStatus: InspectorOnboardingStatus = InspectorOnboardingStatus.NOT_STARTED,
    val testScore: Int = 0,
    val submittedAtMs: Long = 0L,
    val kycCheck: KycCheck = KycCheck(),
)
