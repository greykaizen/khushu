package com.kaizen.khushu.ui.screens.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.local.IslamicEventsCatalog
import com.kaizen.khushu.data.repository.PrayerTimeRepository
import com.kaizen.khushu.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModelProvider

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val prayerTimeRepository: PrayerTimeRepository
) : ViewModel() {

    private val _currentTime = MutableStateFlow(Date())
    private val _previewTimeMillis = MutableStateFlow<Long?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    // State for API prayer times to avoid re-fetching on every tick
    private val _apiPrayerTimes = MutableStateFlow<Map<String, String>?>(null)
    private var lastApiFetchKey: String? = null

    // Fallback UI State
    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settingsFlow,
        _currentTime,
        _previewTimeMillis,
        _isRefreshing
    ) { settings, currentTime, previewTimeMillis, isRefreshing ->
        val effectiveCurrentTime = previewTimeMillis?.let(::Date) ?: currentTime
        // 1. Calculate Prayer Times
        val localMethodSupported = prayerTimeRepository.supportsLocalCalculationMethod(
            settings.prayerCalculationMethod
        )
        val isApiSource = settings.prayerSourceType == "API" || !localMethodSupported

        // Check if we need to fetch API times (once per day or on setting change)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(effectiveCurrentTime)
        val apiFetchKey = listOf(
            todayStr,
            settings.locationLat,
            settings.locationLng,
            settings.prayerCalculationMethod,
            settings.prayerMadhab
        ).joinToString("|")
        if (isApiSource && (lastApiFetchKey != apiFetchKey)) {
            viewModelScope.launch {
                val timings = prayerTimeRepository.getFallbackPrayerTimes(
                    effectiveCurrentTime,
                    settings.locationLat.toDouble(),
                    settings.locationLng.toDouble(),
                    settings.prayerCalculationMethod,
                    settings.prayerMadhab
                )
                _apiPrayerTimes.value = timings
                lastApiFetchKey = apiFetchKey
                settingsRepository.updateLastPrayerRefresh(System.currentTimeMillis())
                _isRefreshing.value = false
            }
        }

        val prayerTimes = runCatching {
            prayerTimeRepository.getLocalPrayerTimes(
                date = effectiveCurrentTime,
                lat = settings.locationLat.toDouble(),
                lng = settings.locationLng.toDouble(),
                methodStr = settings.prayerCalculationMethod,
                madhabStr = settings.prayerMadhab
            )
        }.getOrElse {
            return@combine HomeUiState(
                currentTimeMillis = effectiveCurrentTime.time,
                isRefreshing = isRefreshing,
                usingPreviewTime = previewTimeMillis != null,
                lastPrayerRefreshEpochMs = settings.lastPrayerRefreshEpochMs,
                locationLat = settings.locationLat,
                locationLng = settings.locationLng,
                calculationSource = if (isApiSource) CalculationSource.API else CalculationSource.LOCAL
            )
        }

        // API values helper
        val apiTimings = if (isApiSource) _apiPrayerTimes.value else null

        fun parseApiTime(key: String, fallback: Date): Date {
            val timeStr = apiTimings?.get(key) ?: return fallback
            return try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val normalizedTime = timeStr.take(5)
                val parsed = sdf.parse(normalizedTime) ?: return fallback
                val cal = Calendar.getInstance().apply { time = effectiveCurrentTime }
                val parsedCal = Calendar.getInstance().apply { time = parsed }
                cal.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, 0)
                cal.time
            } catch (e: Exception) {
                fallback
            }
        }

        val fajrDate = if (isApiSource) parseApiTime("Fajr", prayerTimes.fajr) else prayerTimes.fajr
        val sunriseDate = if (isApiSource) parseApiTime("Sunrise", prayerTimes.sunrise) else prayerTimes.sunrise
        val dhuhrDate = if (isApiSource) parseApiTime("Dhuhr", prayerTimes.dhuhr) else prayerTimes.dhuhr
        val asrDate = if (isApiSource) parseApiTime("Asr", prayerTimes.asr) else prayerTimes.asr
        val maghribDate = if (isApiSource) parseApiTime("Maghrib", prayerTimes.maghrib) else prayerTimes.maghrib
        val ishaDate = if (isApiSource) parseApiTime("Isha", prayerTimes.isha) else prayerTimes.isha

        fun applyOffset(date: Date, minutes: Int): Date {
            return Date(date.time + minutes * 60_000L)
        }

        val adjustedFajrDate = applyOffset(fajrDate, settings.fajrOffsetMinutes)
        val adjustedDhuhrDate = applyOffset(dhuhrDate, settings.dhuhrOffsetMinutes)
        val adjustedAsrDate = applyOffset(asrDate, settings.asrOffsetMinutes)
        val adjustedMaghribDate = applyOffset(maghribDate, settings.maghribOffsetMinutes)
        val adjustedIshaDate = applyOffset(ishaDate, settings.ishaOffsetMinutes)

        // 2. Map Time to Arc Bounds (Fajr -> Isha mapped to 0.08 -> 0.93)
        val fajrMs = adjustedFajrDate.time
        val ishaMs = adjustedIshaDate.time

        // Helper to get raw timestamp of a prayer on a specific day
        fun getPrayerTime(hour: Int, min: Int, daysOffset: Int = 0): Long {
            val cal = Calendar.getInstance().apply {
                time = effectiveCurrentTime
                add(Calendar.DAY_OF_YEAR, daysOffset)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun tToArc(ms: Long): Float {
            val total = (ishaMs - fajrMs).toFloat()
            if (total <= 0) return 0.5f
            val ratio = (ms - fajrMs).toFloat() / total
            return 0.08f + ratio * (0.93f - 0.08f)
        }

        // 3. Current Sun Arc
        val currentArcT = tToArc(effectiveCurrentTime.time).coerceIn(-0.1f, 1.1f)

        // 4. Build PrayerInfo List
        val fajrCal = Calendar.getInstance().apply { time = adjustedFajrDate }
        val dhuhrCal = Calendar.getInstance().apply { time = adjustedDhuhrDate }
        val asrCal = Calendar.getInstance().apply { time = adjustedAsrDate }
        val maghribCal = Calendar.getInstance().apply { time = adjustedMaghribDate }
        val ishaCal = Calendar.getInstance().apply { time = adjustedIshaDate }

        val mappedPrayers = listOf(
            PrayerInfo(
                name = "Fajr", ar = "الفجر",
                time = timeFormatter.format(adjustedFajrDate),
                hour = fajrCal.get(Calendar.HOUR_OF_DAY), minute = fajrCal.get(Calendar.MINUTE),
                arcT = tToArc(fajrMs),
                dotColorLight = Color(0xFF4a70b0), dotColorDark = Color(0xFF6890d8),
                rawTime = getPrayerTime(fajrCal.get(Calendar.HOUR_OF_DAY), fajrCal.get(Calendar.MINUTE))
            ),
            PrayerInfo(
                name = "Dhuhr", ar = "الظهر",
                time = timeFormatter.format(adjustedDhuhrDate),
                hour = dhuhrCal.get(Calendar.HOUR_OF_DAY), minute = dhuhrCal.get(Calendar.MINUTE),
                arcT = tToArc(adjustedDhuhrDate.time),
                dotColorLight = Color(0xFFa87010), dotColorDark = Color(0xFFd4a828),
                rawTime = getPrayerTime(dhuhrCal.get(Calendar.HOUR_OF_DAY), dhuhrCal.get(Calendar.MINUTE))
            ),
            PrayerInfo(
                name = "Asr", ar = "العصر",
                time = timeFormatter.format(adjustedAsrDate),
                hour = asrCal.get(Calendar.HOUR_OF_DAY), minute = asrCal.get(Calendar.MINUTE),
                arcT = tToArc(adjustedAsrDate.time),
                dotColorLight = Color(0xFFa06020), dotColorDark = Color(0xFFd08840),
                rawTime = getPrayerTime(asrCal.get(Calendar.HOUR_OF_DAY), asrCal.get(Calendar.MINUTE))
            ),
            PrayerInfo(
                name = "Maghrib", ar = "المغرب",
                time = timeFormatter.format(adjustedMaghribDate),
                hour = maghribCal.get(Calendar.HOUR_OF_DAY), minute = maghribCal.get(Calendar.MINUTE),
                arcT = tToArc(adjustedMaghribDate.time),
                dotColorLight = Color(0xFF9a3828), dotColorDark = Color(0xFFe06050),
                rawTime = getPrayerTime(maghribCal.get(Calendar.HOUR_OF_DAY), maghribCal.get(Calendar.MINUTE))
            ),
            PrayerInfo(
                name = "Isha", ar = "العشاء",
                time = timeFormatter.format(adjustedIshaDate),
                hour = ishaCal.get(Calendar.HOUR_OF_DAY), minute = ishaCal.get(Calendar.MINUTE),
                arcT = tToArc(ishaMs),
                dotColorLight = Color(0xFF584898), dotColorDark = Color(0xFF9070d0),
                rawTime = getPrayerTime(ishaCal.get(Calendar.HOUR_OF_DAY), ishaCal.get(Calendar.MINUTE))
            )
        )

        // 5. Build Makruh Zones
        val min20 = 20 * 60 * 1000L
        val min15 = 15 * 60 * 1000L

        val makruhSunrise = MakruhZone(
            tStart = tToArc(sunriseDate.time),
            tEnd = tToArc(sunriseDate.time + min20),
            label = "Sunrise",
            description = "Disliked to pray until the sun rises a spear’s height (~20 min after sunrise)"
        )
        val makruhZawal = MakruhZone(
            tStart = tToArc(adjustedDhuhrDate.time - min15),
            tEnd = tToArc(adjustedDhuhrDate.time),
            label = "Zawal",
            description = "Sun at exact zenith — forbidden to pray at this moment, just before Dhuhr begins"
        )
        val makruhSunset = MakruhZone(
            tStart = tToArc(adjustedMaghribDate.time - min15),
            tEnd = tToArc(adjustedMaghribDate.time),
            label = "Sunset",
            description = "Disliked to pray as the sun sets until it fully disappears below the horizon"
        )

        // 6. Build Hijri Date & Events without letting ancillary failures break prayer UI
        val todayHijri = runCatching { HijrahDate.now() }.getOrNull()
        val formattedHijri = runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
            todayHijri?.format(formatter).orEmpty()
        }.getOrDefault("")

        val mappedEvents = runCatching {
            if (todayHijri == null) {
                emptyList()
            } else {
                IslamicEventsCatalog.getUpcomingEvents(6).map { ev ->
                    val currentM = todayHijri.get(ChronoField.MONTH_OF_YEAR)
                    val currentD = todayHijri.get(ChronoField.DAY_OF_MONTH)

                    var eventYear = todayHijri.get(ChronoField.YEAR)
                    if (ev.month < currentM || (ev.month == currentM && ev.day < currentD)) {
                        eventYear += 1
                    }

                    val eventDate = try {
                        todayHijri
                            .with(ChronoField.YEAR, eventYear.toLong())
                            .with(ChronoField.MONTH_OF_YEAR, ev.month.toLong())
                            .with(ChronoField.DAY_OF_MONTH, ev.day.toLong())
                    } catch (e: Exception) {
                        todayHijri
                    }

                    val daysBetween = ChronoUnit.DAYS.between(todayHijri, eventDate)
                    val dateLabel = when (daysBetween) {
                        0L -> "Today"
                        1L -> "Tomorrow"
                        else -> "in $daysBetween days"
                    }

                    IslamicEvent(
                        label = dateLabel,
                        date = "${ev.day} ${getMonthName(ev.month)}",
                        name = ev.title,
                        isToday = daysBetween == 0L
                    )
                }
            }
        }.getOrDefault(emptyList())

        HomeUiState(
            prayers = mappedPrayers,
            makruhZones = listOf(makruhSunrise, makruhZawal, makruhSunset),
            events = mappedEvents,
            hijriDate = formattedHijri,
            sunArcT = currentArcT,
            currentTimeMillis = effectiveCurrentTime.time,
            isRefreshing = isRefreshing,
            usingPreviewTime = previewTimeMillis != null,
            lastPrayerRefreshEpochMs = settings.lastPrayerRefreshEpochMs,
            locationLat = settings.locationLat,
            locationLng = settings.locationLng,
            calculationSource = if (isApiSource) CalculationSource.API else CalculationSource.LOCAL
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = Date()
                delay(60000) // Update every minute
            }
        }
    }

    fun refreshPrayerData() {
        _isRefreshing.value = true
        lastApiFetchKey = null
        _currentTime.value = Date()
        viewModelScope.launch {
            settingsRepository.updateLastPrayerRefresh(System.currentTimeMillis())
            if (_isRefreshing.value) {
                _isRefreshing.value = false
            }
        }
    }

    fun setPreviewTime(hourOfDay: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            time = Date()
            set(Calendar.HOUR_OF_DAY, hourOfDay.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _previewTimeMillis.value = cal.timeInMillis
    }

    fun clearPreviewTime() {
        _previewTimeMillis.value = null
    }

    private fun getMonthName(month: Int): String {
        return listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        ).getOrElse(month - 1) { "Unknown" }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            prayerTimeRepository: PrayerTimeRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(settingsRepository, prayerTimeRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
