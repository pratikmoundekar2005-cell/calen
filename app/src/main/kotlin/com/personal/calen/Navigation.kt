package com.personal.calen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.calen.model.Event
import com.personal.calen.model.ThemeMode
import com.personal.calen.ui.calendar.CalendarScreen
import com.personal.calen.ui.calendar.CalendarViewModel
import com.personal.calen.ui.event.EventDetailsDialog
import com.personal.calen.ui.event.EventEditScreen
import com.personal.calen.ui.event.EventEditViewModel
import com.personal.calen.ui.settings.SettingsScreen
import com.personal.calen.ui.settings.SettingsViewModel
import com.personal.calen.ui.theme.CalenTheme
import java.time.LocalDate

sealed interface Screen {
    data object Calendar : Screen
    data object Settings : Screen
    data class EventEdit(val eventId: Long? = null, val initialDate: LocalDate = LocalDate.now()) : Screen
}

@Composable
fun CalenApp(
    deepLinkedEventId: Long? = null,
    calendarViewModel: CalendarViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    eventEditViewModel: EventEditViewModel = viewModel()
) {
    val themeMode by calendarViewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Calendar) }
    var selectedEventForDetails by remember { mutableStateOf<Event?>(null) }
    var handledDeepLinkId by remember { mutableStateOf<Long?>(null) }

    val allEvents by calendarViewModel.allEvents.collectAsState()

    // Handle deep linked notification event tap
    LaunchedEffect(deepLinkedEventId, allEvents) {
        if (deepLinkedEventId != null && deepLinkedEventId != -1L && deepLinkedEventId != handledDeepLinkId) {
            val event = allEvents.firstOrNull { it.id == deepLinkedEventId }
            if (event != null) {
                handledDeepLinkId = deepLinkedEventId
                calendarViewModel.selectDate(event.startDate)
                selectedEventForDetails = event
                currentScreen = Screen.Calendar
            }
        }
    }

    CalenTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CalenTheme.colors.background
        ) {
            when (val screen = currentScreen) {
                is Screen.Calendar -> {
                    CalendarScreen(
                        viewModel = calendarViewModel,
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onNavigateToCreateEvent = { date ->
                            eventEditViewModel.initForCreate(date)
                            currentScreen = Screen.EventEdit(eventId = null, initialDate = date)
                        },
                        onEventClick = { event ->
                            selectedEventForDetails = event
                        }
                    )
                }
                is Screen.Settings -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = { currentScreen = Screen.Calendar }
                    )
                }
                is Screen.EventEdit -> {
                    EventEditScreen(
                        viewModel = eventEditViewModel,
                        isEditMode = screen.eventId != null,
                        onNavigateBack = { currentScreen = Screen.Calendar }
                    )
                }
            }

            // Event Details modal dialog
            selectedEventForDetails?.let { event ->
                EventDetailsDialog(
                    event = event,
                    onEditClick = { ev ->
                        selectedEventForDetails = null
                        eventEditViewModel.initForEdit(ev.id)
                        currentScreen = Screen.EventEdit(eventId = ev.id, initialDate = ev.startDate)
                    },
                    onDeleteClick = { ev ->
                        calendarViewModel.deleteEvent(ev)
                        selectedEventForDetails = null
                    },
                    onDismiss = {
                        selectedEventForDetails = null
                    }
                )
            }
        }
    }
}
