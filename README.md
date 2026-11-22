# Calen

A minimalist, quiet, and private local-only Android calendar built with Kotlin and Jetpack Compose.

---

## Philosophy & Core Principles

Calen is designed as a refined, intentional everyday tool for personal scheduling:
- **Local-Only:** Zero network permissions (`android.permission.INTERNET` is not included in the manifest). Your schedule never leaves your device.
- **Pure Monochrome Design:** Engineered exclusively using a strict 4-color palette (`#000000`, `#555555`, `#A2AAAD`, `#FFFFFF`). No gradients, no rainbow tags, no Material You dynamic color shifts.
- **Editorial Typography:** Featuring the bundled **Cairo Regular** typeface for the `Calen` wordmark and clean, readable Android sans-serif for dates, times, and events.
- **Reliable Reminders:** Hardware-timed alarms via `AlarmManager` with exact scheduling and automatic rescheduling across device reboots and timezone shifts.
- **No Clutter:** No accounts, no sign-ins, no ads, no subscriptions, no cloud sync, and no analytics.

---

## Features

### 1. Monthly Calendar Grid
- 7-column month grid adapting seamlessly to 4, 5, and 6-week months.
- Clean weekday alignment with customizable first day of the week (System locale, Sunday, or Monday).
- Adjacent-month dates displayed in subdued tone; tapping an adjacent date selects it and smoothly navigates the view.
- Inverted monochrome selected-date indicator with subtle outline for "Today".
- Discrete event indicator dots under dates with scheduled items.

### 2. Selected-Date Agenda
- Chronological timeline directly beneath the calendar grid.
- Clear separation between all-day events and timed events.
- Calm "Nothing scheduled" empty state with a subtle quick-add action.

### 3. Focused Event Creation & Editing
- Required event title with validation.
- All-day toggle that cleanly manages date/time input.
- Start and end time pickers with end-after-start validation and 1-hour defaults.
- Reminder intervals: *At event time*, *5 min*, *10 min*, *15 min*, *30 min*, *1 hour*, *1 day before*, or *No reminder*.
- Recurrence engine: *Does not repeat*, *Daily*, *Weekly*, *Monthly* (with month-end clamping), and *Yearly* (with leap-day support).
- Unsaved changes confirmation protection.

### 4. Event Details & Series Management
- Quick overview with time badges, reminder status, and notes.
- Series-aware edit and delete actions with confirmation dialogs.

### 5. Settings & Appearance
- Appearance: *System*, *Light*, and *Dark* modes with high-contrast accessibility compliance.
- First day of the week preference: *System*, *Sunday*, *Monday*.
- Default reminder offset for new events.
- Real-time permission status for notifications and exact alarm timing.

---

## Technical Architecture

```
com.personal.calen
├── data
│   ├── CalenDatabase.kt       // Room Database (SQLite)
│   ├── Converters.kt          // TypeConverters for java.time and enums
│   ├── EventDao.kt            // Data Access Object
│   ├── EventEntity.kt         // Room Entity
│   ├── EventRepository.kt     // Repository pattern
│   └── SettingsRepository.kt  // Jetpack DataStore Preferences
├── domain
│   ├── MonthGridCalculator.kt // Pure Kotlin 7-column grid generator
│   └── RecurrenceCalculator.kt// Recurrence matching & date expansion
├── model
│   ├── CalendarDay.kt         // UI day cell model
│   ├── Event.kt               // Domain event model
│   ├── RecurrenceRule.kt      // Recurrence enum
│   ├── ReminderOption.kt      // Reminder offset enum
│   └── SettingsOptions.kt     // Theme & Week start enums
├── notifications
│   ├── BootAndConfigReceiver.kt   // Reboot & timezone change listener
│   ├── ReminderNotificationHelper.kt // Notification channel & builder
│   ├── ReminderReceiver.kt    // BroadcastReceiver for AlarmManager
│   └── ReminderScheduler.kt   // AlarmManager scheduling engine
└── ui
    ├── calendar
    │   ├── CalendarScreen.kt   // Month grid & agenda Composable
    │   └── CalendarViewModel.kt// UI state & navigation flow
    ├── event
    │   ├── EventDetailsDialog.kt
    │   ├── EventEditScreen.kt
    │   └── EventEditViewModel.kt
    ├── settings
    │   ├── SettingsScreen.kt
    │   └── SettingsViewModel.kt
    └── theme
        ├── Color.kt           // Strict 4-color monochrome palette
        ├── Theme.kt           // Light & Dark theme mappings
        └── Type.kt            // Cairo Regular wordmark & typography
```

---

## Requirements & Building

- **Android SDK:** `minSdk = 26` (Android 8.0+), `targetSdk = 36`, `compileSdk = 36`
- **JDK:** Java 17
- **Gradle:** 9.1 with Android Gradle Plugin 9.0.1 and Kotlin 2.1.0

### Build Debug APK
```bash
./gradlew assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

---

## License

Personal project — MIT License.
