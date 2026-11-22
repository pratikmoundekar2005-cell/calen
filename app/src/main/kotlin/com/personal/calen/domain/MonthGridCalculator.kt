package com.personal.calen.domain

import com.personal.calen.model.CalendarDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

object MonthGridCalculator {

    /**
     * Computes the 7-column month grid days for the given [yearMonth].
     * Includes padding days from the previous and next months to complete all visible weeks.
     */
    fun calculateMonthGrid(
        yearMonth: YearMonth,
        selectedDate: LocalDate,
        today: LocalDate = LocalDate.now(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
        datesWithEvents: Set<LocalDate> = emptySet()
    ): List<CalendarDay> {
        val firstOfMonth = yearMonth.atDay(1)
        val lastOfMonth = yearMonth.atEndOfMonth()

        // Find the start date of the first week row
        val startOfGrid = if (firstOfMonth.dayOfWeek == firstDayOfWeek) {
            firstOfMonth
        } else {
            firstOfMonth.with(TemporalAdjusters.previous(firstDayOfWeek))
        }

        // Find the end date of the last week row
        val lastDayOfWeek = firstDayOfWeek.minus(1)
        val endOfGrid = if (lastOfMonth.dayOfWeek == lastDayOfWeek) {
            lastOfMonth
        } else {
            lastOfMonth.with(TemporalAdjusters.next(lastDayOfWeek))
        }

        val days = mutableListOf<CalendarDay>()
        var current = startOfGrid
        while (!current.isAfter(endOfGrid)) {
            val isCurrentMonth = current.year == yearMonth.year && current.month == yearMonth.month
            days.add(
                CalendarDay(
                    date = current,
                    isCurrentMonth = isCurrentMonth,
                    isToday = current == today,
                    isSelected = current == selectedDate,
                    hasEvents = datesWithEvents.contains(current)
                )
            )
            current = current.plusDays(1)
        }

        return days
    }

    /**
     * Returns the ordered 7 days of the week starting from [firstDayOfWeek].
     */
    fun getOrderedWeekDays(firstDayOfWeek: DayOfWeek): List<DayOfWeek> {
        return (0..6).map { firstDayOfWeek.plus(it.toLong()) }
    }
}
