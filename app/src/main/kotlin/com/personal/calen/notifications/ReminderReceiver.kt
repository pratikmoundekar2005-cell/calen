package com.personal.calen.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.personal.calen.data.CalenDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_EVENT_REMINDER) return

        val eventId = intent.getLongExtra(ReminderScheduler.EXTRA_EVENT_ID, -1L)
        if (eventId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = CalenDatabase.getDatabase(context)
                val entity = db.eventDao().getEventById(eventId)
                if (entity != null) {
                    val event = entity.toDomain()
                    ReminderNotificationHelper.showEventNotification(context, event)
                    // If recurring, schedule the next occurrence
                    ReminderScheduler.scheduleReminder(context, event)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
