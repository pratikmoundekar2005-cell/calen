package com.personal.calen.notifications

import com.personal.calen.model.Event
import com.personal.calen.model.RecurrenceRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderSchedulerTest {

    @Test
    fun testCalculateNextReminder_NoReminder_ReturnsNull() {
        val event = Event(
            id = 1,
            title = "Quiet Event",
            startDate = LocalDate.now().plusDays(2),
            reminderOffsetMinutes = null
        )
        val trigger = ReminderScheduler.calculateNextReminderEpochMs(event)
        assertNull(trigger)
    }

    @Test
    fun testCalculateNextReminder_FutureTimedEvent_AtEventTime() {
        val futureDate = LocalDate.now().plusDays(2)
        val time = LocalTime.of(14, 0)
        val event = Event(
            id = 2,
            title = "Dentist",
            startDate = futureDate,
            startTime = time,
            reminderOffsetMinutes = 0
        )

        val trigger = ReminderScheduler.calculateNextReminderEpochMs(event)
        assertNotNull(trigger)

        val expected = LocalDateTime.of(futureDate, time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, trigger)
    }

    @Test
    fun testCalculateNextReminder_FutureTimedEvent_15MinBefore() {
        val futureDate = LocalDate.now().plusDays(3)
        val time = LocalTime.of(10, 30)
        val event = Event(
            id = 3,
            title = "Client Call",
            startDate = futureDate,
            startTime = time,
            reminderOffsetMinutes = 15
        )

        val trigger = ReminderScheduler.calculateNextReminderEpochMs(event)
        assertNotNull(trigger)

        val expected = LocalDateTime.of(futureDate, time)
            .minusMinutes(15)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, trigger)
    }

    @Test
    fun testCalculateNextReminder_AllDayEvent_DefaultsTo9AM() {
        val futureDate = LocalDate.now().plusDays(4)
        val event = Event(
            id = 4,
            title = "Holiday",
            isAllDay = true,
            startDate = futureDate,
            reminderOffsetMinutes = 0 // At event time for all-day -> 9:00 AM on the day
        )

        val trigger = ReminderScheduler.calculateNextReminderEpochMs(event)
        assertNotNull(trigger)

        val expected = LocalDateTime.of(futureDate, LocalTime.of(9, 0))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, trigger)
    }

    @Test
    fun testCalculateNextReminder_AllDayEvent_1DayBefore() {
        val futureDate = LocalDate.now().plusDays(5)
        val event = Event(
            id = 5,
            title = "Tax Filing Deadline",
            isAllDay = true,
            startDate = futureDate,
            reminderOffsetMinutes = 1440 // 1 day before -> 9:00 AM the day before
        )

        val trigger = ReminderScheduler.calculateNextReminderEpochMs(event)
        assertNotNull(trigger)

        val expected = LocalDateTime.of(futureDate, LocalTime.of(9, 0))
            .minusMinutes(1440)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, trigger)
    }

    @Test
    fun testCalculateNextReminder_PastNonRecurringEvent_ReturnsNull() {
        val pastDate = LocalDate.now().minusDays(5)
        val event = Event(
            id = 6,
            title = "Past Event",
            startDate = pastDate,
            startTime = LocalTime.of(10, 0),
            reminderOffsetMinutes = 0
        )

        val trigger = ReminderScheduler.calculateNextReminderEpochMs(event)
        assertNull(trigger)
    }

    @Test
    fun testCalculateNextReminder_PastStartRecurringEvent_FindsNextFutureOccurrence() {
        // Event started 3 weeks ago, repeats weekly on the same day-of-week at 15:00
        val today = LocalDate.now()
        val pastStart = today.minusWeeks(3)
        val event = Event(
            id = 7,
            title = "Weekly Team Sync",
            startDate = pastStart,
            startTime = LocalTime.of(15, 0),
            recurrenceRule = RecurrenceRule.WEEKLY,
            reminderOffsetMinutes = 30
        )

        val trigger = ReminderScheduler.calculateNextReminderEpochMs(event)
        assertNotNull(trigger)
        // Must be in the future
        assertTrue(trigger!! > System.currentTimeMillis())
    }
}
