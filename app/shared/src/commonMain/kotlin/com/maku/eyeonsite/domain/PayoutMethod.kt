package com.maku.eyeonsite.domain

enum class PayoutMethod(
    val label: String,
) {
    MTN_MOMO("MTN Mobile Money"),
    MPESA("M-Pesa"),
    ORANGE_MONEY("Orange Money"),
    BANK_TRANSFER("Bank Transfer"),
}
