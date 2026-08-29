package com.hospitalfinder.app.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hospitalfinder.app.R
import com.hospitalfinder.app.databinding.ActivitySettingsBinding
import com.hospitalfinder.app.databinding.ItemSettingsRowBinding
import com.hospitalfinder.app.theme.ThemeMode
import com.hospitalfinder.app.theme.ThemePreferences

/**
 * Settings screen. THEME is the only functional control here — Change
 * PIN, Language, Notification, and Logout are visually present per the
 * reference design but structurally disabled: not clickable, not
 * focusable, no navigation, no fake functionality wired up.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val themePreferences by lazy { ThemePreferences(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        setupThemeSelector()
        setupDisabledRow(binding.rowChangePin, R.string.settings_change_pin, isDestructive = false)
        setupDisabledRow(binding.rowLanguage, R.string.settings_language, isDestructive = false)
        setupDisabledRow(binding.rowNotification, R.string.settings_notification, isDestructive = false)
        setupDisabledRow(binding.rowLogout, R.string.settings_logout, isDestructive = true)
    }

    private fun setupThemeSelector() {
        binding.optionThemeLight.setOnClickListener { selectTheme(ThemeMode.LIGHT) }
        binding.optionThemeDark.setOnClickListener { selectTheme(ThemeMode.DARK) }
        binding.optionThemeSystem.setOnClickListener { selectTheme(ThemeMode.SYSTEM_DEFAULT) }

        renderSelectedTheme(themePreferences.getMode())
    }

    private fun selectTheme(mode: ThemeMode) {
        themePreferences.setMode(mode)
        renderSelectedTheme(mode)
    }

    private fun renderSelectedTheme(mode: ThemeMode) {
        binding.optionThemeLight.setBackgroundResource(
            if (mode == ThemeMode.LIGHT) R.drawable.bg_theme_option_selected else 0
        )
        binding.optionThemeDark.setBackgroundResource(
            if (mode == ThemeMode.DARK) R.drawable.bg_theme_option_selected else 0
        )
        binding.optionThemeSystem.setBackgroundResource(
            if (mode == ThemeMode.SYSTEM_DEFAULT) R.drawable.bg_theme_option_selected else 0
        )
    }

    /**
     * Renders a visible-but-inert row: no click listener attached at all
     * (not just disabled styling), so there is no accidental tap-through
     * to any placeholder behavior. Logout is styled in red text per the
     * reference, matching image 4.
     */
    private fun setupDisabledRow(row: ItemSettingsRowBinding, labelRes: Int, isDestructive: Boolean) {
        row.txtRowLabel.text = getString(labelRes)
        row.txtRowLabel.contentDescription =
            "${getString(labelRes)}. ${getString(R.string.settings_row_unavailable_desc)}"
        row.txtRowLabel.isClickable = false
        row.txtRowLabel.isFocusable = false
        if (isDestructive) {
            row.txtRowLabel.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }
}