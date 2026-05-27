package com.maku.eyeonsite.platform

interface GeocoderService {
    suspend fun reverseGeocode(
        lat: Double,
        lng: Double,
    ): String?
}

object NoOpGeocoderService : GeocoderService {
    override suspend fun reverseGeocode(
        lat: Double,
        lng: Double,
    ): String? = null
}
