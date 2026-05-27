@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.ui.evidence

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("MagicNumber", "InjectDispatcher")
class LocalPhotoQualityAnalyzer : PhotoQualityAnalyzer {
    override suspend fun analyze(bytes: ByteArray): PhotoAnalysisResult =
        withContext(Dispatchers.Default) {
            val bmp =
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: return@withContext PhotoAnalysisResult(blurScore = 0f, brightnessScore = 0f)
            val result =
                PhotoAnalysisResult(
                    blurScore = computeBlurScore(bmp),
                    brightnessScore = computeBrightnessScore(bmp),
                )
            bmp.recycle()
            result
        }

    private fun computeBlurScore(bmp: Bitmap): Float {
        val w = 80
        val h = 80
        val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        scaled.recycle()

        // Laplacian variance: high variance ≡ sharp edges present
        var sum = 0.0
        var sumSq = 0.0
        val inner = pixels.size - w
        for (i in w until inner) {
            val lap =
                luminance(pixels[i]) * 4 -
                    luminance(pixels[i - 1]) -
                    luminance(pixels[i + 1]) -
                    luminance(pixels[i - w]) -
                    luminance(pixels[i + w])
            sum += lap
            sumSq += lap * lap
        }
        val n = (inner - w).toDouble()
        val variance = sumSq / n - (sum / n) * (sum / n)
        return (variance / BLUR_SCALE).toFloat().coerceIn(0f, 1f)
    }

    private fun computeBrightnessScore(bmp: Bitmap): Float {
        val w = 40
        val h = 40
        val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        scaled.recycle()

        val avg = pixels.sumOf { luminance(it).toDouble() }.toFloat() / pixels.size
        return when {
            avg < 0.1f -> avg * 10f
            avg > 0.9f -> (1f - avg) * 10f
            else -> 1f
        }
    }

    private fun luminance(px: Int): Float {
        val r = (px shr 16 and 0xFF) / 255f
        val g = (px shr 8 and 0xFF) / 255f
        val b = (px and 0xFF) / 255f
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    companion object {
        private const val BLUR_SCALE = 0.05
    }
}
