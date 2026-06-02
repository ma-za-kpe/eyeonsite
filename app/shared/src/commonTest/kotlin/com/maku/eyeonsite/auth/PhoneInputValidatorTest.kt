package com.maku.eyeonsite.auth

import com.maku.eyeonsite.ui.auth.PhoneInputValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneInputValidatorTest {
    // ── assembleE164 ────────────────────────────────────────────────────────────

    @Test
    fun `assembleE164 strips leading zero from Ghanaian number`() {
        assertEquals("+233201234567", PhoneInputValidator.assembleE164("233", "0201234567"))
    }

    @Test
    fun `assembleE164 leaves number unchanged when no leading zero`() {
        assertEquals("+12025551234", PhoneInputValidator.assembleE164("1", "2025551234"))
    }

    @Test
    fun `assembleE164 strips spaces from formatted input`() {
        assertEquals("+233201234567", PhoneInputValidator.assembleE164("233", "020 123 4567"))
    }

    @Test
    fun `assembleE164 strips hyphens from formatted input`() {
        assertEquals("+233201234567", PhoneInputValidator.assembleE164("233", "020-123-4567"))
    }

    @Test
    fun `assembleE164 strips only first leading zero`() {
        // removePrefix("0") removes exactly one leading zero
        assertEquals("+2330501234567", PhoneInputValidator.assembleE164("233", "00501234567"))
    }

    @Test
    fun `assembleE164 works for UK number`() {
        assertEquals("+447911123456", PhoneInputValidator.assembleE164("44", "07911123456"))
    }

    @Test
    fun `assembleE164 works for Nigerian number`() {
        assertEquals("+2348012345678", PhoneInputValidator.assembleE164("234", "08012345678"))
    }

    // ── isValid ─────────────────────────────────────────────────────────────────

    @Test
    fun `isValid returns true for 9-digit Ghanaian subscriber number`() {
        assertTrue(PhoneInputValidator.isValid("0201234567")) // 10 digits → 9 after strip
    }

    @Test
    fun `isValid returns true for 10-digit US number without leading zero`() {
        assertTrue(PhoneInputValidator.isValid("2025551234"))
    }

    @Test
    fun `isValid returns true for number with spaces`() {
        assertTrue(PhoneInputValidator.isValid("020 123 4567"))
    }

    @Test
    fun `isValid returns true for number with hyphens`() {
        assertTrue(PhoneInputValidator.isValid("020-123-4567"))
    }

    @Test
    fun `isValid returns false for empty input`() {
        assertFalse(PhoneInputValidator.isValid(""))
    }

    @Test
    fun `isValid returns false for too short number`() {
        assertFalse(PhoneInputValidator.isValid("123456")) // 6 digits, below minimum 7
    }

    @Test
    fun `isValid returns false for too long number`() {
        assertFalse(PhoneInputValidator.isValid("1234567890123")) // 13 digits, above maximum 12
    }

    @Test
    fun `isValid returns false for whitespace only`() {
        assertFalse(PhoneInputValidator.isValid("   "))
    }

    @Test
    fun `isValid returns true for 7-digit minimum subscriber number`() {
        assertTrue(PhoneInputValidator.isValid("1234567"))
    }

    @Test
    fun `isValid returns true for 12-digit maximum subscriber number`() {
        assertTrue(PhoneInputValidator.isValid("123456789012"))
    }

    @Test
    fun `isValid accounts for leading zero when checking length`() {
        // "01234567" = 8 digits but leading zero stripped → 7 subscriber digits = valid
        assertTrue(PhoneInputValidator.isValid("01234567"))
        // "0123456" = 7 digits, stripped → 6 subscriber digits = invalid
        assertFalse(PhoneInputValidator.isValid("0123456"))
    }
}
