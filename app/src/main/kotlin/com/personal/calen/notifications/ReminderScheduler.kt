package com.personal.calen.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.personal.calen.domain.RecurrenceCalculator
import com.personal.calen.model.Event
import com.personal.calen.model.RecurrenceRule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ReminderScheduler {

    const val ACTION_EVENT_REMINDER = "com.personal.calen.action.EVENT_REMINDER"
    const val EXTRA_EVENT_ID = "extra_event_id"

    /**
     * Schedules the next reminder alarm for the given [event].
     * If the event has no reminder or all occurrences are in the past, cancels any pending alarm.
     */
    fun scheduleReminder(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = getPendingIntent(context, event.id)

        val reminderOffset = event.reminderOffsetMinutes
        if (reminderOffset == null) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val nextTriggerEpochMs = calculateNextReminderEpochMs(event)
        if (nextTriggerEpochMs == null || nextTriggerEpochMs <= System.currentTimeMillis()) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerEpochMs,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerEpochMs,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback to inexact if permission denied
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerEpochMs,
                pendingIntent
            )
        }
    }

    /**
     * Cancels any scheduled reminder alarm for the event with [eventId].
     */
    fun cancelReminder(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = getPendingIntent(context, eventId)
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Calculates the next future reminder trigger time in epoch milliseconds.
     * Evaluates recurring events to find the earliest future occurrence.
     */
    fun calculateNextReminderEpochMs(event: Event, now: LocalDateTime = LocalDateTime.now()): Long? {
        val offsetMinutes = event.reminderOffsetMinutes ?: return null
        val eventTime = if (event.isAllDay) LocalTime.of(9, 0) else (event.startTime ?: LocalTime.of(9, 0))
        val zone = ZoneId.systemDefault()

        if (event.recurrenceRule == RecurrenceRule.NONE) {
            val eventDateTime = LocalDateTime.of(event.startDate, eventTime)
            val reminderDateTime = eventDateTime.minusMinutes(offsetMinutes.toLong())
            val epochMs = reminderDateTime.atZone(zone).toInstant().toEpochMilli()
            return if (epochMs > System.currentTimeMillis()) epochMs else null
        }

        // For recurring events, check up to 366 days into the future
        var checkDate = now.toLocalDate()
        if (checkDate.isBefore(event.startDate)) {
            checkDate = event.startDate
        }

        val maxDate = checkDate.plusDays(366)
        while (!checkDate.isAfter(maxDate)) {
            if (RecurrenceCalculator.occursOnDate(event, checkDate)) {
                val candidateDateTime = LocalDateTime.of(checkDate, eventTime)
                val reminderDateTime = candidateDateTime.minusMinutes(offsetMinutes.toLong())
                val epochMs = reminderDateTime.atZone(zone).toInstant().toEpochMilli()
                if (epochMs > System.currentTimeMillis()) {
                    return epochMs
                }
            }
            checkDate = checkDate.plusDays(1)
        }

        return null
    }

    private fun getPendingIntent(context: Context, eventId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        return PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
