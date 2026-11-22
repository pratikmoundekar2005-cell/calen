package com.personal.calen.model

enum class ReminderOption(val minutesBefore: Int?, val label: String) {
    AT_EVENT_TIME(0, "At event time"),
    MIN_5_BEFORE(5, "5 minutes before"),
    MIN_10_BEFORE(10, "10 minutes before"),
    MIN_15_BEFORE(15, "15 minutes before"),
    MIN_30_BEFORE(30, "30 minutes before"),
    HOUR_1_BEFORE(60, "1 hour before"),
    DAY_1_BEFORE(1440, "1 day before"),
    NO_REMINDER(null, "No reminder");

    companion object {
        fun fromMinutes(minutes: Int?): ReminderOption {
            if (minutes == null) return NO_REMINDER
            return entries.firstOrNull { it.minutesBefore == minutes } ?: AT_EVENT_TIME
        }
    }
}
