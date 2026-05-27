package com.maku.eyeonsite.ui.evidence

data class PhotoAnalysisResult(
    val blurScore: Float,
    val brightnessScore: Float,
) {
    val isAcceptable: Boolean get() = blurScore >= MIN_BLUR_SCORE && brightnessScore >= MIN_BRIGHTNESS_SCORE

    val warningMessage: String?
        get() =
            when {
                blurScore < MIN_BLUR_SCORE -> "Photo may be blurry — consider retaking."
                brightnessScore < MIN_BRIGHTNESS_SCORE -> "Photo is too dark or overexposed — consider retaking."
                else -> null
            }

    companion object {
        const val MIN_BLUR_SCORE = 0.3f
        const val MIN_BRIGHTNESS_SCORE = 0.15f
    }
}

interface PhotoQualityAnalyzer {
    suspend fun analyze(bytes: ByteArray): PhotoAnalysisResult
}

object NoOpPhotoQualityAnalyzer : PhotoQualityAnalyzer {
    override suspend fun analyze(bytes: ByteArray): PhotoAnalysisResult = PhotoAnalysisResult(blurScore = 1f, brightnessScore = 1f)
}
