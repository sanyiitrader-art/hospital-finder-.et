package com.hospitalfinder.app.ui.menu

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.hospitalfinder.app.databinding.ActivityMainBinding
import com.hospitalfinder.app.databinding.NavDrawerContentBinding

/**
 * Wires up the side menu/drawer: opening it from the hamburger button,
 * and handling taps on each menu item.
 *
 * The menu destinations (Settings, Rate App, Help, Privacy and Policy) have
 * no deeper functionality implemented yet per current scope — this class
 * only gives them a correct, real navigation/action structure so the
 * interface is not a fake screenshot.
 */
class DrawerController(
    private val activityBinding: ActivityMainBinding,
    private val drawerBinding: NavDrawerContentBinding,
    private val onSettingsClick: () -> Unit = {},
    private val onRateAppClick: () -> Unit = {},
    private val onHelpClick: () -> Unit = {},
    private val onPrivacyPolicyClick: () -> Unit = {}
) {

    fun setup() {
        activityBinding.btnMenu.setOnClickListener {
            openDrawer()
        }

        drawerBinding.itemSettings.setOnClickListener {
            closeDrawer()
            onSettingsClick()
        }
        drawerBinding.itemRateApp.setOnClickListener {
            closeDrawer()
            onRateAppClick()
        }
        drawerBinding.itemHelp.setOnClickListener {
            closeDrawer()
            onHelpClick()
        }
        drawerBinding.itemPrivacyPolicy.setOnClickListener {
            closeDrawer()
            onPrivacyPolicyClick()
        }
    }

    fun openDrawer() {
        activityBinding.drawerLayout.openDrawer(GravityCompat.END)
    }

    fun closeDrawer() {
        activityBinding.drawerLayout.closeDrawer(GravityCompat.END)
    }

    fun isDrawerOpen(): Boolean =
        activityBinding.drawerLayout.isDrawerOpen(GravityCompat.END)
}