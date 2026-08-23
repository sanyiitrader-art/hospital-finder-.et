package com.hospitalfinder.app.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Local/demo implementation of [AuthRepository], backed by
 * EncryptedSharedPreferences. Stores username/phone and a salted hash of
 * the PIN — never the raw PIN. No server calls are made anywhere in this
 * class; it exists purely to demonstrate the login/PIN UI flow locally,
 * as required by the current feature scope.
 */
class LocalAuthRepository(context: Context) : AuthRepository {

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

    override fun getState(): AuthState {
        val stateFlag = prefs.getString(KEY_STATE, STATE_NOT_STARTED)
        return when (stateFlag) {
            STATE_GUEST -> AuthState.Guest
            STATE_AUTHENTICATED -> {
                val username = prefs.getString(KEY_USERNAME, "") ?: ""
                val phone = prefs.getString(KEY_PHONE, "") ?: ""
                AuthState.Authenticated(username, phone)
            }
            else -> AuthState.NotStarted
        }
    }

    override fun registerAccount(username: String, phone: String, pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)

        prefs.edit()
            .putString(KEY_STATE, STATE_AUTHENTICATED)
            .putString(KEY_USERNAME, username)
            .putString(KEY_PHONE, phone)
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    override fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return hashPin(pin, salt) == storedHash
    }

    override fun setGuest() {
        // Only downgrade to guest if no account has been established yet;
        // an existing authenticated account is never silently cleared.
        if (getState() is AuthState.NotStarted) {
            prefs.edit().putString(KEY_STATE, STATE_GUEST).apply()
        }
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    companion object {
        private const val PREFS_FILE_NAME = "hospitalfinder_auth_prefs"
        private const val KEY_STATE = "state"
        private const val KEY_USERNAME = "username"
        private const val KEY_PHONE = "phone"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"

        private const val STATE_NOT_STARTED = "not_started"
        private const val STATE_GUEST = "guest"
        private const val STATE_AUTHENTICATED = "authenticated"
    }
}