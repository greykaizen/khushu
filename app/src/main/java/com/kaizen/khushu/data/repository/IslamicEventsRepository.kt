package com.kaizen.khushu.data.repository

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

@Serializable
data class IslamicMonthEventsJson(
    val month_id: Int,
    val month_name: String,
    val events: List<IslamicMonthEventJson>,
)

@Serializable
data class IslamicMonthEventJson(
    val day: Int,
    val title: String,
    val description: String,
    val type: String,
    val notes: String? = null,
    val perspective: String = "UNIVERSAL",
)

data class IslamicCalendarEvent(
    val month: Int,
    val monthNameEnglish: String,
    val day: Int,
    val endDay: Int = day,
    val title: String,
    val description: String,
    val type: String,
    val notes: String? = null,
    val perspective: String = "UNIVERSAL",
)

class IslamicEventsRepository(
    private val context: Context
) {
    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }
    @Volatile
    private var cachedEvents: List<IslamicCalendarEvent>? = null

    fun getUpcomingEvents(limit: Int = 6, perspective: String = "UNIVERSAL"): List<IslamicCalendarEvent> {
        return getAllEvents(perspective).let { events ->
            if (events.isEmpty()) return emptyList()

            val today = HijrahDate.now()
            val currentMonth = today.get(ChronoField.MONTH_OF_YEAR)
            val currentDay = today.get(ChronoField.DAY_OF_MONTH)

            events.filter {
                (it.month == currentMonth && it.day >= currentDay) || it.month > currentMonth
            }.sortedWith(compareBy({ it.month }, { it.day }))
                .plus(
                    events.filter { it.month < currentMonth || (it.month == currentMonth && it.day < currentDay) }
                        .sortedWith(compareBy({ it.month }, { it.day }))
                )
                .mergeConsecutiveSpans()
                .take(limit)
        }
    }

    fun getEventsForMonth(month: Int, perspective: String = "UNIVERSAL"): List<IslamicCalendarEvent> {
        return getAllEvents(perspective)
            .filter { it.month == month }
            .sortedWith(compareBy({ it.day }, { it.title }))
    }

    fun getAllEvents(perspective: String = "UNIVERSAL"): List<IslamicCalendarEvent> {
        val allEvents = cachedEvents ?: loadEvents()
            .sortedWith(compareBy({ it.month }, { it.day }, { it.title }))
            .mergeConsecutiveSpans()
            .also { cachedEvents = it }
        return when (perspective) {
            "ALL" -> allEvents
            "SUNNI" -> allEvents.filter {
                it.perspective == "UNIVERSAL" || it.perspective == "SUNNI"
            }
            "SHIA" -> allEvents.filter {
                it.perspective == "UNIVERSAL" || it.perspective == "SHIA"
            }
            else -> allEvents.filter { it.perspective == "UNIVERSAL" }
        }
    }

    private fun loadEvents(): List<IslamicCalendarEvent> {
        return try {
            val raw = appContext.assets.open("catalogs/islamic-month-events.json")
                .bufferedReader()
                .use { it.readText() }
            json.decodeFromString<List<IslamicMonthEventsJson>>(raw)
                .flatMap { month ->
                    month.events.map { event ->
                        IslamicCalendarEvent(
                            month = month.month_id,
                            monthNameEnglish = month.month_name,
                            day = event.day,
                            title = event.title,
                            description = event.description,
                            type = event.type,
                            notes = event.notes,
                            perspective = event.perspective.ifBlank { "UNIVERSAL" }
                        )
                    }
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun List<IslamicCalendarEvent>.mergeConsecutiveSpans(): List<IslamicCalendarEvent> {
        if (isEmpty()) return emptyList()

        val merged = mutableListOf<IslamicCalendarEvent>()
        for (event in this) {
            val last = merged.lastOrNull()
            if (last != null &&
                last.month == event.month &&
                last.monthNameEnglish == event.monthNameEnglish &&
                last.title == event.title &&
                last.description == event.description &&
                last.type == event.type &&
                last.notes == event.notes &&
                last.perspective == event.perspective &&
                event.day == last.endDay + 1
            ) {
                merged[merged.lastIndex] = last.copy(endDay = event.day)
            } else {
                merged += event
            }
        }
        return merged
    }
}
