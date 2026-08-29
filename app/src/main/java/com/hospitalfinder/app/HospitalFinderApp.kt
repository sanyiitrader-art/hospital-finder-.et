package com.hospitalfinder.app

import android.app.Application
import com.hospitalfinder.app.theme.ThemePreferences

/**
 * Applies the persisted theme mode before any Activity is created, so the
 * correct light/dark appearance is active from the very first frame
 * rather than flashing the default theme and then switching.
 */
class HospitalFinderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ThemePreferences(this).applyPersistedMode()
    }
}