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
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

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
            "SINGAPORE",
            "ALGERIA",
            "TUNISIA",
            "FRANCE_UOIF",
            "FRANCE_15",
            "FRANCE_18" -> true
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

    suspend fun getEffectivePrayerDateTimes(
        date: Date,
        settings: UserSettings
    ): Map<String, Date> {
        val localMethodSupported = supportsLocalCalculationMethod(settings.prayerCalculationMethod)
        val isApiSource = settings.prayerSourceType == "API" || !localMethodSupported
        val localPrayerTimes = getLocalPrayerTimes(
            date = date,
            lat = settings.locationLat.toDouble(),
            lng = settings.locationLng.toDouble(),
            methodStr = settings.prayerCalculationMethod,
            madhabStr = settings.prayerMadhab
        )
        val apiTimings = if (isApiSource) {
            getFallbackPrayerTimes(
                date = date,
                lat = settings.locationLat.toDouble(),
                lng = settings.locationLng.toDouble(),
                methodStr = settings.prayerCalculationMethod,
                madhabStr = settings.prayerMadhab
            )
        } else {
            null
        }

        fun parseApiTime(key: String, fallback: Date): Date {
            val timeStr = apiTimings?.get(key) ?: return fallback
            return try {
                val normalizedTime = timeStr.take(5)
                val parsed = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).parse(normalizedTime)
                    ?: return fallback
                val targetCal = Calendar.getInstance().apply { time = date }
                val parsedCal = Calendar.getInstance().apply { time = parsed }
                targetCal.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                targetCal.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                targetCal.set(Calendar.SECOND, 0)
                targetCal.set(Calendar.MILLISECOND, 0)
                targetCal.time
            } catch (e: Exception) {
                fallback
            }
        }

        fun applyOffset(time: Date, minutes: Int): Date = Date(time.time + minutes * 60_000L)

        val fajr = if (isApiSource) parseApiTime("Fajr", localPrayerTimes.fajr) else localPrayerTimes.fajr
        val dhuhr = if (isApiSource) parseApiTime("Dhuhr", localPrayerTimes.dhuhr) else localPrayerTimes.dhuhr
        val asr = if (isApiSource) parseApiTime("Asr", localPrayerTimes.asr) else localPrayerTimes.asr
        val maghrib = if (isApiSource) parseApiTime("Maghrib", localPrayerTimes.maghrib) else localPrayerTimes.maghrib
        val isha = if (isApiSource) parseApiTime("Isha", localPrayerTimes.isha) else localPrayerTimes.isha

        return mapOf(
            "Fajr" to applyOffset(fajr, settings.fajrOffsetMinutes),
            "Dhuhr" to applyOffset(dhuhr, settings.dhuhrOffsetMinutes),
            "Asr" to applyOffset(asr, settings.asrOffsetMinutes),
            "Maghrib" to applyOffset(maghrib, settings.maghribOffsetMinutes),
            "Isha" to applyOffset(isha, settings.ishaOffsetMinutes)
        )
    }

    suspend fun getExtraPrayerDateTimes(
        date: Date,
        settings: UserSettings
    ): Map<String, Date> {
        val localMethodSupported = supportsLocalCalculationMethod(settings.prayerCalculationMethod)
        val isApiSource = settings.prayerSourceType == "API" || !localMethodSupported
        val localPrayerTimes = getLocalPrayerTimes(
            date = date,
            lat = settings.locationLat.toDouble(),
            lng = settings.locationLng.toDouble(),
            methodStr = settings.prayerCalculationMethod,
            madhabStr = settings.prayerMadhab
        )
        val nextDayPrayerTimes = getLocalPrayerTimes(
            date = Date(date.time + TimeUnit.DAYS.toMillis(1)),
            lat = settings.locationLat.toDouble(),
            lng = settings.locationLng.toDouble(),
            methodStr = settings.prayerCalculationMethod,
            madhabStr = settings.prayerMadhab
        )
        val apiTimings = if (isApiSource) {
            getFallbackPrayerTimes(
                date = date,
                lat = settings.locationLat.toDouble(),
                lng = settings.locationLng.toDouble(),
                methodStr = settings.prayerCalculationMethod,
                madhabStr = settings.prayerMadhab
            )
        } else {
            null
        }

        fun parseApiTime(key: String, fallback: Date): Date {
            val timeStr = apiTimings?.get(key) ?: return fallback
            return try {
                val normalizedTime = timeStr.take(5)
                val parsed = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).parse(normalizedTime)
                    ?: return fallback
                val targetCal = Calendar.getInstance().apply { time = date }
                val parsedCal = Calendar.getInstance().apply { time = parsed }
                targetCal.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                targetCal.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                targetCal.set(Calendar.SECOND, 0)
                targetCal.set(Calendar.MILLISECOND, 0)
                targetCal.time
            } catch (e: Exception) {
                fallback
            }
        }

        val localSunrise = localPrayerTimes.sunrise
        val localSunset = localPrayerTimes.maghrib
        val localImsak = Date(localPrayerTimes.fajr.time - TimeUnit.MINUTES.toMillis(10))
        val sunnahTimes = getSunnahTimes(localPrayerTimes)
        val localMidnight = sunnahTimes.middleOfTheNight
        val localLastThird = sunnahTimes.lastThirdOfTheNight
        val firstThirdMillis = localPrayerTimes.maghrib.time +
            ((nextDayPrayerTimes.fajr.time - localPrayerTimes.maghrib.time) / 3L)
        val localFirstThird = Date(firstThirdMillis)

        return mapOf(
            "IMSAK" to if (isApiSource) parseApiTime("Imsak", localImsak) else localImsak,
            "SUNRISE" to if (isApiSource) parseApiTime("Sunrise", localSunrise) else localSunrise,
            "SUNSET" to if (isApiSource) parseApiTime("Sunset", localSunset) else localSunset,
            "FIRST_THIRD" to if (isApiSource) parseApiTime("Firstthird", localFirstThird) else localFirstThird,
            "MIDNIGHT" to if (isApiSource) parseApiTime("Midnight", localMidnight) else localMidnight,
            "LAST_THIRD" to if (isApiSource) parseApiTime("Lastthird", localLastThird) else localLastThird,
        )
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
                if (methodId == 99) {
                    val params = getCalculationParameters(methodStr)
                    append("&methodSettings=${params.fajrAngle},null,${params.ishaAngle}")
                }
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
            "ALGERIA" -> CalculationParameters(18.0, 17.0).apply { method = CalculationMethod.OTHER }
            "TUNISIA" -> CalculationParameters(18.0, 18.0).apply { method = CalculationMethod.OTHER }
            "FRANCE_UOIF" -> CalculationParameters(12.0, 12.0).apply { method = CalculationMethod.OTHER }
            "FRANCE_15" -> CalculationParameters(15.0, 15.0).apply { method = CalculationMethod.OTHER }
            "FRANCE_18" -> CalculationParameters(18.0, 17.0).apply { method = CalculationMethod.OTHER }
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
            "ALGERIA", "TUNISIA", "FRANCE_UOIF", "FRANCE_15", "FRANCE_18" -> 99
            else -> 3
        }
    }
}
