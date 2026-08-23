package com.hospitalfinder.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hospitalfinder.app.databinding.FragmentLoginBinding

/**
 * Combined username + phone-number account-entry form. Both fields are
 * required together (not alternative login methods). Also hosts the
 * visual-only "Continue with Google" button and the X (skip → guest)
 * action.
 *
 * No network calls are made anywhere in this class. Google sign-in is
 * not implemented — pressing it currently has no real OAuth effect,
 * per current feature scope.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            (activity as? LoginActivity)?.onGuestSelected()
        }

        binding.btnContinue.setOnClickListener {
            validateAndContinue()
        }

        // Visual-only per current scope — no real Google OAuth is wired up.
        binding.btnGoogle.setOnClickListener {
            (activity as? LoginActivity)?.onLoginFormCompleted()
        }
    }

    private fun validateAndContinue() {
        val username = binding.inputUsername.text?.toString()?.trim().orEmpty()
        val phone = binding.inputPhone.text?.toString()?.trim().orEmpty()

        val usernameMissing = username.isEmpty()
        val phoneMissing = phone.isEmpty()

        binding.txtUsernameError.visibility = if (phoneMissing && !usernameMissing) {
            // Only phone missing → error appears under the phone field, per spec.
            View.GONE
        } else if (usernameMissing) {
            View.VISIBLE
        } else {
            View.GONE
        }

        binding.txtPhoneError.visibility = if (phoneMissing) View.VISIBLE else View.GONE

        if (usernameMissing || phoneMissing) {
            return
        }

        // Both fields present — proceed to PIN screen (set mode).
        // Username/phone are handed off via the activity so PinFragment
        // doesn't need to know about this fragment's view state directly.
        (activity as? LoginActivity)?.let { loginActivity ->
            loginActivity.pendingUsername = username
            loginActivity.pendingPhone = phone
            loginActivity.onLoginFormCompleted()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = LoginFragment()
    }
}