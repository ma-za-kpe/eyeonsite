package com.maku.eyeonsite.domain

data class EvidencePackage(
    val taskId: String,
    val photos: List<EvidencePhoto>,
) {
    fun isValid(): Boolean = photos.isNotEmpty() && photos.size <= MAX_PHOTOS

    companion object {
        const val MAX_PHOTOS = 10
    }
}
