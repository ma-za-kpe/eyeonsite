package com.maku.eyeonsite.ui.auth

private const val MIN_SUBSCRIBER_DIGITS = 7
private const val MAX_SUBSCRIBER_DIGITS = 12

object PhoneInputValidator {
    fun isValid(localNumber: String): Boolean {
        val subscriber = localNumber.filter { it.isDigit() }.removePrefix("0")
        return subscriber.length in MIN_SUBSCRIBER_DIGITS..MAX_SUBSCRIBER_DIGITS
    }

    fun assembleE164(
        dialCode: String,
        localNumber: String,
    ): String {
        val subscriber = localNumber.filter { it.isDigit() }.removePrefix("0")
        return "+$dialCode$subscriber"
    }
}
