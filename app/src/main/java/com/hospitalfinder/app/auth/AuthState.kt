package com.hospitalfinder.app.auth

/**
 * Represents the three possible local account/session states for this
 * device, per the current local/demo authentication scope. There is no
 * server-side account model yet — this is purely local state.
 */
sealed class AuthState {

    /** No account/session has ever been established on this device. */
    object NotStarted : AuthState()

    /** The user skipped account entry (pressed X) and is browsing as a guest. */
    object Guest : AuthState()

    /**
     * A local account/session has been established on this device
     * (username + phone + PIN all recorded locally).
     */
    data class Authenticated(
        val username: String,
        val phone: String
    ) : AuthState()
}