package com.personal.calen.model

import java.time.LocalDate
import java.time.LocalTime

data class Event(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val isAllDay: Boolean = false,
    val startDate: LocalDate,
    val startTime: LocalTime? = null,
    val endDate: LocalDate = startDate,
    val endTime: LocalTime? = null,
    val reminderOffsetMinutes: Int? = 0, // 0 = At event time, null = No reminder
    val recurrenceRule: RecurrenceRule = RecurrenceRule.NONE,
    val recurrenceEndDate: LocalDate? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
