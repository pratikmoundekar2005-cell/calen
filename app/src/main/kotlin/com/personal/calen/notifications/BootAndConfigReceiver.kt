package com.personal.calen.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.personal.calen.data.CalenDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootAndConfigReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = CalenDatabase.getDatabase(context)
                        val entities = db.eventDao().getEventsWithReminders()
                        for (entity in entities) {
                            ReminderScheduler.scheduleReminder(context, entity.toDomain())
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
