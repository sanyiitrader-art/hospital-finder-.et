package com.hospitalfinder.app.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Persists the selected [ThemeMode] locally and applies it via
 * AppCompatDelegate's night-mode API — the standard Android mechanism
 * that controls which values/ vs values-night/ resource set is resolved.
 * This does not introduce a second theme system: values-night/ remains
 * the actual source of dark-theme colors, untouched by this class.
 *
 * Uses the same EncryptedSharedPreferences pattern already established
 * by LocalAuthRepository, but in its own file under a distinct key,
 * keeping each repository focused on one concern.
 */
class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getMode(): ThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, VALUE_SYSTEM)
        return when (stored) {
            VALUE_LIGHT -> ThemeMode.LIGHT
            VALUE_DARK -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM_DEFAULT
        }
    }

    /** Persists [mode] and immediately applies it so the change is visible without restarting. */
    fun setMode(mode: ThemeMode) {
        val value = when (mode) {
            ThemeMode.LIGHT -> VALUE_LIGHT
            ThemeMode.DARK -> VALUE_DARK
            ThemeMode.SYSTEM_DEFAULT -> VALUE_SYSTEM
        }
        prefs.edit().putString(KEY_THEME_MODE, value).apply()
        applyMode(mode)
    }

    /** Applies the currently persisted mode — called once at process start. */
    fun applyPersistedMode() {
        applyMode(getMode())
    }

    private fun applyMode(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM_DEFAULT -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    companion object {
        private const val PREFS_FILE_NAME = "hospitalfinder_theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val VALUE_LIGHT = "light"
        private const val VALUE_DARK = "dark"
        private const val VALUE_SYSTEM = "system"
    }
}