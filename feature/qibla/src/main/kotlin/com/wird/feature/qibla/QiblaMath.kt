package com.wird.feature.qibla

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaMath {
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LNG = 39.8262

    /** Initial great-circle bearing (degrees, true north, 0..360) from a point to the Kaaba. */
    fun bearingToKaaba(latitude: Double, longitude: Double): Float {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LNG - longitude)
        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val bearing = Math.toDegrees(atan2(y, x))
        return ((bearing + 360.0) % 360.0).toFloat()
    }
}
