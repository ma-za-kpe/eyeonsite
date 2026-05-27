@file:Suppress("ktlint:standard:filename")

package com.maku.eyeonsite.platform

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AndroidGeocoderService(
    private val context: Context,
) : GeocoderService {
    @Suppress("DEPRECATION", "InjectDispatcher")
    override suspend fun reverseGeocode(
        lat: Double,
        lng: Double,
    ): String? =
        withContext(Dispatchers.IO) {
            try {
                Geocoder(context, Locale.getDefault())
                    .getFromLocation(lat, lng, 1)
                    ?.firstOrNull()
                    ?.getAddressLine(0)
            } catch (_: Exception) {
                null
            }
        }
}
