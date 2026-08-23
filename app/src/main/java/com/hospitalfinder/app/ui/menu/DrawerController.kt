package com.hospitalfinder.app.ui.menu

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.hospitalfinder.app.R
import com.hospitalfinder.app.auth.AuthRepository
import com.hospitalfinder.app.auth.AuthState
import com.hospitalfinder.app.databinding.ActivityMainBinding
import com.hospitalfinder.app.databinding.NavDrawerContentBinding

/**
 * Wires up the side menu/drawer: opening it from the hamburger button,
 * rendering the profile area according to the current local auth state,
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
    private val authRepository: AuthRepository,
    private val onGuestProfileClick: () -> Unit = {},
    private val onSettingsClick: () -> Unit = {},
    private val onRateAppClick: () -> Unit = {},
    private val onHelpClick: () -> Unit = {},
    private val onPrivacyPolicyClick: () -> Unit = {}
) {

    fun setup() {
        activityBinding.btnMenu.setOnClickListener {
            renderProfileArea()
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

        renderProfileArea()
    }

    /**
     * Updates the drawer's profile area to reflect the real local auth
     * state — a real username/phone when authenticated (not clickable),
     * or a clearly guest-labeled state that routes back to login when
     * the whole profile row is tapped.
     */
    private fun renderProfileArea() {
        when (val state = authRepository.getState()) {
            is AuthState.Authenticated -> {
                drawerBinding.txtUsername.text = state.username
                drawerBinding.txtEmail.text = state.phone
                drawerBinding.profileContainer.setOnClickListener(null)
                drawerBinding.profileContainer.isClickable = false
            }
            is AuthState.Guest, is AuthState.NotStarted -> {
                drawerBinding.txtUsername.setText(R.string.guest_username)
                drawerBinding.txtEmail.setText(R.string.guest_prompt)
                drawerBinding.profileContainer.isClickable = true
                drawerBinding.profileContainer.setOnClickListener {
                    closeDrawer()
                    onGuestProfileClick()
                }
            }
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