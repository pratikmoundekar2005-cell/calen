package com.personal.calen.domain

import com.personal.calen.model.Event
import com.personal.calen.model.RecurrenceRule
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

object RecurrenceCalculator {

    /**
     * Determines whether [event] occurs on [targetDate].
     */
    fun occursOnDate(event: Event, targetDate: LocalDate): Boolean {
        if (targetDate.isBefore(event.startDate)) return false
        if (event.recurrenceEndDate != null && targetDate.isAfter(event.recurrenceEndDate)) return false

        return when (event.recurrenceRule) {
            RecurrenceRule.NONE -> {
                !targetDate.isBefore(event.startDate) && !targetDate.isAfter(event.endDate)
            }
            RecurrenceRule.DAILY -> true
            RecurrenceRule.WEEKLY -> {
                targetDate.dayOfWeek == event.startDate.dayOfWeek
            }
            RecurrenceRule.MONTHLY -> {
                val startDay = event.startDate.dayOfMonth
                val maxDayInTargetMonth = targetDate.lengthOfMonth()
                if (startDay > maxDayInTargetMonth) {
                    targetDate.dayOfMonth == maxDayInTargetMonth
                } else {
                    targetDate.dayOfMonth == startDay
                }
            }
            RecurrenceRule.YEARLY -> {
                if (targetDate.month != event.startDate.month) return false
                val startDay = event.startDate.dayOfMonth
                val maxDayInTargetMonth = targetDate.lengthOfMonth()
                if (startDay > maxDayInTargetMonth) {
                    targetDate.dayOfMonth == maxDayInTargetMonth
                } else {
                    targetDate.dayOfMonth == startDay
                }
            }
        }
    }

    /**
     * Filters and returns all events that occur on [date].
     * Sorts all-day events first, followed chronologically by startTime.
     */
    fun getEventsForDate(events: List<Event>, date: LocalDate): List<Event> {
        return events
            .filter { occursOnDate(it, date) }
            .sortedWith(
                compareBy<Event> { !it.isAllDay }
                    .thenBy { it.startTime }
                    .thenBy { it.title }
            )
    }

    /**
     * Finds all dates in [yearMonth] (including adjacent grid days if provided) that have at least one event.
     */
    fun getDatesWithEventsForMonth(events: List<Event>, yearMonth: YearMonth): Set<LocalDate> {
        if (events.isEmpty()) return emptySet()

        val datesWithEvents = mutableSetOf<LocalDate>()
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()

        var current = start
        while (!current.isAfter(end)) {
            if (events.any { occursOnDate(it, current) }) {
                datesWithEvents.add(current)
            }
            current = current.plusDays(1)
        }

        return datesWithEvents
    }

    /**
     * Finds all dates in a list of [dates] that have at least one event.
     */
    fun getDatesWithEvents(events: List<Event>, dates: Collection<LocalDate>): Set<LocalDate> {
        if (events.isEmpty() || dates.isEmpty()) return emptySet()
        return dates.filter { date -> events.any { occursOnDate(it, date) } }.toSet()
    }
}
