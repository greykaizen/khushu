package com.kaizen.khushu.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object LocationUtil {
    
    fun isGenericLabel(label: String?): Boolean {
        if (label.isNullOrBlank()) return true
        val lower = label.lowercase().trim()
        return lower == "your area" || lower == "location" || lower == "unknown"
    }

    @Suppress("DEPRECATION")
    fun resolveLocationName(context: Context, lat: Double, lng: Double): String? {
        if (lat == 0.0 && lng == 0.0) return null
        
        return runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val results = mutableListOf<android.location.Address>()
                val latch = CountDownLatch(1)
                geocoder.getFromLocation(lat, lng, 1) { found ->
                    results += found
                    latch.countDown()
                }
                // Increased timeout to 8 seconds for even more robustness
                latch.await(8, TimeUnit.SECONDS)
                results
            } else {
                geocoder.getFromLocation(lat, lng, 1).orEmpty()
            }

            val best = addresses.firstOrNull() ?: return@runCatching null
            
            // Try different fields in order of preference
            listOfNotNull(
                best.locality?.takeIf { it.isNotBlank() },
                best.subAdminArea?.takeIf { it.isNotBlank() },
                best.adminArea?.takeIf { it.isNotBlank() },
                best.featureName?.takeIf { it.isNotBlank() && !it.contains(Regex("\\d")) }, // Skip if it's just a number or building ID
                best.countryName?.takeIf { it.isNotBlank() }
            ).firstOrNull()
        }.getOrNull()
    }
}
