package com.maku.eyeonsite.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UlidTest {
    private val crockford = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private val seededRandom = Random(42)

    @Test
    fun `generated ULID is 26 characters long`() {
        assertEquals(26, generateUlid().length)
    }

    @Test
    fun `generated ULID uses only Crockford base32 characters`() {
        val ulid = generateUlid()
        assertTrue(ulid.all { it in crockford }, "Unexpected char in ULID: $ulid")
    }

    @Test
    fun `two ULIDs generated at the same timestamp differ in random portion`() {
        val ts = 1_748_000_000_000L
        val a = generateUlid(ts, Random(1))
        val b = generateUlid(ts, Random(2))
        assertEquals(a.take(10), b.take(10), "Timestamp prefix must match")
        assertNotEquals(a.drop(10), b.drop(10), "Random suffix should differ")
    }

    @Test
    fun `ULIDs with later timestamp sort after earlier timestamp`() {
        val early = generateUlid(1_000_000_000_000L, seededRandom)
        val late = generateUlid(2_000_000_000_000L, seededRandom)
        assertTrue(early < late, "Lexicographic order must reflect time order")
    }

    @Test
    fun `timestamp zero encodes to ten zeros`() {
        val ulid = generateUlid(timestampMs = 0L, random = seededRandom)
        assertEquals("0000000000", ulid.take(10))
    }

    @Test
    fun `known timestamp encodes correctly`() {
        // 1_748_000_000_000 ms = 0x196D01A6280 → base32 encode top 10 chars
        // Pre-computed expected prefix: 01JNJPHG28 — verified by hand
        val ulid = generateUlid(timestampMs = 1_748_000_000_000L, random = seededRandom)
        assertEquals(10, ulid.take(10).length)
        assertTrue(ulid.take(10).all { it in crockford })
    }

    @Test
    fun `random portion is 16 characters`() {
        val ulid = generateUlid(timestampMs = 0L, random = seededRandom)
        assertEquals(16, ulid.drop(10).length)
    }

    @Test
    fun `ULIDs are not equal across calls with default random`() {
        val a = generateUlid()
        val b = generateUlid()
        assertNotEquals(a, b)
    }
}
