package com.kaizen.khushu.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import com.kaizen.khushu.data.repository.PrayerTimeRepository
import com.kaizen.khushu.data.repository.SettingsRepository
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.data.repository.EXTRA_PRAYER_TIMINGS
import com.kaizen.khushu.receiver.PrayerAlarmReceiver
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date

data class PrayerNotificationScheduleConfig(
    val prayerCalculationMethod: String,
    val prayerMadhab: String,
    val locationLat: Float,
    val locationLng: Float,
    val useGpsLocation: Boolean,
    val prayerSourceType: String,
    val fajrOffsetMinutes: Int,
    val dhuhrOffsetMinutes: Int,
    val asrOffsetMinutes: Int,
    val maghribOffsetMinutes: Int,
    val ishaOffsetMinutes: Int,
    val fajrPrayerNotificationEnabled: Boolean,
    val dhuhrPrayerNotificationEnabled: Boolean,
    val asrPrayerNotificationEnabled: Boolean,
    val maghribPrayerNotificationEnabled: Boolean,
    val ishaPrayerNotificationEnabled: Boolean,
    val fajrPrePrayerNotificationEnabled: Boolean,
    val dhuhrPrePrayerNotificationEnabled: Boolean,
    val asrPrePrayerNotificationEnabled: Boolean,
    val maghribPrePrayerNotificationEnabled: Boolean,
    val ishaPrePrayerNotificationEnabled: Boolean,
    val fajrPrePrayerMinutes: Int,
    val dhuhrPrePrayerMinutes: Int,
    val asrPrePrayerMinutes: Int,
    val maghribPrePrayerMinutes: Int,
    val ishaPrePrayerMinutes: Int,
    val prayerNotificationAlertStyle: String,
    val extraPrayerNotifications: Set<String>,
)

fun UserSettings.toPrayerNotificationScheduleConfig(): PrayerNotificationScheduleConfig {
    return PrayerNotificationScheduleConfig(
        prayerCalculationMethod = prayerCalculationMethod,
        prayerMadhab = prayerMadhab,
        locationLat = locationLat,
        locationLng = locationLng,
        useGpsLocation = useGpsLocation,
        prayerSourceType = prayerSourceType,
        fajrOffsetMinutes = fajrOffsetMinutes,
        dhuhrOffsetMinutes = dhuhrOffsetMinutes,
        asrOffsetMinutes = asrOffsetMinutes,
        maghribOffsetMinutes = maghribOffsetMinutes,
        ishaOffsetMinutes = ishaOffsetMinutes,
        fajrPrayerNotificationEnabled = fajrPrayerNotificationEnabled,
        dhuhrPrayerNotificationEnabled = dhuhrPrayerNotificationEnabled,
        asrPrayerNotificationEnabled = asrPrayerNotificationEnabled,
        maghribPrayerNotificationEnabled = maghribPrayerNotificationEnabled,
        ishaPrayerNotificationEnabled = ishaPrayerNotificationEnabled,
        fajrPrePrayerNotificationEnabled = fajrPrePrayerNotificationEnabled,
        dhuhrPrePrayerNotificationEnabled = dhuhrPrePrayerNotificationEnabled,
        asrPrePrayerNotificationEnabled = asrPrePrayerNotificationEnabled,
        maghribPrePrayerNotificationEnabled = maghribPrePrayerNotificationEnabled,
        ishaPrePrayerNotificationEnabled = ishaPrePrayerNotificationEnabled,
        fajrPrePrayerMinutes = fajrPrePrayerMinutes,
        dhuhrPrePrayerMinutes = dhuhrPrePrayerMinutes,
        asrPrePrayerMinutes = asrPrePrayerMinutes,
        maghribPrePrayerMinutes = maghribPrePrayerMinutes,
        ishaPrePrayerMinutes = ishaPrePrayerMinutes,
        prayerNotificationAlertStyle = prayerNotificationAlertStyle,
        extraPrayerNotifications = extraPrayerNotifications,
    )
}

