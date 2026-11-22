package com.personal.calen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.personal.calen.notifications.ReminderNotificationHelper

class MainActivity : ComponentActivity() {

    private var deepLinkedEventId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create the notification channel on app launch
        ReminderNotificationHelper.createNotificationChannel(this)

        handleIntent(intent)

        setContent {
            CalenApp(deepLinkedEventId = deepLinkedEventId)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val eventId = intent.getLongExtra(ReminderNotificationHelper.EXTRA_EVENT_ID, -1L)
        if (eventId != -1L) {
            deepLinkedEventId = eventId
        }
    }
}
