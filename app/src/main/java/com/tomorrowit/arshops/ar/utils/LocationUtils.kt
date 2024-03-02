package com.tomorrowit.arshops.ar.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {

    /**
     * Bearing in degrees between two coordinates.
     * [0-360] Clockwise
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    @JvmStatic
    fun bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val latitude1 = Math.toRadians(lat1)
        val latitude2 = Math.toRadians(lat2)
        val longDiff = Math.toRadians(lon2 - lon1)
        val y = sin(longDiff) * cos(latitude2)
        val x =
            cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(
                longDiff
            )
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    /**
     * Distance in metres between two coordinates
     *
     * @param lat1
     * @param lat2
     * @param lon1
     * @param lon2
     * @param el1  - Elevation 1
     * @param el2  - Elevation 2
     * @return
     */
    @JvmStatic
    fun distance(
        lat1: Double, lat2: Double, lon1: Double,
        lon2: Double, el1: Double, el2: Double
    ): Double {
        val R = 6371 // Radius of the earth
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = (sin(latDistance / 2) * sin(latDistance / 2)
                + (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                * sin(lonDistance / 2) * sin(lonDistance / 2)))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var distance = R * c * 1000 // convert to meters
        val height = el1 - el2
        distance = distance.pow(2.0) + height.pow(2.0)
        return sqrt(distance)
    }
}