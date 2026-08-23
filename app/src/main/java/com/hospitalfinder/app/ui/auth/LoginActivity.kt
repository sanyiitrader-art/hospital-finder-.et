package com.hospitalfinder.app.ui.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hospitalfinder.app.R
import com.hospitalfinder.app.auth.AuthState
import com.hospitalfinder.app.auth.LocalAuthRepository
import com.hospitalfinder.app.auth.SessionState

/**
 * Hosts the login and PIN screens and decides which one to show based on
 * the current local [AuthState]. Returns RESULT_OK once the user has
 * either completed authentication or explicitly chosen to continue as a
 * guest — at which point [SessionState.unlockedThisProcess] is set and
 * control returns to whoever launched this activity (normally
 * MainActivity, or DrawerController for a guest returning to sign in).
 */
class LoginActivity : AppCompatActivity() {

    private val authRepository by lazy { LocalAuthRepository(applicationContext) }

    /**
     * Holds the username/phone collected by LoginFragment while PinFragment
     * (mode = SET) is being filled in. Cleared once consumed in
     * [onPinCompleted]. This is the hand-off point between the two
     * fragments, mediated by their host activity rather than the
     * fragments referencing each other directly.
     */
    var pendingUsername: String? = null
    var pendingPhone: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (savedInstanceState == null) {
            showInitialScreen()
        }
    }

    private fun showInitialScreen() {
        val forceLogin = intent.getBooleanExtra(EXTRA_FORCE_LOGIN_SCREEN, false)

        val startFragment: Fragment = if (forceLogin) {
            LoginFragment.newInstance()
        } else {
            when (authRepository.getState()) {
                is AuthState.NotStarted -> LoginFragment.newInstance()
                is AuthState.Guest -> LoginFragment.newInstance()
                is AuthState.Authenticated -> PinFragment.newInstance(PinFragment.Mode.VERIFY)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.login_fragment_container, startFragment)
            .commit()
    }

    /** Called by LoginFragment once username+phone are validated and Continue is pressed. */
    fun onLoginFormCompleted() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.login_fragment_container, PinFragment.newInstance(PinFragment.Mode.SET))
            .commit()
    }

    /** Called by LoginFragment when the user presses X to skip login. */
    fun onGuestSelected() {
        authRepository.setGuest()
        finishUnlocked()
    }

    /**
     * Called by PinFragment once a new PIN has been set (new user, mode
     * SET) or verified (returning user, mode VERIFY). For SET mode,
     * [pendingUsername]/[pendingPhone] must have been populated by
     * LoginFragment beforehand; for VERIFY mode they are ignored.
     */
    fun onPinCompleted(pin: String) {
        val username = pendingUsername
        val phone = pendingPhone

        if (username != null && phone != null) {
            authRepository.registerAccount(username, phone, pin)
            pendingUsername = null
            pendingPhone = null
        }
        // VERIFY mode: PinFragment already confirmed the PIN matches via
        // authRepository.verifyPin(pin) before calling this, so no further
        // action is needed here beyond unlocking.

        finishUnlocked()
    }

    /** Exposed so PinFragment can validate a returning user's PIN. */
    fun verifyPin(pin: String): Boolean = authRepository.verifyPin(pin)

    private fun finishUnlocked() {
        SessionState.unlockedThisProcess = true
        setResult(Activity.RESULT_OK)
        finish()
    }

    companion object {
        private const val EXTRA_FORCE_LOGIN_SCREEN = "extra_force_login_screen"

        /** Normal entry point — respects existing local auth state. */
        fun newIntent(context: Context): Intent =
            Intent(context, LoginActivity::class.java)

        /**
         * Used when a guest taps the profile area in the drawer to return
         * to the login form, regardless of their current guest state.
         */
        fun newIntentForceLogin(context: Context): Intent =
            Intent(context, LoginActivity::class.java)
                .putExtra(EXTRA_FORCE_LOGIN_SCREEN, true)
    }
}