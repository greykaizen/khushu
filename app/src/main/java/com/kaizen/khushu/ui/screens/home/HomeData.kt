package com.kaizen.khushu.ui.screens.home

import androidx.compose.ui.graphics.Color

// ── Placeholder data — to be replaced by HomeViewModel + real prayer times ──
enum class CalculationSource { LOCAL, API }

data class PrayerInfo(
    val name: String,
    val ar: String,
    val time: String,
    val hour: Int,
    val minute: Int,
    /** Fractional position on the day arc (0..1) */
    val arcT: Float,
    /** Per-prayer colour dot (not theme-able — it represents the sky colour at that time) */
    val dotColorLight: Color,
    val dotColorDark: Color,
    val rawTime: Long = 0L,
)

data class MakruhZone(
    val tStart: Float,
    val tEnd: Float,
    val label: String,
    val description: String,
)

data class IslamicEvent(
    val month: Int,
    val monthNameEnglish: String,
    val day: Int,
    val endDay: Int = day,
    val label: String,
    val date: String,
    val name: String,
    val description: String = "",
    val notes: String? = null,
    val detailDate: String = date,
    val isToday: Boolean = false,
    val isActive: Boolean = false,
)

data class HomeUiState(
    val prayers: List<PrayerInfo> = emptyList(),
    val makruhZones: List<MakruhZone> = emptyList(),
    val events: List<IslamicEvent> = emptyList(),
    val calendarEvents: List<IslamicEvent> = emptyList(),
    val eventsHeader: String = "",
    val hijriDate: String = "",
//    val ayahText: String = "Verily, in the remembrance of Allah do hearts find rest.",
    val ayahRef: String = "Ar-Ra\u02bbd \u00b7 13:28",
    /** Fractional position of the sun/moon on the day arc (0..1) */
    val sunArcT: Float = 0.5f,
    val currentTimeMillis: Long = 0L,
    val isRefreshing: Boolean = false,
    val usingPreviewTime: Boolean = false,
    val lastPrayerRefreshEpochMs: Long = 0L,
    val locationLat: Float = 0f,
    val locationLng: Float = 0f,
    val calculationSource: CalculationSource = CalculationSource.LOCAL,
)

val PLACEHOLDER_PRAYERS = listOf(
    PrayerInfo(
        name = "Fajr", ar = "\u0627\u0644\u0641\u062c\u0631",
        time = "5:14 AM", hour = 5, minute = 14, arcT = 0.08f,
        dotColorLight = Color(0xFF4a70b0), dotColorDark = Color(0xFF6890d8),
        rawTime = 1734000000000L,
    ),
    PrayerInfo(
        name = "Dhuhr", ar = "\u0627\u0644\u0638\u0647\u0631",
        time = "12:48 PM", hour = 12, minute = 48, arcT = 0.52f,
        dotColorLight = Color(0xFFa87010), dotColorDark = Color(0xFFd4a828),
        rawTime = 1734015000000L,
    ),
    PrayerInfo(
        name = "Asr", ar = "\u0627\u0644\u0639\u0635\u0631",
        time = "4:22 PM", hour = 16, minute = 22, arcT = 0.66f,
        dotColorLight = Color(0xFFa06020), dotColorDark = Color(0xFFd08840),
        rawTime = 1734028000000L,
    ),
    PrayerInfo(
        name = "Maghrib", ar = "\u0627\u0644\u0645\u063a\u0631\u0628",
        time = "7:03 PM", hour = 19, minute = 3, arcT = 0.82f,
        dotColorLight = Color(0xFF9a3828), dotColorDark = Color(0xFFe06050),
        rawTime = 1734038000000L,
    ),
    PrayerInfo(
        name = "Isha", ar = "\u0627\u0644\u0639\u0634\u0627\u0621",
        time = "8:31 PM", hour = 20, minute = 31, arcT = 0.93f,
        dotColorLight = Color(0xFF584898), dotColorDark = Color(0xFF9070d0),
        rawTime = 1734043000000L,
    ),
)

val PLACEHOLDER_MAKRUH = listOf(
    MakruhZone(
        tStart = 0.08f, tEnd = 0.15f, label = "Sunrise",
        description = "Disliked to pray until the sun rises a spear\u2019s height (~20 min after sunrise)",
    ),
    MakruhZone(
        tStart = 0.48f, tEnd = 0.53f, label = "Zawal",
        description = "Sun at exact zenith \u2014 forbidden to pray at this moment, just before Dhuhr begins",
    ),
    MakruhZone(
        tStart = 0.79f, tEnd = 0.84f, label = "Sunset",
        description = "Disliked to pray as the sun sets until it fully disappears below the horizon",
    ),
)

val PLACEHOLDER_EVENTS = listOf(
    IslamicEvent(month = 11, monthNameEnglish = "Dhu al-Qidah", day = 7, label = "Today", date = "7 Dhu\u02bbl-Qi\u02bbdah", name = "Sacred Month", isToday = true),
    IslamicEvent(month = 11, monthNameEnglish = "Dhu al-Qidah", day = 25, label = "25 Dhu\u02bbl-Qi\u02bbdah", date = "in 18 days", name = "Dahw al-Ard"),
    IslamicEvent(month = 12, monthNameEnglish = "Dhu al-Hijjah", day = 1, label = "1 Dhu\u02bbl-Hijjah", date = "in 23 days", name = "Dhul Hijjah begins"),
    IslamicEvent(month = 12, monthNameEnglish = "Dhu al-Hijjah", day = 9, label = "9 Dhu\u02bbl-Hijjah", date = "in 31 days", name = "Yawm Arafah"),
    IslamicEvent(month = 12, monthNameEnglish = "Dhu al-Hijjah", day = 10, label = "10 Dhu\u02bbl-Hijjah", date = "in 32 days", name = "Eid al-Adha"),
    IslamicEvent(month = 12, monthNameEnglish = "Dhu al-Hijjah", day = 13, label = "13 Dhu\u02bbl-Hijjah", date = "in 35 days", name = "End of Tashreeq"),
)
