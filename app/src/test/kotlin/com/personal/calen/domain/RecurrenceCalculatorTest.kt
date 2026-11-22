package com.personal.calen.domain

import com.personal.calen.model.Event
import com.personal.calen.model.RecurrenceRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class RecurrenceCalculatorTest {

    @Test
    fun testNonRepeatingEvent() {
        val event = Event(
            id = 1,
            title = "Doctor Appointment",
            startDate = LocalDate.of(2026, 11, 21),
            endDate = LocalDate.of(2026, 11, 21),
            recurrenceRule = RecurrenceRule.NONE
        )

        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 21)))
        assertFalse(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 20)))
        assertFalse(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 22)))
    }

    @Test
    fun testMultiDayNonRepeatingEvent() {
        val event = Event(
            id = 2,
            title = "Conference",
            startDate = LocalDate.of(2026, 11, 20),
            endDate = LocalDate.of(2026, 11, 23),
            recurrenceRule = RecurrenceRule.NONE
        )

        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 20)))
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 21)))
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 22)))
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 23)))
        assertFalse(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 24)))
    }

    @Test
    fun testDailyRecurrence() {
        val event = Event(
            id = 3,
            title = "Morning Run",
            startDate = LocalDate.of(2026, 11, 1),
            recurrenceRule = RecurrenceRule.DAILY,
            recurrenceEndDate = LocalDate.of(2026, 11, 15)
        )

        assertFalse(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 10, 31)))
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 1)))
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 10)))
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 15)))
        assertFalse(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 16)))
    }

    @Test
    fun testWeeklyRecurrence() {
        // Nov 4, 2026 is a Wednesday
        val event = Event(
            id = 4,
            title = "Weekly Review",
            startDate = LocalDate.of(2026, 11, 4),
            recurrenceRule = RecurrenceRule.WEEKLY
        )

        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 4)))
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 11)))
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 18)))
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 25)))
        assertFalse(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 11, 5))) // Thursday
    }

    @Test
    fun testMonthlyRecurrence_StandardAndMonthEndClamping() {
        // Started on Jan 31
        val event = Event(
            id = 5,
            title = "Month-end Report",
            startDate = LocalDate.of(2026, 1, 31),
            recurrenceRule = RecurrenceRule.MONTHLY
        )

        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 1, 31)))
        // 2026 is non-leap year, Feb ends on 28th
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 2, 28)))
        assertFalse(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 2, 27)))
        // March has 31 days
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 3, 31)))
        // April has 30 days
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2026, 4, 30)))
    }

    @Test
    fun testYearlyRecurrence_LeapDay() {
        val event = Event(
            id = 6,
            title = "Leap Day Celebration",
            startDate = LocalDate.of(2024, 2, 29),
            recurrenceRule = RecurrenceRule.YEARLY
        )

        // Leap year 2024
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2024, 2, 29)))
        // Non-leap year 2025 -> Feb 28
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2025, 2, 28)))
        // Leap year 2028 -> Feb 29
        assertTrue(RecurrenceCalculator.occursOnDate(event, LocalDate.of(2028, 2, 29)))
    }

    @Test
    fun testEventSorting_AllDayFirstThenChronological() {
        val date = LocalDate.of(2026, 11, 21)
        val allDay = Event(
            id = 1,
            title = "Full Day Off",
            isAllDay = true,
            startDate = date,
            startTime = null
        )
        val laterEvent = Event(
            id = 2,
            title = "Dinner",
            isAllDay = false,
            startDate = date,
            startTime = LocalTime.of(19, 0)
        )
        val earlierEvent = Event(
            id = 3,
            title = "Standup",
            isAllDay = false,
            startDate = date,
            startTime = LocalTime.of(9, 30)
        )

        val sorted = RecurrenceCalculator.getEventsForDate(listOf(laterEvent, allDay, earlierEvent), date)
        assertEquals(3, sorted.size)
        assertEquals("Full Day Off", sorted[0].title)
        assertEquals("Standup", sorted[1].title)
        assertEquals("Dinner", sorted[2].title)
    }

    @Test
    fun testGetDatesWithEventsForMonth() {
        val event = Event(
            id = 1,
            title = "Weekend Run",
            startDate = LocalDate.of(2026, 11, 1),
            recurrenceRule = RecurrenceRule.WEEKLY
        )
        val ym = YearMonth.of(2026, 11)
        val dates = RecurrenceCalculator.getDatesWithEventsForMonth(listOf(event), ym)

        // Nov 2026 Sundays: 1, 8, 15, 22, 29
        assertEquals(5, dates.size)
        assertTrue(dates.contains(LocalDate.of(2026, 11, 1)))
        assertTrue(dates.contains(LocalDate.of(2026, 11, 29)))
    }
}
