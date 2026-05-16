@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.kaizen.khushu.ui.screens.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaizen.khushu.data.repository.extraPrayerTimingArabicLabel
import com.kaizen.khushu.data.repository.extraPrayerTimingShortLabel
import com.kaizen.khushu.data.repository.IslamicEventsRepository
import com.kaizen.khushu.data.repository.PrayerTimeRepository
import com.kaizen.khushu.data.repository.SettingsRepository
import com.kaizen.khushu.logic.PrayerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
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
    private val prayerTimeRepository: PrayerTimeRepository,
    private val islamicEventsRepository: IslamicEventsRepository,
    val prayerManager: PrayerManager,
) : ViewModel() {

    private val _previewTimeMillis = MutableStateFlow<Long?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    private var refreshStartedAtMillis = 0L

    private suspend fun completeRefreshWithMinimumDuration() {
        val remaining = MIN_REFRESH_DURATION_MS - (System.currentTimeMillis() - refreshStartedAtMillis)
        if (remaining > 0L) {
            delay(remaining)
        }
        _isRefreshing.value = false
    }

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settingsFlow,
        prayerManager.prayerState,
        _previewTimeMillis,
        _isRefreshing
    ) { settings, prayerState, previewTimeMillis, isRefreshing ->
        val usingPreviewTime = previewTimeMillis != null
        val previewTime = previewTimeMillis?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) }
        val effectiveDate = Date(previewTimeMillis ?: System.currentTimeMillis())
        val isApiSource = prayerTimeRepository.usesApiSource(settings)

        if (prayerState == null) {
            return@combine HomeUiState(
                isRefreshing = isRefreshing,
                usingPreviewTime = usingPreviewTime,
                previewTime = previewTime,
                lastPrayerRefreshEpochMs = settings.lastPrayerRefreshEpochMs,
                locationLat = settings.locationLat,
                locationLng = settings.locationLng,
                calculationSource = if (isApiSource) CalculationSource.API else CalculationSource.LOCAL
            )
        }

        val effectivePrayerTimes = prayerTimeRepository.getEffectivePrayerDateTimes(
            date = effectiveDate,
            settings = settings
        ).mapValues { (_, value) -> kotlinx.datetime.Instant.fromEpochMilliseconds(value.time) }

        fun applyOffset(instant: kotlinx.datetime.Instant, minutes: Int): kotlinx.datetime.Instant {
            return kotlinx.datetime.Instant.fromEpochMilliseconds(instant.toEpochMilliseconds() + minutes * 60_000L)
        }

        val adjustedFajr = effectivePrayerTimes["Fajr"] ?: prayerState.allPrayers.fajr
        val adjustedDhuhr = effectivePrayerTimes["Dhuhr"] ?: prayerState.allPrayers.dhuhr
        val adjustedAsr = effectivePrayerTimes["Asr"] ?: prayerState.allPrayers.asr
        val adjustedMaghrib = effectivePrayerTimes["Maghrib"] ?: prayerState.allPrayers.maghrib
        val adjustedIsha = effectivePrayerTimes["Isha"] ?: prayerState.allPrayers.isha

        val extraPrayerTimes = runCatching {
            prayerTimeRepository.getExtraPrayerDateTimes(
                date = effectiveDate,
                settings = settings
            )
        }.getOrDefault(emptyMap())

        val sunriseInstant =
            extraPrayerTimes["SUNRISE"]?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it.time) }
                ?: prayerState.allPrayers.sunrise
        val sunsetInstant = adjustedMaghrib

        // --- NEW SUNPATH MATH: Center on Solar Noon (Zawal) ---
        // We define Solar Noon as the midpoint between Sunrise and Sunset.
        val solarNoonMs = (sunriseInstant.toEpochMilliseconds() + sunsetInstant.toEpochMilliseconds()) / 2L
        val dayMs = 24 * 60 * 60 * 1000L

        /**
         * Maps a timestamp to a 0..1 range representing a 24-hour window
         * centered EXACTLY on Solar Noon (t=0.5).
         */
        fun tToArc(ms: Long): Float {
            val offsetFromNoon = ms - solarNoonMs
            val ratio = offsetFromNoon.toFloat() / dayMs.toFloat()
            // Center is 0.5. 12 hours before is 0.0, 12 hours after is 1.0.
            return (0.5f + ratio).coerceIn(-0.2f, 1.2f)
        }

        fun formatInstant(instant: kotlinx.datetime.Instant): String {
            return timeFormatter.format(Date(instant.toEpochMilliseconds()))
        }

        fun getHourMin(instant: kotlinx.datetime.Instant): Pair<Int, Int> {
            val cal = Calendar.getInstance().apply { timeInMillis = instant.toEpochMilliseconds() }
            return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
        }

        val (fajrHr, fajrMin) = getHourMin(adjustedFajr)
        val (dhuhrHr, dhuhrMin) = getHourMin(adjustedDhuhr)
        val (asrHr, asrMin) = getHourMin(adjustedAsr)
        val (maghribHr, maghribMin) = getHourMin(adjustedMaghrib)
        val (ishaHr, ishaMin) = getHourMin(adjustedIsha)

        val mappedPrayers = listOf(
            PrayerInfo(
                name = "Fajr", ar = "الفجر",
                time = formatInstant(adjustedFajr),
                hour = fajrHr, minute = fajrMin,
                arcT = tToArc(adjustedFajr.toEpochMilliseconds()),
                dotColorLight = androidx.compose.ui.graphics.Color(0xFF4a70b0), dotColorDark = androidx.compose.ui.graphics.Color(0xFF6890d8),
                rawTime = adjustedFajr
            ),
            PrayerInfo(
                name = "Dhuhr", ar = "الظهر",
                time = formatInstant(adjustedDhuhr),
                hour = dhuhrHr, minute = dhuhrMin,
                arcT = tToArc(adjustedDhuhr.toEpochMilliseconds()),
                dotColorLight = androidx.compose.ui.graphics.Color(0xFFa87010), dotColorDark = androidx.compose.ui.graphics.Color(0xFFd4a828),
                rawTime = adjustedDhuhr
            ),
            PrayerInfo(
                name = "Asr", ar = "العصر",
                time = formatInstant(adjustedAsr),
                hour = asrHr, minute = asrMin,
                arcT = tToArc(adjustedAsr.toEpochMilliseconds()),
                dotColorLight = androidx.compose.ui.graphics.Color(0xFFa06020), dotColorDark = androidx.compose.ui.graphics.Color(0xFFd08840),
                rawTime = adjustedAsr
            ),
            PrayerInfo(
                name = "Maghrib", ar = "المغرب",
                time = formatInstant(adjustedMaghrib),
                hour = maghribHr, minute = maghribMin,
                arcT = tToArc(adjustedMaghrib.toEpochMilliseconds()),
                dotColorLight = androidx.compose.ui.graphics.Color(0xFF9a3828), dotColorDark = androidx.compose.ui.graphics.Color(0xFFe06050),
                rawTime = adjustedMaghrib
            ),
            PrayerInfo(
                name = "Isha", ar = "العشاء",
                time = formatInstant(adjustedIsha),
                hour = ishaHr, minute = ishaMin,
                arcT = tToArc(adjustedIsha.toEpochMilliseconds()),
                dotColorLight = androidx.compose.ui.graphics.Color(0xFF584898), dotColorDark = androidx.compose.ui.graphics.Color(0xFF9070d0),
                rawTime = adjustedIsha
            )
        )

        val selectedExtraTimings = settings.selectedExtraPrayerTimings
        val mappedExtraTimings = selectedExtraTimings.mapNotNull { id ->
            val timingDate = extraPrayerTimes[id] ?: return@mapNotNull null
            val timing = kotlinx.datetime.Instant.fromEpochMilliseconds(timingDate.time)
            val (hr, min) = getHourMin(timing)
            PrayerInfo(
                name = extraPrayerTimingShortLabel(id),
                ar = extraPrayerTimingArabicLabel(id),
                time = formatInstant(timing),
                hour = hr,
                minute = min,
                arcT = tToArc(timing.toEpochMilliseconds()),
                dotColorLight = when (id) {
                    "IMSAK" -> androidx.compose.ui.graphics.Color(0xFF5D739C)
                    "SUNRISE" -> androidx.compose.ui.graphics.Color(0xFFCC8B33)
                    "SUNSET" -> androidx.compose.ui.graphics.Color(0xFFAF5A3E)
                    "FIRST_THIRD" -> androidx.compose.ui.graphics.Color(0xFF6F66AF)
                    "MIDNIGHT" -> androidx.compose.ui.graphics.Color(0xFF534E9B)
                    "LAST_THIRD" -> androidx.compose.ui.graphics.Color(0xFF4D79B2)
                    else -> androidx.compose.ui.graphics.Color(0xFF8A8A8A)
                },
                dotColorDark = when (id) {
                    "IMSAK" -> androidx.compose.ui.graphics.Color(0xFF7E95C1)
                    "SUNRISE" -> androidx.compose.ui.graphics.Color(0xFFE7B055)
                    "SUNSET" -> androidx.compose.ui.graphics.Color(0xFFD37858)
                    "FIRST_THIRD" -> androidx.compose.ui.graphics.Color(0xFF8B80D1)
                    "MIDNIGHT" -> androidx.compose.ui.graphics.Color(0xFF7C74D4)
                    "LAST_THIRD" -> androidx.compose.ui.graphics.Color(0xFF70A2E6)
                    else -> androidx.compose.ui.graphics.Color(0xFFB0B0B0)
                },
                rawTime = timing,
                isExtra = true
            )
        }.sortedBy { it.rawTime }

        val min20 = 20 * 60 * 1000L
        val min15 = 15 * 60 * 1000L

        val makruhSunrise = MakruhZone(
            tStart = tToArc(sunriseInstant.toEpochMilliseconds()),
            tEnd = tToArc(sunriseInstant.toEpochMilliseconds() + min20),
            label = "Sunrise",
            description = "Disliked to pray until the sun rises a spear's height (~20 min). \"Whoever catches a rak'ah of Fajr before sunrise has caught Fajr.\" — Bukhari 579, Muslim 608"
        )
        val makruhZawal = MakruhZone(
            tStart = tToArc(adjustedDhuhr.toEpochMilliseconds() - min15),
            tEnd = tToArc(adjustedDhuhr.toEpochMilliseconds()),
            label = "Zawal",
            description = "Sun at zenith — prayer is forbidden at this moment. \"The Prophet ﷺ forbade prayer when the sun is at its peak (Zawal) until it declines.\" — Muslim 831"
        )
        val makruhSunset = MakruhZone(
            tStart = tToArc(adjustedMaghrib.toEpochMilliseconds() - min15),
            tEnd = tToArc(adjustedMaghrib.toEpochMilliseconds()),
            label = "Sunset",
            description = "Disliked to pray as the sun sets. \"Whoever catches a rak'ah of Asr before sunset has caught Asr.\" — Bukhari 556, Muslim 608"
        )

        val todayHijri = runCatching { java.time.chrono.HijrahDate.now() }.getOrNull()
        val formattedHijri = runCatching {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy")
            todayHijri?.format(formatter).orEmpty()
        }.getOrDefault("")
        val currentMonth = todayHijri?.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) ?: 0
        val currentDay = todayHijri?.get(java.time.temporal.ChronoField.DAY_OF_MONTH) ?: 0
        val currentYear = todayHijri?.get(java.time.temporal.ChronoField.YEAR) ?: 0

        fun mapEvent(ev: com.kaizen.khushu.data.repository.IslamicCalendarEvent): IslamicEvent {
            val detailDate = if (ev.day == ev.endDay) {
                "${ev.day} ${ev.monthNameEnglish}"
            } else {
                "${ev.day}-${ev.endDay} ${ev.monthNameEnglish}"
            }
            val isPastInCurrentMonth = ev.month == currentMonth && ev.endDay < currentDay
            val isTodayEvent = ev.month == currentMonth && currentDay in ev.day..ev.endDay

            val dateLabel = if (isPastInCurrentMonth) {
                ""
            } else {
                val eventYear = if (
                    todayHijri != null &&
                    (ev.month < currentMonth || (ev.month == currentMonth && ev.day < currentDay))
                ) {
                    currentYear + 1
                } else {
                    currentYear
                }

                val eventDate = try {
                    todayHijri
                        ?.with(java.time.temporal.ChronoField.YEAR, eventYear.toLong())
                        ?.with(java.time.temporal.ChronoField.MONTH_OF_YEAR, ev.month.toLong())
                        ?.with(java.time.temporal.ChronoField.DAY_OF_MONTH, ev.day.toLong())
                } catch (e: Exception) {
                    todayHijri
                }

                val daysBetween = if (todayHijri != null && eventDate != null) {
                    java.time.temporal.ChronoUnit.DAYS.between(todayHijri, eventDate)
                } else {
                    0L
                }

                when (daysBetween) {
                    0L -> "Today"
                    1L -> "Tomorrow"
                    else -> "in $daysBetween days"
                }
            }

            return IslamicEvent(
                month = ev.month,
                monthNameEnglish = ev.monthNameEnglish,
                day = ev.day,
                endDay = ev.endDay,
                label = dateLabel,
                date = if (dateLabel.isBlank()) detailDate else "$dateLabel | $detailDate",
                name = ev.title,
                description = ev.description,
                notes = ev.notes,
                detailDate = detailDate,
                isToday = isTodayEvent,
            )
        }

        val currentMonthName = islamicEventsRepository.getAllEvents(settings.islamicEventPerspective)
            .firstOrNull { it.month == currentMonth }
            ?.monthNameEnglish
            ?: getMonthName(currentMonth)
        val currentMonthEvents = runCatching {
            islamicEventsRepository.getEventsForMonth(currentMonth, settings.islamicEventPerspective)
                .map(::mapEvent)
                .let { monthEvents ->
                    val activeIndex = monthEvents.indexOfFirst { currentDay in it.day..it.endDay }
                        .takeIf { it >= 0 }
                        ?: monthEvents.indexOfFirst { it.day > currentDay }
                            .takeIf { it >= 0 }
                        ?: monthEvents.lastIndex

                    monthEvents.mapIndexed { index, event ->
                        event.copy(isActive = index == activeIndex)
                    }
                }
        }.getOrDefault(emptyList())
        val calendarEvents = runCatching {
            islamicEventsRepository.getAllEvents(settings.islamicEventPerspective)
                .map(::mapEvent)
        }.getOrDefault(emptyList())
        val eventsHeader = if (currentYear > 0) {
            "UPCOMING · ${currentMonthName.uppercase(Locale.getDefault())} $currentYear"
        } else {
            "UPCOMING EVENTS"
        }

        HomeUiState(
            prayers = mappedPrayers,
            extraTimings = mappedExtraTimings,
            makruhZones = listOf(makruhSunrise, makruhZawal, makruhSunset),
            events = currentMonthEvents,
            calendarEvents = calendarEvents,
            eventsHeader = eventsHeader,
            hijriDate = formattedHijri,
            isRefreshing = isRefreshing,
            usingPreviewTime = usingPreviewTime,
            previewTime = previewTime,
            lastPrayerRefreshEpochMs = settings.lastPrayerRefreshEpochMs,
            locationLat = settings.locationLat,
            locationLng = settings.locationLng,
            calculationSource = if (isApiSource) CalculationSource.API else CalculationSource.LOCAL,
            showExtraPrayerTimingsOnHome = settings.showExtraPrayerTimingsOnHome,
            showUpcomingEventsOnHome = settings.showUpcomingEventsOnHome
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )



    fun refreshPrayerData() {
        refreshStartedAtMillis = System.currentTimeMillis()
        _isRefreshing.value = true
        viewModelScope.launch {
            settingsRepository.updateLastPrayerRefresh(System.currentTimeMillis())
            if (uiState.value.calculationSource == CalculationSource.LOCAL && _isRefreshing.value) {
                completeRefreshWithMinimumDuration()
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
        private const val MIN_REFRESH_DURATION_MS = 700L

        fun factory(
            settingsRepository: SettingsRepository,
            prayerTimeRepository: PrayerTimeRepository,
            islamicEventsRepository: IslamicEventsRepository,
            prayerManager: PrayerManager,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(settingsRepository, prayerTimeRepository, islamicEventsRepository, prayerManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
