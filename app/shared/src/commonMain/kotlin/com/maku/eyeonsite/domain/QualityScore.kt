package com.maku.eyeonsite.domain

@Suppress("MagicNumber")
fun computeQualityScore(
    photos: List<EvidencePhoto>,
    checklistAnswers: List<ChecklistAnswer> = emptyList(),
): Float {
    if (photos.isEmpty()) return 0f

    val uploadRatio =
        photos.count { it.uploadStatus == UploadStatus.UPLOADED }.toFloat() / photos.size

    val photoCountScore =
        (photos.size.toFloat() / EvidencePackage.MAX_PHOTOS).coerceAtMost(1f)

    val checklistScore =
        if (checklistAnswers.isEmpty()) {
            1f
        } else {
            checklistAnswers.count { it.isAnswered }.toFloat() / checklistAnswers.size
        }

    return (uploadRatio * 0.6f + photoCountScore * 0.25f + checklistScore * 0.15f)
        .coerceIn(0f, 1f)
}
