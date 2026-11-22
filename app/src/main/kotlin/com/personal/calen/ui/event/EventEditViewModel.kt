package com.personal.calen.ui.event

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.personal.calen.data.CalenDatabase
import com.personal.calen.data.DataStoreSettingsRepository
import com.personal.calen.data.EventRepository
import com.personal.calen.data.RoomEventRepository
import com.personal.calen.data.SettingsRepository
import com.personal.calen.model.Event
import com.personal.calen.model.RecurrenceRule
import com.personal.calen.model.ReminderOption
import com.personal.calen.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class EventEditViewModel(
    application: Application,
    private val eventRepository: EventRepository = RoomEventRepository(
        CalenDatabase.getDatabase(application).eventDao()
    ),
    private val settingsRepository: SettingsRepository = DataStoreSettingsRepository(application)
) : AndroidViewModel(application) {

    private var editingEventId: Long? = null
    private var initialSnapshot: EventSnapshot? = null

    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val isAllDay = MutableStateFlow(false)
    val startDate = MutableStateFlow(LocalDate.now())
    val startTime = MutableStateFlow(LocalTime.of(10, 0))
    val endDate = MutableStateFlow(LocalDate.now())
    val endTime = MutableStateFlow(LocalTime.of(11, 0))
    val reminderOption = MutableStateFlow(ReminderOption.AT_EVENT_TIME)
    val recurrenceRule = MutableStateFlow(RecurrenceRule.NONE)

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isSaveSuccess = MutableStateFlow(false)
    val isSaveSuccess: StateFlow<Boolean> = _isSaveSuccess.asStateFlow()

    data class EventSnapshot(
        val title: String,
        val description: String,
        val isAllDay: Boolean,
        val startDate: LocalDate,
        val startTime: LocalTime,
        val endDate: LocalDate,
        val endTime: LocalTime,
        val reminderOption: ReminderOption,
        val recurrenceRule: RecurrenceRule
    )

    fun initForCreate(initialDate: LocalDate) {
        startDate.value = initialDate
        endDate.value = initialDate

        // Round current time up to nearest 30 mins
        val now = LocalTime.now()
        val minute = now.minute
        val roundedMinute = if (minute < 30) 30 else 0
        val roundedHour = if (minute >= 30) (now.hour + 1) % 24 else now.hour
        val start = LocalTime.of(roundedHour, roundedMinute)
        val end = start.plusHours(1)

        startTime.value = start
        endTime.value = end

        viewModelScope.launch {
            val defaultMinutes = settingsRepository.defaultReminderMinutes.first()
            reminderOption.value = ReminderOption.fromMinutes(defaultMinutes)
            captureInitialSnapshot()
        }
    }

    fun initForEdit(eventId: Long) {
        editingEventId = eventId
        viewModelScope.launch {
            val event = eventRepository.getEventById(eventId) ?: return@launch
            title.value = event.title
            description.value = event.description ?: ""
            isAllDay.value = event.isAllDay
            startDate.value = event.startDate
            endDate.value = event.endDate
            event.startTime?.let { startTime.value = it }
            event.endTime?.let { endTime.value = it }
            reminderOption.value = ReminderOption.fromMinutes(event.reminderOffsetMinutes)
            recurrenceRule.value = event.recurrenceRule
            captureInitialSnapshot()
        }
    }

    private fun captureInitialSnapshot() {
        initialSnapshot = EventSnapshot(
            title = title.value,
            description = description.value,
            isAllDay = isAllDay.value,
            startDate = startDate.value,
            startTime = startTime.value,
            endDate = endDate.value,
            endTime = endTime.value,
            reminderOption = reminderOption.value,
            recurrenceRule = recurrenceRule.value
        )
    }

    val isModified: Boolean
        get() {
            val snap = initialSnapshot ?: return title.value.isNotBlank()
            return title.value != snap.title ||
                    description.value != snap.description ||
                    isAllDay.value != snap.isAllDay ||
                    startDate.value != snap.startDate ||
                    startTime.value != snap.startTime ||
                    endDate.value != snap.endDate ||
                    endTime.value != snap.endTime ||
                    reminderOption.value != snap.reminderOption ||
                    recurrenceRule.value != snap.recurrenceRule
        }

    val isSaveValid: Boolean
        get() {
            if (title.value.isBlank()) return false
            if (!isAllDay.value) {
                // If same day, endTime must be after startTime
                if (startDate.value == endDate.value && !endTime.value.isAfter(startTime.value)) {
                    return false
                }
                if (endDate.value.isBefore(startDate.value)) {
                    return false
                }
            }
            return true
        }

    fun saveEvent() {
        if (!isSaveValid || _isSaving.value) return

        _isSaving.value = true
        viewModelScope.launch {
            val event = Event(
                id = editingEventId ?: 0L,
                title = title.value.trim(),
                description = description.value.trim().ifEmpty { null },
                isAllDay = isAllDay.value,
                startDate = startDate.value,
                startTime = if (isAllDay.value) null else startTime.value,
                endDate = if (isAllDay.value) startDate.value else endDate.value,
                endTime = if (isAllDay.value) null else endTime.value,
                reminderOffsetMinutes = reminderOption.value.minutesBefore,
                recurrenceRule = recurrenceRule.value
            )

            val savedId = if (editingEventId != null) {
                eventRepository.updateEvent(event)
                editingEventId!!
            } else {
                eventRepository.saveEvent(event)
            }

            // Update reminder alarm schedule immediately
            val savedEvent = event.copy(id = savedId)
            ReminderScheduler.scheduleReminder(getApplication(), savedEvent)

            _isSaving.value = false
            _isSaveSuccess.value = true
        }
    }
}
