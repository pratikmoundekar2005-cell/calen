package com.personal.calen.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.personal.calen.MainActivity
import com.personal.calen.R
import com.personal.calen.model.Event
import java.time.format.DateTimeFormatter

object ReminderNotificationHelper {

    const val CHANNEL_ID = "calen_reminders"
    const val CHANNEL_NAME = "Event Reminders"
    const val EXTRA_EVENT_ID = "extra_event_id"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled event reminders"
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showEventNotification(context: Context, event: Event) {
        createNotificationChannel(context)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_EVENT_ID, event.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val contentText = if (event.isAllDay) {
            "All day"
        } else if (event.startTime != null && event.endTime != null) {
            "${event.startTime.format(timeFormatter)} - ${event.endTime.format(timeFormatter)}"
        } else if (event.startTime != null) {
            event.startTime.format(timeFormatter)
        } else {
            ""
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(event.title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                if (!event.description.isNullOrBlank()) {
                    "$contentText\n${event.description}"
                } else {
                    contentText
                }
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(event.id.toInt(), builder.build())
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may have been revoked
        }
    }
}
