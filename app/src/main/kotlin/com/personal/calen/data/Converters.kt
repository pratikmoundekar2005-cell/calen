package com.personal.calen.data

import androidx.room.TypeConverter
import com.personal.calen.model.RecurrenceRule
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromRecurrenceRule(rule: RecurrenceRule?): String? = rule?.name

    @TypeConverter
    fun toRecurrenceRule(value: String?): RecurrenceRule = RecurrenceRule.fromName(value)
}
