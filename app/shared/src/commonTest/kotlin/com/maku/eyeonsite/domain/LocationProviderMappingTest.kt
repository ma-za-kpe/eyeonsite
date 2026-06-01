package com.maku.eyeonsite.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class LocationProviderMappingTest {
    @Test
    fun `gps string maps to GPS`() {
        assertEquals(LocationProvider.GPS, "gps".toLocationProvider())
    }

    @Test
    fun `fused string maps to FUSED`() {
        assertEquals(LocationProvider.FUSED, "fused".toLocationProvider())
    }

    @Test
    fun `network string maps to NETWORK`() {
        assertEquals(LocationProvider.NETWORK, "network".toLocationProvider())
    }

    @Test
    fun `empty string maps to MANUAL`() {
        assertEquals(LocationProvider.MANUAL, "".toLocationProvider())
    }

    @Test
    fun `unknown string maps to UNKNOWN`() {
        assertEquals(LocationProvider.UNKNOWN, "passive".toLocationProvider())
        assertEquals(LocationProvider.UNKNOWN, "wifi".toLocationProvider())
    }

    @Test
    fun `mapping is case insensitive`() {
        assertEquals(LocationProvider.GPS, "GPS".toLocationProvider())
        assertEquals(LocationProvider.FUSED, "Fused".toLocationProvider())
        assertEquals(LocationProvider.NETWORK, "NETWORK".toLocationProvider())
    }
}
