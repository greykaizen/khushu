package com.kaizen.khushu.data.repository

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.SunnahTimes
import com.batoulapps.adhan.data.DateComponents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.Date
import java.util.Calendar

@Serializable
data class AlAdhanResponse(val code: Int, val data: AlAdhanData)

@Serializable
data class AlAdhanData(val timings: Map<String, String>)

class PrayerTimeRepository(
    private val settingsRepository: SettingsRepository
) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun supportsLocalCalculationMethod(methodStr: String): Boolean {
        return when (methodStr) {
            "MUSLIM_WORLD_LEAGUE",
            "EGYPTIAN",
            "KARACHI",
            "UMM_AL_QURA",
            "DUBAI",
            "MOON_SIGHTING_COMMITTEE",
            "NORTH_AMERICA",
            "KUWAIT",
            "QATAR",
            "SINGAPORE" -> true
            "TEHRAN",
            "TURKEY" -> false
            else -> false
        }
    }

    fun getLocalPrayerTimes(
        date: Date,
        lat: Double,
        lng: Double,
        methodStr: String,
        madhabStr: String
    ): PrayerTimes {
        val coordinates = Coordinates(lat, lng)
        val calendar = Calendar.getInstance().apply { time = date }
        val dateComponents = DateComponents.from(date)
        
        val parameters = getCalculationParameters(methodStr).apply {
            madhab = if (madhabStr == "HANAFI") Madhab.HANAFI else Madhab.SHAFI
        }

        return PrayerTimes(coordinates, dateComponents, parameters)
    }

    fun getSunnahTimes(prayerTimes: PrayerTimes): SunnahTimes {
        return SunnahTimes(prayerTimes)
    }

    suspend fun getFallbackPrayerTimes(
        date: Date,
        lat: Double,
        lng: Double,
        methodStr: String,
        madhabStr: String
    ): Map<String, String>? = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance().apply { time = date }
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            val methodId = getApiMethodId(methodStr)
            val school = if (madhabStr == "HANAFI") 1 else 0
            val timeZoneString = URLEncoder.encode(calendar.timeZone.id, Charsets.UTF_8.name())

            val url = buildString {
                append("https://api.aladhan.com/v1/timings/")
                append("$day-$month-$year")
                append("?latitude=$lat")
                append("&longitude=$lng")
                append("&method=$methodId")
                append("&school=$school")
                append("&timezonestring=$timeZoneString")
                if (methodStr == "MOON_SIGHTING_COMMITTEE") {
                    append("&shafaq=general")
                }
            }
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string()
                    if (bodyStr != null) {
                        val alAdhanResponse = json.decodeFromString<AlAdhanResponse>(bodyStr)
                        return@withContext alAdhanResponse.data.timings
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun getCalculationParameters(methodStr: String): CalculationParameters {
        return when (methodStr) {
            "MUSLIM_WORLD_LEAGUE" -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            "EGYPTIAN" -> CalculationMethod.EGYPTIAN.parameters
            "KARACHI" -> CalculationMethod.KARACHI.parameters
            "UMM_AL_QURA" -> CalculationMethod.UMM_AL_QURA.parameters
            "DUBAI" -> CalculationMethod.DUBAI.parameters
            "MOON_SIGHTING_COMMITTEE" -> CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters
            "NORTH_AMERICA" -> CalculationMethod.NORTH_AMERICA.parameters
            "KUWAIT" -> CalculationMethod.KUWAIT.parameters
            "QATAR" -> CalculationMethod.QATAR.parameters
            "SINGAPORE" -> CalculationMethod.SINGAPORE.parameters
            "TEHRAN" -> CalculationMethod.OTHER.parameters
            "TURKEY" -> CalculationMethod.OTHER.parameters
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        }
    }

    private fun getApiMethodId(methodStr: String): Int {
        return when (methodStr) {
            "KARACHI" -> 1
            "NORTH_AMERICA" -> 2
            "MUSLIM_WORLD_LEAGUE" -> 3
            "UMM_AL_QURA" -> 4
            "EGYPTIAN" -> 5
            "TEHRAN" -> 7
            "DUBAI" -> 16
            "KUWAIT" -> 9
            "QATAR" -> 10
            "SINGAPORE" -> 11
            "TURKEY" -> 13
            "MOON_SIGHTING_COMMITTEE" -> 15
            else -> 3
        }
    }
}
