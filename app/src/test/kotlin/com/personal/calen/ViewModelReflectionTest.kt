package com.personal.calen

import android.app.Application
import com.personal.calen.ui.calendar.CalendarViewModel
import com.personal.calen.ui.event.EventEditViewModel
import com.personal.calen.ui.settings.SettingsViewModel
import org.junit.Assert.assertNotNull
import org.junit.Test

class ViewModelReflectionTest {

    @Test
    fun testViewModelConstructorsForAndroidViewModelFactory() {
        // AndroidViewModelFactory requires a constructor with single Application argument
        val calConstructor = CalendarViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(calConstructor)

        val settingsConstructor = SettingsViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(settingsConstructor)

        val eventEditConstructor = EventEditViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(eventEditConstructor)
    }
}
