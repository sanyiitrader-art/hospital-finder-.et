package com.hospitalfinder.app.auth

/**
 * Abstraction over local account/PIN state.
 *
 * This is the intended swap point for a real backend later — nothing
 * outside this interface (fragments, activities, DrawerController) should
 * need to change when local/demo auth is replaced with real server-side
 * authentication and real phone/Google verification.
 */
interface AuthRepository {

    /** Current local account/session state. */
    fun getState(): AuthState

    /**
     * Registers a new local account and sets its access PIN. Used the
     * first time a user completes the login form + PIN screen.
     * Overwrites any previous local account state.
     */
    fun registerAccount(username: String, phone: String, pin: String)

    /**
     * Checks the supplied PIN against the previously established local
     * PIN for a returning authenticated user. Returns true on match.
     */
    fun verifyPin(pin: String): Boolean

    /** Marks the current session as guest (login skipped via X). */
    fun setGuest()
}