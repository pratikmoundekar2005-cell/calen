package com.personal.calen.ui.settings

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.personal.calen.data.DataStoreSettingsRepository
import com.personal.calen.data.SettingsRepository
import com.personal.calen.model.ReminderOption
import com.personal.calen.model.ThemeMode
import com.personal.calen.model.WeekStartOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository = DataStoreSettingsRepository(application)
) : AndroidViewModel(application) {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val weekStart: StateFlow<WeekStartOption> = settingsRepository.weekStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeekStartOption.SYSTEM)

    val defaultReminderMinutes: StateFlow<Int?> = settingsRepository.defaultReminderMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isNotificationPermissionGranted = MutableStateFlow(true)
    val isNotificationPermissionGranted: StateFlow<Boolean> = _isNotificationPermissionGranted.asStateFlow()

    private val _isExactAlarmPermitted = MutableStateFlow(true)
    val isExactAlarmPermitted: StateFlow<Boolean> = _isExactAlarmPermitted.asStateFlow()

    init {
        refreshPermissionStates()
    }

    fun refreshPermissionStates() {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            _isNotificationPermissionGranted.value = (status == PackageManager.PERMISSION_GRANTED)
        } else {
            _isNotificationPermissionGranted.value = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            _isExactAlarmPermitted.value = alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            _isExactAlarmPermitted.value = true
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setWeekStart(option: WeekStartOption) {
        viewModelScope.launch {
            settingsRepository.setWeekStart(option)
        }
    }

    fun setDefaultReminder(option: ReminderOption) {
        viewModelScope.launch {
            settingsRepository.setDefaultReminderMinutes(option.minutesBefore)
        }
    }
}
