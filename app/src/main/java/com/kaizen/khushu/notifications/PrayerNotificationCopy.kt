package com.kaizen.khushu.notifications

import android.content.Context
import com.kaizen.khushu.data.repository.extraPrayerTimingLabel
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.util.LocationUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class PrayerNotificationContent(
    val title: String,
    val utilityLine: String,
    val expandedText: String,
)

object PrayerNotificationCopy {
    private val prayerTitles = mapOf(
        "Fajr" to listOf(
            "Fajr is here. Let's begin the day well.",
            "Fajr time. A quiet start is waiting for you.",
            "Fajr is calling. Come, let us rise for prayer.",
            "Fajr time. Let's meet the morning with prayer."
        ),
        "Dhuhr" to listOf(
            "Dhuhr is here. Let's step away and pray.",
            "Dhuhr time. Take the better break now.",
            "Dhuhr is calling. Let us pause for prayer.",
            "Dhuhr time. A small pause, a real reset."
        ),
        "Asr" to listOf(
            "Asr is here. Let's not let the day slip away.",
            "Asr time. Come, let's steady the afternoon.",
            "Asr is calling. A little khushu before the day runs on.",
            "Asr time. Let's return for a moment and pray."
        ),
        "Maghrib" to listOf(
            "Maghrib is here. Let's begin the evening with prayer.",
            "Maghrib time. The day has softened, come pray.",
            "Maghrib is calling. Let us answer before the night settles.",
            "Maghrib time. A grateful evening begins here."
        ),
        "Isha" to listOf(
            "Isha is here. Let's end the day in peace.",
            "Isha time. Come, let the day rest with prayer.",
            "Isha is calling. One last meeting before the night deepens.",
            "Isha time. Let's leave the day lighter than we carried it."
        )
    )

    private val prePrayerTitles = mapOf(
        "Fajr" to listOf(
            "Fajr is close. Let's get ready.",
            "Fajr is coming up. A gentle start is near.",
            "Fajr soon. Time to prepare for prayer."
        ),
        "Dhuhr" to listOf(
            "Dhuhr is close. Let's make room for it.",
            "Dhuhr is coming up. Wrap up and get ready.",
            "Dhuhr soon. A good pause is almost here."
        ),
        "Asr" to listOf(
            "Asr is close. Let's not miss the moment.",
            "Asr is coming up. Time to get ready.",
            "Asr soon. A steadying pause is near."
        ),
        "Maghrib" to listOf(
            "Maghrib is close. Let's get ready for the evening prayer.",
            "Maghrib is coming up. Sunset prayer is near.",
            "Maghrib soon. Let us be ready when it arrives."
        ),
        "Isha" to listOf(
            "Isha is close. Let's prepare to close the day well.",
            "Isha is coming up. A peaceful ending is near.",
            "Isha soon. Time to get ready for prayer."
        )
    )

    private val reflections = mapOf(
        "Fajr" to listOf(
            "A quiet start changes the whole day.",
            "Begin with Allah before the world asks for you.",
            "The morning opens differently when it begins with prayer."
        ),
        "Dhuhr" to listOf(
            "The best break is the one that brings you back to Allah.",
            "A few minutes of prayer can clear the whole middle of the day.",
            "Step away from the rush and answer what matters first."
        ),
        "Asr" to listOf(
            "Do not let the afternoon carry you past what matters.",
            "A small return to prayer can steady the rest of the day.",
            "This is a good moment to gather yourself again."
        ),
        "Maghrib" to listOf(
            "Let the evening begin with gratitude.",
            "The day has ended; prayer is a gentle way to enter the night.",
            "A calm evening starts with answering the call."
        ),
        "Isha" to listOf(
            "Leave the weight of the day on the prayer mat.",
            "A peaceful night begins with a peaceful prayer.",
            "End the day with nearness, not noise."
        )
    )

    private val prePrayerReflections = listOf(
        "A few minutes now will make the prayer easier to answer.",
        "Get ready early and arrive with a quieter heart.",
        "Take this as a gentle nudge from someone who wants good for you."
    )

    private val extraTimingReflections = mapOf(
        "IMSAK" to "A quiet boundary before Fajr begins.",
        "SUNRISE" to "The morning has opened fully now.",
        "SUNSET" to "The day is folding into evening.",
        "FIRST_THIRD" to "The night has entered its first deep stretch.",
        "MIDNIGHT" to "The middle of the night has arrived.",
        "LAST_THIRD" to "The last third of the night is here."
    )

    fun build(
        context: Context,
        settings: UserSettings,
        prayerName: String,
        type: PrayerAlarmType,
        triggerAtMillis: Long,
        prePrayerMinutes: Int,
    ): PrayerNotificationContent {
        val locationLabel = if (LocationUtil.isGenericLabel(settings.locationLabel)) {
            LocationUtil.resolveLocationName(context, settings.locationLat.toDouble(), settings.locationLng.toDouble()) ?: "your area"
        } else {
            settings.locationLabel
        }
        val prayerUtilityLine = "$prayerName at ${formatTime(triggerAtMillis)} in $locationLabel"
        val seed = "$prayerName|${type.name}|${dayStamp(triggerAtMillis)}".hashCode()
        val isExtraTiming = prayerTitles[prayerName] == null && prePrayerTitles[prayerName] == null

        return if (type == PrayerAlarmType.PRE_PRAYER) {
            val title = pick(prePrayerTitles[prayerName].orEmpty(), seed)
                ?: "$prayerName soon. Let's get ready."
            val utilityLine = "$prayerName in $prePrayerMinutes minutes${if (locationLabel.isNotBlank()) " · $locationLabel" else ""}"
            val reflection = pick(prePrayerReflections, seed + prePrayerMinutes)
                ?: "A few minutes left. Let us get ready for prayer."
            PrayerNotificationContent(
                title = title,
                utilityLine = utilityLine,
                expandedText = "$utilityLine\n\n$reflection"
            )
        } else if (isExtraTiming) {
            val timingLabel = extraPrayerTimingLabel(prayerName)
            val utilityLine = "$timingLabel at ${formatTime(triggerAtMillis)} in $locationLabel"
            val reflection = extraTimingReflections[prayerName] ?: "A notable point in the day has arrived."
            PrayerNotificationContent(
                title = "$timingLabel time",
                utilityLine = utilityLine,
                expandedText = "$utilityLine\n\n$reflection"
            )
        } else {
            val title = pick(prayerTitles[prayerName].orEmpty(), seed)
                ?: "$prayerName time. Let's pray."
            val reflection = pick(reflections[prayerName].orEmpty(), seed + 17)
                ?: "Come, let us answer the call to prayer."
            PrayerNotificationContent(
                title = title,
                utilityLine = prayerUtilityLine,
                expandedText = "$prayerUtilityLine\n\n$reflection"
            )
        }
    }

    private fun formatTime(triggerAtMillis: Long): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(triggerAtMillis))
    }


    private fun pick(options: List<String>, seed: Int): String? {
        if (options.isEmpty()) return null
        return options[abs(seed) % options.size]
    }

    private fun dayStamp(triggerAtMillis: Long): String {
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(triggerAtMillis))
    }
}
