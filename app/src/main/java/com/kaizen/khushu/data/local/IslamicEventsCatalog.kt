package com.kaizen.khushu.data.local

import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

data class IslamicEvent(
    val month: Int,
    val day: Int,
    val title: String,
    val description: String,
    val type: EventType
) {
    enum class EventType {
        HOLIDAY, HISTORICAL, VIRTUE
    }
}

object IslamicEventsCatalog {
    val events = listOf(
        // Muharram
        IslamicEvent(1, 1, "Islamic New Year", "The start of the Hijri calendar year.", IslamicEvent.EventType.HOLIDAY),
        IslamicEvent(1, 10, "Day of Ashura", "The 10th of Muharram. A day of fasting, commemorating Musa (AS) being saved from Pharaoh.", IslamicEvent.EventType.HOLIDAY),
        
        // Safar
        
        // Rabi' al-Awwal
        IslamicEvent(3, 12, "Mawlid an-Nabi", "Birth of the Prophet Muhammad (PBUH) (widely recognized date).", IslamicEvent.EventType.HISTORICAL),
        
        // Rajab
        IslamicEvent(7, 27, "Isra and Mi'raj", "The Night Journey and Ascension of the Prophet Muhammad (PBUH).", IslamicEvent.EventType.HISTORICAL),
        
        // Sha'ban
        IslamicEvent(8, 15, "Mid-Sha'ban (Laylat al-Bara'at)", "The 15th night of Sha'ban. A night of forgiveness and mercy.", IslamicEvent.EventType.VIRTUE),
        
        // Ramadan
        IslamicEvent(9, 1, "Start of Ramadan", "The month of fasting begins.", IslamicEvent.EventType.HOLIDAY),
        IslamicEvent(9, 17, "Battle of Badr", "The first major battle in Islam, occurring in 2 AH.", IslamicEvent.EventType.HISTORICAL),
        IslamicEvent(9, 20, "Conquest of Makkah", "The peaceful liberation of Makkah in 8 AH.", IslamicEvent.EventType.HISTORICAL),
        IslamicEvent(9, 21, "Laylat al-Qadr Search", "Search for the Night of Decree in the last 10 odd nights.", IslamicEvent.EventType.VIRTUE),
        IslamicEvent(9, 23, "Laylat al-Qadr Search", "Search for the Night of Decree.", IslamicEvent.EventType.VIRTUE),
        IslamicEvent(9, 25, "Laylat al-Qadr Search", "Search for the Night of Decree.", IslamicEvent.EventType.VIRTUE),
        IslamicEvent(9, 27, "Laylat al-Qadr (27th Night)", "Often observed as Laylat al-Qadr by many scholars.", IslamicEvent.EventType.VIRTUE),
        IslamicEvent(9, 29, "Laylat al-Qadr Search", "Search for the Night of Decree.", IslamicEvent.EventType.VIRTUE),
        
        // Shawwal
        IslamicEvent(10, 1, "Eid al-Fitr", "The festival of breaking the fast.", IslamicEvent.EventType.HOLIDAY),
        
        // Dhu al-Hijjah
        IslamicEvent(12, 1, "First 10 Days of Dhul Hijjah", "The best days of the year for good deeds.", IslamicEvent.EventType.VIRTUE),
        IslamicEvent(12, 8, "Day of Tarwiyah", "The first day of Hajj rituals.", IslamicEvent.EventType.HOLIDAY),
        IslamicEvent(12, 9, "Day of Arafah", "The core pillar of Hajj and a recommended day of fasting for non-pilgrims.", IslamicEvent.EventType.HOLIDAY),
        IslamicEvent(12, 10, "Eid al-Adha", "The festival of sacrifice, marking the end of Hajj.", IslamicEvent.EventType.HOLIDAY),
        IslamicEvent(12, 11, "Days of Tashreeq", "11th-13th of Dhul Hijjah. Days of eating, drinking, and remembering Allah.", IslamicEvent.EventType.HOLIDAY),
        IslamicEvent(12, 12, "Days of Tashreeq", "11th-13th of Dhul Hijjah. Days of eating, drinking, and remembering Allah.", IslamicEvent.EventType.HOLIDAY),
        IslamicEvent(12, 13, "Days of Tashreeq", "11th-13th of Dhul Hijjah. Days of eating, drinking, and remembering Allah.", IslamicEvent.EventType.HOLIDAY)
    )

    fun getUpcomingEvents(limit: Int = 5): List<IslamicEvent> {
        val today = HijrahDate.now()
        val currentMonth = today.get(ChronoField.PROLEPTIC_MONTH).toInt() % 12 + 1 // HijrahDate proleptic month isn't 1-12 directly, so use MonthOfYear
        val currentMonthOfYear = today.get(ChronoField.MONTH_OF_YEAR)
        val currentDay = today.get(ChronoField.DAY_OF_MONTH)

        // Sort events so that we look forward from today
        return events.filter { 
            (it.month == currentMonthOfYear && it.day >= currentDay) || (it.month > currentMonthOfYear)
        }.sortedWith(compareBy({ it.month }, { it.day }))
        .plus(
            // Add events from next year if we need more to fill the limit
            events.filter { it.month < currentMonthOfYear || (it.month == currentMonthOfYear && it.day < currentDay) }
            .sortedWith(compareBy({ it.month }, { it.day }))
        ).take(limit)
    }
}
