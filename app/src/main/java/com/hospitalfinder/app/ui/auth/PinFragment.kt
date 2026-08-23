package com.hospitalfinder.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hospitalfinder.app.databinding.FragmentPinBinding

/**
 * Single PIN-entry screen used in two modes:
 *
 * - SET: a new user has just completed the login form; the PIN entered
 *   here becomes their local access PIN (handled by
 *   LoginActivity.onPinCompleted, which calls registerAccount).
 * - VERIFY: a returning user is unlocking the app; the PIN entered here
 *   is checked against the previously established PIN.
 *
 * There is no confirm/repeat field and no wrong-PIN lockout, attempt
 * counter, or recovery flow, per current feature scope.
 */
class PinFragment : Fragment() {

    enum class Mode { SET, VERIFY }

    private var _binding: FragmentPinBinding? = null
    private val binding get() = _binding!!

    private val mode: Mode by lazy {
        Mode.valueOf(requireArguments().getString(ARG_MODE, Mode.VERIFY.name))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinuePin.setOnClickListener {
            handleContinue()
        }
    }

    private fun handleContinue() {
        val pin = binding.inputPin.text?.toString()?.trim().orEmpty()

        if (pin.isEmpty()) {
            binding.txtPinError.visibility = View.VISIBLE
            return
        }
        binding.txtPinError.visibility = View.GONE

        val loginActivity = activity as? LoginActivity ?: return

        when (mode) {
            Mode.SET -> {
                // New PIN becomes the account's PIN — LoginActivity pairs
                // it with the pending username/phone collected earlier.
                loginActivity.onPinCompleted(pin)
            }
            Mode.VERIFY -> {
                if (loginActivity.verifyPin(pin)) {
                    loginActivity.onPinCompleted(pin)
                } else {
                    binding.txtPinError.text = getString(com.hospitalfinder.app.R.string.error_incorrect_pin)
                    binding.txtPinError.visibility = View.VISIBLE
                    binding.inputPin.text?.clear()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MODE = "arg_mode"

        fun newInstance(mode: Mode): PinFragment {
            val fragment = PinFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_MODE, mode.name)
            }
            return fragment
        }
    }
}