class PrayerNotificationScheduler(
    private val context: Context
) {
    private val appContext = context.applicationContext
    private val settingsRepository = SettingsRepository(appContext)
    private val prayerTimeRepository = PrayerTimeRepository(settingsRepository)
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    suspend fun syncNotifications(settings: UserSettings? = null) {
        val currentSettings = settings ?: settingsRepository.settingsFlow.first()
        createNotificationChannels(appContext, currentSettings.prayerNotificationCustomSoundUri)
        cancelAllScheduledNotifications()
        if (!hasAnyNotificationEnabled(currentSettings)) return

        val now = System.currentTimeMillis()
        val datesToSchedule = listOf(
            DateAnchor.Today,
            DateAnchor.Tomorrow
        )

        for (anchor in datesToSchedule) {
            val targetDate = anchor.resolve()
            val prayerTimes = prayerTimeRepository.getEffectivePrayerDateTimes(
                date = targetDate,
                settings = currentSettings
            )
            val extraPrayerTimes = prayerTimeRepository.getExtraPrayerDateTimes(
                date = targetDate,
                settings = currentSettings
            )

            prayerTimes.forEach { (prayerName, prayerDate) ->
                if (isPrayerNotificationEnabled(currentSettings, prayerName) && prayerDate.time > now) {
                    schedulePrayerAlarm(
                        prayerName = prayerName,
                        triggerAtMillis = prayerDate.time,
                        type = PrayerAlarmType.PRAYER,
                        prePrayerMinutes = 0
                    )
                }

                if (isPrePrayerNotificationEnabled(currentSettings, prayerName)) {
                    val leadMinutes = prePrayerMinutes(currentSettings, prayerName)
                    val prePrayerTime = prayerDate.time - leadMinutes * 60_000L
                    if (prePrayerTime > now) {
                        schedulePrayerAlarm(
                            prayerName = prayerName,
                            triggerAtMillis = prePrayerTime,
                            type = PrayerAlarmType.PRE_PRAYER,
                            prePrayerMinutes = leadMinutes
                        )
                    }
                }
            }

            extraPrayerTimes.forEach { (timingId, timingDate) ->
                if (currentSettings.extraPrayerNotifications.contains(timingId) && timingDate.time > now) {
                    schedulePrayerAlarm(
                        prayerName = timingId,
                        triggerAtMillis = timingDate.time,
                        type = PrayerAlarmType.PRAYER,
                        prePrayerMinutes = 0
                    )
                }
            }
        }
    }

    suspend fun maybeDeliverCurrentPrayerNotification(
        settings: UserSettings? = null,
        nowMillis: Long = System.currentTimeMillis(),
        windowMillis: Long = 75_000L,
    ) {
        val currentSettings = settings ?: settingsRepository.settingsFlow.first()
        if (!hasAnyNotificationEnabled(currentSettings)) return

        val prayerTimes = prayerTimeRepository.getEffectivePrayerDateTimes(
            date = Date(nowMillis),
            settings = currentSettings
        )
        val extraPrayerTimes = prayerTimeRepository.getExtraPrayerDateTimes(
            date = Date(nowMillis),
            settings = currentSettings
        )

        prayerTimes.forEach { (prayerName, prayerDate) ->
            val prayerTimeMillis = prayerDate.time
            if (isPrayerNotificationEnabled(currentSettings, prayerName) &&
                nowMillis >= prayerTimeMillis &&
                nowMillis - prayerTimeMillis <= windowMillis
            ) {
                deliverIfNeeded(
                    prayerName = prayerName,
                    type = PrayerAlarmType.PRAYER,
                    triggerAtMillis = prayerTimeMillis,
                    prePrayerMinutes = 0,
                    settings = currentSettings
                )
            }

            if (isPrePrayerNotificationEnabled(currentSettings, prayerName)) {
                val leadMinutes = prePrayerMinutes(currentSettings, prayerName)
                val prePrayerTimeMillis = prayerTimeMillis - leadMinutes * 60_000L
                if (nowMillis >= prePrayerTimeMillis &&
                    nowMillis - prePrayerTimeMillis <= windowMillis
                ) {
                    deliverIfNeeded(
                        prayerName = prayerName,
                        type = PrayerAlarmType.PRE_PRAYER,
                        triggerAtMillis = prePrayerTimeMillis,
                        prePrayerMinutes = leadMinutes,
                        settings = currentSettings
                    )
                }
            }
        }

        extraPrayerTimes.forEach { (timingId, timingDate) ->
            val timingMillis = timingDate.time
            if (currentSettings.extraPrayerNotifications.contains(timingId) &&
                nowMillis >= timingMillis &&
                nowMillis - timingMillis <= windowMillis
            ) {
                deliverIfNeeded(
                    prayerName = timingId,
                    type = PrayerAlarmType.PRAYER,
                    triggerAtMillis = timingMillis,
                    prePrayerMinutes = 0,
                    settings = currentSettings
                )
            }
        }
    }

    fun cancelAllScheduledNotifications() {
        val dayOffsets = -1..2
        val prayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha") + EXTRA_PRAYER_TIMINGS.map { it.id }
        val alarmTypes = PrayerAlarmType.entries

        for (offset in dayOffsets) {
            val requestDay = dayStamp(offset)
            for (prayerName in prayerNames) {
                for (type in alarmTypes) {
                    val pendingIntent = buildAlarmPendingIntent(
                        prayerName = prayerName,
                        requestDay = requestDay,
                        type = type,
                        prePrayerMinutes = 0,
                        triggerAtMillis = 0L,
                        flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                        pendingIntent.cancel()
                    }
                }
            }
        }
    }

    private fun schedulePrayerAlarm(
        prayerName: String,
        triggerAtMillis: Long,
        type: PrayerAlarmType,
        prePrayerMinutes: Int,
    ) {
        val requestDay = dayStamp(triggerAtMillis)
        val pendingIntent = buildAlarmPendingIntent(
            prayerName = prayerName,
            requestDay = requestDay,
            type = type,
            prePrayerMinutes = prePrayerMinutes,
            triggerAtMillis = triggerAtMillis,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun buildAlarmPendingIntent(
        prayerName: String,
        requestDay: String,
        type: PrayerAlarmType,
        prePrayerMinutes: Int,
        triggerAtMillis: Long,
        flags: Int,
    ): PendingIntent? {
        val intent = Intent(appContext, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_FIRE_PRAYER_NOTIFICATION
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
            putExtra(PrayerAlarmReceiver.EXTRA_NOTIFICATION_TYPE, type.name)
            putExtra(PrayerAlarmReceiver.EXTRA_PRE_PRAYER_MINUTES, prePrayerMinutes)
            putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT_MILLIS, triggerAtMillis)
        }
        return PendingIntent.getBroadcast(
            appContext,
            requestCode(prayerName, requestDay, type),
            intent,
            flags
        )
    }

    private fun hasAnyNotificationEnabled(settings: UserSettings): Boolean {
        return listOf(
            settings.fajrPrayerNotificationEnabled,
            settings.dhuhrPrayerNotificationEnabled,
            settings.asrPrayerNotificationEnabled,
            settings.maghribPrayerNotificationEnabled,
            settings.ishaPrayerNotificationEnabled,
            settings.fajrPrePrayerNotificationEnabled,
            settings.dhuhrPrePrayerNotificationEnabled,
            settings.asrPrePrayerNotificationEnabled,
            settings.maghribPrePrayerNotificationEnabled,
            settings.ishaPrePrayerNotificationEnabled
        ).any { it }
            || settings.extraPrayerNotifications.isNotEmpty()
    }

    private fun isPrayerNotificationEnabled(settings: UserSettings, prayerName: String): Boolean {
        return when (prayerName) {
            "Fajr" -> settings.fajrPrayerNotificationEnabled
            "Dhuhr" -> settings.dhuhrPrayerNotificationEnabled
            "Asr" -> settings.asrPrayerNotificationEnabled
            "Maghrib" -> settings.maghribPrayerNotificationEnabled
            "Isha" -> settings.ishaPrayerNotificationEnabled
            else -> false
        }
    }

    private fun isPrePrayerNotificationEnabled(settings: UserSettings, prayerName: String): Boolean {
        return when (prayerName) {
            "Fajr" -> settings.fajrPrePrayerNotificationEnabled
            "Dhuhr" -> settings.dhuhrPrePrayerNotificationEnabled
            "Asr" -> settings.asrPrePrayerNotificationEnabled
            "Maghrib" -> settings.maghribPrePrayerNotificationEnabled
            "Isha" -> settings.ishaPrePrayerNotificationEnabled
            else -> false
        }
    }

    private fun prePrayerMinutes(settings: UserSettings, prayerName: String): Int {
        return when (prayerName) {
            "Fajr" -> settings.fajrPrePrayerMinutes
            "Dhuhr" -> settings.dhuhrPrePrayerMinutes
            "Asr" -> settings.asrPrePrayerMinutes
            "Maghrib" -> settings.maghribPrePrayerMinutes
            "Isha" -> settings.ishaPrePrayerMinutes
            else -> 10
        }
    }

    private fun requestCode(prayerName: String, requestDay: String, type: PrayerAlarmType): Int {
        return "$prayerName|$requestDay|${type.name}".hashCode()
    }

    private suspend fun deliverIfNeeded(
        prayerName: String,
        type: PrayerAlarmType,
        triggerAtMillis: Long,
        prePrayerMinutes: Int,
        settings: UserSettings,
    ) {
        val eventId = eventIdFor(prayerName, type, triggerAtMillis)
        if (settings.lastDeliveredPrayerNotificationEventId == eventId) return

        PrayerNotificationPublisher.publish(
            context = appContext,
            settings = settings,
            prayerName = prayerName,
            type = type,
            triggerAtMillis = triggerAtMillis,
            prePrayerMinutes = prePrayerMinutes,
            alertStyle = settings.prayerNotificationAlertStyle
        )
        settingsRepository.updateLastDeliveredPrayerNotificationEventId(eventId)
    }

    private fun dayStamp(offset: Int): String {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, offset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dayStamp(calendar.timeInMillis)
    }

    private fun dayStamp(epochMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = epochMillis
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "%04d%02d%02d".format(year, month, day)
    }

    private enum class DateAnchor {
        Today,
        Tomorrow;

        fun resolve(): Date {
            val calendar = Calendar.getInstance().apply {
                if (this@DateAnchor == Tomorrow) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            return calendar.time
        }
    }

    companion object {
        const val CHANNEL_SYSTEM_SOUND = "prayer_reminders_system_sound_v2"
        const val CHANNEL_CUSTOM_SOUND_PREFIX = "prayer_reminders_custom_sound_v3_"
        const val CHANNEL_VIBRATION = "prayer_reminders_vibration_v2"
        const val CHANNEL_SILENT = "prayer_reminders_silent_v2"

        fun createNotificationChannels(
            context: Context,
            customSoundUriString: String = "",
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val customSoundUri = customSoundUriString
                .takeIf { it.isNotBlank() }
                ?.let(Uri::parse)
                ?: defaultCustomSoundUri()
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_SYSTEM_SOUND,
                    "Prayer Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Prayer reminders with system notification sound and heads-up alerts"
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                    enableVibration(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
                NotificationChannel(
                    customSoundChannelId(customSoundUriString),
                    "Prayer Reminders Custom",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Prayer reminders with a stronger alarm-style tone and heads-up alerts"
                    setSound(customSoundUri, audioAttributes)
                    enableVibration(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
                NotificationChannel(
                    CHANNEL_VIBRATION,
                    "Prayer Reminders Vibration",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Prayer reminders with vibration-only heads-up alerts"
                    setSound(null, null)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0L, 220L, 140L, 220L)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
                NotificationChannel(
                    CHANNEL_SILENT,
                    "Prayer Reminders Silent",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Prayer reminders with no sound or vibration"
                    setSound(null, null)
                    enableVibration(false)
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            )
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun channelIdForStyle(
            style: String,
            customSoundUriString: String = "",
        ): String {
            return when (style) {
                "CUSTOM_SOUND" -> customSoundChannelId(customSoundUriString)
                "VIBRATION" -> CHANNEL_VIBRATION
                "SILENT" -> CHANNEL_SILENT
                else -> CHANNEL_SYSTEM_SOUND
            }
        }

        fun defaultSoundUriForStyle(
            style: String,
            customSoundUriString: String = "",
        ): Uri? {
            return when (style) {
                "CUSTOM_SOUND" -> customSoundUriString
                    .takeIf { it.isNotBlank() }
                    ?.let(Uri::parse)
                    ?: defaultCustomSoundUri()
                "SYSTEM_SOUND" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                else -> null
            }
        }

        fun eventIdFor(
            prayerName: String,
            type: PrayerAlarmType,
            triggerAtMillis: Long,
        ): String {
            val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
            return "%04d%02d%02d|%s|%s".format(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                prayerName,
                type.name
            )
        }

        private fun defaultCustomSoundUri(): Uri {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        private fun customSoundChannelId(customSoundUriString: String): String {
            val channelSuffix = customSoundUriString.ifBlank { "default" }.hashCode().toString().replace("-", "n")
            return "$CHANNEL_CUSTOM_SOUND_PREFIX$channelSuffix"
        }
    }
}

enum class PrayerAlarmType {
    PRAYER,
    PRE_PRAYER
}
