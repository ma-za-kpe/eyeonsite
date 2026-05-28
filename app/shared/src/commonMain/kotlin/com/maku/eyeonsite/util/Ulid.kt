package com.maku.eyeonsite.util

import kotlin.time.Clock
import kotlin.random.Random

private const val ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
private const val TIMESTAMP_LEN = 10
private const val RANDOM_LEN = 16

@Suppress("MagicNumber")
fun generateUlid(
    timestampMs: Long = Clock.System.now().toEpochMilliseconds(),
    random: Random = Random.Default,
): String {
    val chars = CharArray(TIMESTAMP_LEN + RANDOM_LEN)

    // Encode 48-bit timestamp into 10 base-32 characters (MSB first)
    var t = timestampMs
    for (i in TIMESTAMP_LEN - 1 downTo 0) {
        chars[i] = ENCODING[(t and 0x1FL).toInt()]
        t = t ushr 5
    }

    // Encode 80 random bits into 16 base-32 characters
    val randomBytes = random.nextBytes(10)
    var bitBuf = 0L
    var bitsLeft = 0
    var charIdx = TIMESTAMP_LEN
    for (byte in randomBytes) {
        bitBuf = (bitBuf shl 8) or (byte.toLong() and 0xFFL)
        bitsLeft += 8
        while (bitsLeft >= 5) {
            bitsLeft -= 5
            chars[charIdx++] = ENCODING[((bitBuf ushr bitsLeft) and 0x1FL).toInt()]
        }
    }

    return chars.concatToString()
}
