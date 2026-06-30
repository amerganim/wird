package com.wird.feature.qibla

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies the great-circle Qibla bearing against widely-published reference
 * values (true north). Tolerance 2.5° absorbs rounding / slightly different
 * Kaaba coordinates between sources.
 */
class QiblaMathTest {

    private fun assertBearing(expected: Double, lat: Double, lng: Double) {
        val actual = QiblaMath.bearingToKaaba(lat, lng).toDouble()
        assertEquals("bearing for ($lat,$lng)", expected, actual, 2.5)
    }

    @Test
    fun `bearing matches known cities`() {
        assertBearing(277.5, 23.8103, 90.4125) // Dhaka
        assertBearing(118.99, 51.5074, -0.1278) // London
        assertBearing(58.48, 40.7128, -74.0060) // New York
        assertBearing(295.0, -6.2088, 106.8456) // Jakarta
        assertBearing(136.0, 30.0444, 31.2357) // Cairo
        assertBearing(151.5, 41.0082, 28.9784) // Istanbul
    }

    @Test
    fun `bearing is always within 0 to 360`() {
        for (lat in -80..80 step 20) {
            for (lng in -180..180 step 30) {
                val b = QiblaMath.bearingToKaaba(lat.toDouble(), lng.toDouble())
                assert(b in 0f..360f) { "out of range at $lat,$lng: $b" }
            }
        }
    }
}
