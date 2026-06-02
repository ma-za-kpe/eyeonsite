package com.maku.eyeonsite.domain

enum class MoneyProvider {
    NONE,
    STRIPE,
    FLUTTERWAVE,
    MOCK,
}

enum class EscrowStatus {
    UNFUNDED,
    HELD,
    RELEASE_PENDING,
    RELEASED,
    REFUND_PENDING,
    REFUNDED,
    DISPUTED,
    FROZEN,
    FAILED,
}

data class EscrowSnapshot(
    val status: EscrowStatus = EscrowStatus.UNFUNDED,
    val provider: MoneyProvider = MoneyProvider.NONE,
    val amountUsdCents: Long = 0L,
    val inspectorPayoutUsdCents: Long = 0L,
    val platformFeeUsdCents: Long = 0L,
    val escrowFeeUsdCents: Long = 0L,
    val providerPaymentId: String = "",
    val providerTransferId: String = "",
) {
    fun hasBalancedFeeBreakdown(): Boolean {
        if (amountUsdCents == 0L) return true
        val totalFees = inspectorPayoutUsdCents + platformFeeUsdCents + escrowFeeUsdCents
        return totalFees == amountUsdCents
    }
}

enum class EscrowLedgerEntryType {
    ESCROW_HOLD,
    ESCROW_RELEASE,
    ESCROW_REFUND,
    PAYOUT,
    FEE,
}

enum class EscrowLedgerEntryStatus {
    PENDING,
    POSTED,
    FAILED,
    REVERSED,
}

data class EscrowLedgerEntry(
    val id: String = "",
    val taskId: String,
    val type: EscrowLedgerEntryType,
    val status: EscrowLedgerEntryStatus = EscrowLedgerEntryStatus.PENDING,
    val provider: MoneyProvider = MoneyProvider.NONE,
    val amountUsdCents: Long,
    val createdAtMs: Long = 0L,
    val providerReference: String = "",
    val idempotencyKey: String = "",
)
