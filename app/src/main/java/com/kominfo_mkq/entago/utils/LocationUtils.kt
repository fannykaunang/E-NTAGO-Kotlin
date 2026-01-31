package com.kominfo_mkq.entago.utils

import android.location.Location

object LocationUtils {
    private const val MAX_RADIUS_METERS = 10000.0

    fun isWithinRadius(
        userLat: Double,
        userLng: Double,
        officeLat: Double,
        officeLng: Double
    ): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            userLat, userLng, officeLat, officeLng, results
        )
        return results[0] <= MAX_RADIUS_METERS
    }

    fun isMockLocation(location: android.location.Location): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }
}