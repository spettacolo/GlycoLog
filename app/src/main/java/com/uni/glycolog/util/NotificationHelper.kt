package com.uni.glycolog.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uni.glycolog.MainActivity
import com.uni.glycolog.R

object NotificationHelper {

    const val CHANNEL_ALERTS = "glycolog_alerts"
    const val CHANNEL_REMINDERS = "glycolog_reminders"
    private const val NOTIFICATION_ID_ALERT = 1
    private const val NOTIFICATION_ID_REMINDER = 2

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val alerts = NotificationChannel(
                CHANNEL_ALERTS,
                context.getString(R.string.channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            )
            val reminders = NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(alerts)
            manager.createNotificationChannel(reminders)
        }
    }

    fun showOutOfRangeAlert(context: Context, value: Int) {
        val titleRes = if (value < GlucoseStats.RANGE_MIN) {
            R.string.alert_low_title
        } else {
            R.string.alert_high_title
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(titleRes))
            .setContentText(context.getString(R.string.alert_value_text, value))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        notify(context, NOTIFICATION_ID_ALERT, notification)
    }

    fun showReminder(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_text))
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        notify(context, NOTIFICATION_ID_REMINDER, notification)
    }

    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

    private fun notify(context: Context, id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
