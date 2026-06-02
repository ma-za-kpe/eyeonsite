package com.maku.eyeonsite.domain

enum class KycProvider {
    NONE,
    SMILE_ID,
    MOCK,
}

enum class KycStatus {
    NOT_STARTED,
    SUBMITTED,
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    RETRY_REQUIRED,
    EXPIRED,
}

data class KycCheck(
    val provider: KycProvider = KycProvider.NONE,
    val status: KycStatus = KycStatus.NOT_STARTED,
    val providerJobId: String = "",
    val submittedAtMs: Long = 0L,
    val decidedAtMs: Long = 0L,
    val resultCode: String = "",
    val failureReason: String = "",
) {
    fun requiresProviderDecision(): Boolean = status == KycStatus.SUBMITTED || status == KycStatus.PENDING_REVIEW
}
