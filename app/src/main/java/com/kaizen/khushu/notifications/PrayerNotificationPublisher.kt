package com.kaizen.khushu.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kaizen.khushu.MainActivity
import com.kaizen.khushu.R
import com.kaizen.khushu.data.repository.UserSettings

object PrayerNotificationPublisher {

    fun publish(
        context: Context,
        settings: UserSettings,
        prayerName: String,
        type: PrayerAlarmType,
        triggerAtMillis: Long,
        prePrayerMinutes: Int,
        alertStyle: String,
    ) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            "$prayerName-${type.name}".hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val content = PrayerNotificationCopy.build(
            context = context,
            settings = settings,
            prayerName = prayerName,
            type = type,
            triggerAtMillis = triggerAtMillis,
            prePrayerMinutes = prePrayerMinutes
        )

        val builder = NotificationCompat.Builder(
            context,
            PrayerNotificationScheduler.channelIdForStyle(
                style = alertStyle,
                customSoundUriString = settings.prayerNotificationCustomSoundUri
            )
        )
            .setSmallIcon(R.drawable.ic_notification_glyph)
            .setColor(ContextCompat.getColor(context, R.color.splash_background))
            .setContentTitle(content.title)
            .setContentText(content.utilityLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.expandedText))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(
                if (alertStyle == "SILENT") {
                    NotificationCompat.PRIORITY_LOW
                } else {
                    NotificationCompat.PRIORITY_HIGH
                }
            )
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            when (alertStyle) {
                "CUSTOM_SOUND", "SYSTEM_SOUND" -> {
                    builder.setSound(
                        PrayerNotificationScheduler.defaultSoundUriForStyle(
                            style = alertStyle,
                            customSoundUriString = settings.prayerNotificationCustomSoundUri
                        )
                    )
                    builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                    builder.setVibrate(longArrayOf(0L, 180L, 120L, 180L))
                }
                "VIBRATION" -> {
                    builder.setVibrate(longArrayOf(0L, 220L, 140L, 220L))
                }
                "SILENT" -> builder.setSilent(true)
            }
        } else if (alertStyle == "SILENT") {
            builder.setSilent(true)
        }

        notificationManager.notify("$prayerName-${type.name}".hashCode(), builder.build())
    }
}
