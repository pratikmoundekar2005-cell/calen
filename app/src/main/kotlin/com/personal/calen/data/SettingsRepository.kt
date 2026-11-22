package com.personal.calen.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.personal.calen.model.ThemeMode
import com.personal.calen.model.WeekStartOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calen_settings")

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val weekStart: Flow<WeekStartOption>
    val defaultReminderMinutes: Flow<Int?>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setWeekStart(option: WeekStartOption)
    suspend fun setDefaultReminderMinutes(minutes: Int?)
}

class DataStoreSettingsRepository(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WEEK_START = stringPreferencesKey("first_day_of_week")
        val DEFAULT_REMINDER = intPreferencesKey("default_reminder_minutes")
    }

    override val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.THEME_MODE]
        ThemeMode.fromName(raw)
    }

    override val weekStart: Flow<WeekStartOption> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.WEEK_START]
        WeekStartOption.fromName(raw)
    }

    override val defaultReminderMinutes: Flow<Int?> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.DEFAULT_REMINDER] ?: 0
        if (raw == -1) null else raw
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    override suspend fun setWeekStart(option: WeekStartOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEK_START] = option.name
        }
    }

    override suspend fun setDefaultReminderMinutes(minutes: Int?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REMINDER] = minutes ?: -1
        }
    }
}
