package com.hospitalfinder.app.auth

/**
 * In-memory (NOT persisted) flag tracking whether the PIN/login gate has
 * already been passed during the current app process lifetime.
 *
 * This is what allows the user to background and return to the app
 * without re-entering their PIN every time, while still requiring PIN
 * entry on a genuinely fresh process start — matching the spec's
 * "OPEN APPLICATION → PIN ENTRY SCREEN" flow rather than gating on every
 * single onResume.
 */
object SessionState {
    var unlockedThisProcess: Boolean = false
}