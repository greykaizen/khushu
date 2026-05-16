package com.kaizen.khushu.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kaizen.khushu.data.repository.SettingsRepository
import com.kaizen.khushu.notifications.PrayerAlarmType
import com.kaizen.khushu.notifications.PrayerNotificationPublisher
import com.kaizen.khushu.notifications.PrayerNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE_PRAYER_NOTIFICATION) return

        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val type = runCatching {
            PrayerAlarmType.valueOf(intent.getStringExtra(EXTRA_NOTIFICATION_TYPE).orEmpty())
        }.getOrDefault(PrayerAlarmType.PRAYER)
        val prePrayerMinutes = intent.getIntExtra(EXTRA_PRE_PRAYER_MINUTES, 10)
        val triggerAtMillis = intent.getLongExtra(EXTRA_TRIGGER_AT_MILLIS, System.currentTimeMillis())

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settings = SettingsRepository(context.applicationContext).settingsFlow.first()
                PrayerNotificationScheduler.createNotificationChannels(
                    context,
                    settings.prayerNotificationCustomSoundUri
                )
                PrayerNotificationPublisher.publish(
                    context = context,
                    settings = settings,
                    prayerName = prayerName,
                    type = type,
                    triggerAtMillis = triggerAtMillis,
                    prePrayerMinutes = prePrayerMinutes,
                    alertStyle = settings.prayerNotificationAlertStyle
                )
                SettingsRepository(context.applicationContext).updateLastDeliveredPrayerNotificationEventId(
                    PrayerNotificationScheduler.eventIdFor(
                        prayerName = prayerName,
                        type = type,
                        triggerAtMillis = triggerAtMillis
                    )
                )
                PrayerNotificationScheduler(context).syncNotifications()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_FIRE_PRAYER_NOTIFICATION = "com.kaizen.khushu.action.FIRE_PRAYER_NOTIFICATION"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
        const val EXTRA_PRE_PRAYER_MINUTES = "extra_pre_prayer_minutes"
        const val EXTRA_TRIGGER_AT_MILLIS = "extra_trigger_at_millis"
    }
}
