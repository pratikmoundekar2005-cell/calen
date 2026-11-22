package com.personal.calen.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.personal.calen.data.CalenDatabase
import com.personal.calen.data.DataStoreSettingsRepository
import com.personal.calen.data.EventRepository
import com.personal.calen.data.RoomEventRepository
import com.personal.calen.data.SettingsRepository
import com.personal.calen.domain.MonthGridCalculator
import com.personal.calen.domain.RecurrenceCalculator
import com.personal.calen.model.CalendarDay
import com.personal.calen.model.Event
import com.personal.calen.model.ThemeMode
import com.personal.calen.model.WeekStartOption
import com.personal.calen.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

class CalendarViewModel @JvmOverloads constructor(
    application: Application,
    private val eventRepository: EventRepository = RoomEventRepository(
        CalenDatabase.getDatabase(application).eventDao()
    ),
    private val settingsRepository: SettingsRepository = DataStoreSettingsRepository(application)
) : AndroidViewModel(application) {

    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    val displayedMonth: StateFlow<YearMonth> = _displayedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val allEvents: StateFlow<List<Event>> = eventRepository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val weekStart: StateFlow<WeekStartOption> = settingsRepository.weekStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeekStartOption.SYSTEM)

    val defaultReminderMinutes: StateFlow<Int?> = settingsRepository.defaultReminderMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthGrid: StateFlow<List<CalendarDay>> = combine(
        _displayedMonth,
        _selectedDate,
        allEvents,
        weekStart
    ) { month, selected, events, weekStartOption ->
        val firstDayOfWeek = resolveFirstDayOfWeek(weekStartOption)
        val datesWithEvents = RecurrenceCalculator.getDatesWithEventsForMonth(events, month)

        MonthGridCalculator.calculateMonthGrid(
            yearMonth = month,
            selectedDate = selected,
            today = LocalDate.now(),
            firstDayOfWeek = firstDayOfWeek,
            datesWithEvents = datesWithEvents
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDateEvents: StateFlow<List<Event>> = combine(
        _selectedDate,
        allEvents
    ) { selected, events ->
        RecurrenceCalculator.getEventsForDate(events, selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        // If the selected date is in an adjacent month, transition the displayed month
        if (date.year != _displayedMonth.value.year || date.month != _displayedMonth.value.month) {
            _displayedMonth.value = YearMonth.of(date.year, date.month)
        }
    }

    fun nextMonth() {
        val next = _displayedMonth.value.plusMonths(1)
        _displayedMonth.value = next
        // Update selected date to 1st of new month if current selection is outside
        if (_selectedDate.value.year != next.year || _selectedDate.value.month != next.month) {
            _selectedDate.value = next.atDay(1)
        }
    }

    fun previousMonth() {
        val prev = _displayedMonth.value.minusMonths(1)
        _displayedMonth.value = prev
        if (_selectedDate.value.year != prev.year || _selectedDate.value.month != prev.month) {
            _selectedDate.value = prev.atDay(1)
        }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _displayedMonth.value = YearMonth.of(today.year, today.month)
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
            ReminderScheduler.cancelReminder(getApplication(), event.id)
        }
    }

    fun resolveFirstDayOfWeek(option: WeekStartOption): DayOfWeek {
        return when (option) {
            WeekStartOption.SUNDAY -> DayOfWeek.SUNDAY
            WeekStartOption.MONDAY -> DayOfWeek.MONDAY
            WeekStartOption.SYSTEM -> WeekFields.of(Locale.getDefault()).firstDayOfWeek
        }
    }
}
