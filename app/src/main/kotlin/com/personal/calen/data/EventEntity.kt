package com.personal.calen.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.personal.calen.model.Event
import com.personal.calen.model.RecurrenceRule
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
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
) {
    fun toDomain(): Event = Event(
        id = id,
        title = title,
        description = description,
        isAllDay = isAllDay,
        startDate = startDate,
        startTime = startTime,
        endDate = endDate,
        endTime = endTime,
        reminderOffsetMinutes = reminderOffsetMinutes,
        recurrenceRule = recurrenceRule,
        recurrenceEndDate = recurrenceEndDate,
        createdAtEpochMs = createdAtEpochMs
    )
}

fun Event.toEntity(): EventEntity = EventEntity(
    id = id,
    title = title,
    description = description,
    isAllDay = isAllDay,
    startDate = startDate,
    startTime = startTime,
    endDate = endDate,
    endTime = endTime,
    reminderOffsetMinutes = reminderOffsetMinutes,
    recurrenceRule = recurrenceRule,
    recurrenceEndDate = recurrenceEndDate,
    createdAtEpochMs = createdAtEpochMs
)
