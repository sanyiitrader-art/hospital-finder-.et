package com.hospitalfinder.app.ui.details

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hospitalfinder.app.MainActivity
import com.hospitalfinder.app.R
import com.hospitalfinder.app.data.HospitalRepository
import com.hospitalfinder.app.databinding.ActivityHospitalDetailsBinding
import com.hospitalfinder.app.model.schedule.HospitalOperatingState
import com.hospitalfinder.app.model.schedule.OperatingStateCalculator
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Displays the selected hospital's current status, computed live from its
 * configured HospitalSchedule via OperatingStateCalculator.
 *
 * Leaving this screen always returns a result telling MainActivity which
 * tab to show and which hospital was being viewed. MainActivity uses that
 * to decide whether tapping the List tab later should show the plain list
 * (back arrow / this screen's own List button) or reopen this same
 * hospital's details (its Map button) — see MainActivity.pendingDetailsHospitalId.
 */
class HospitalDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHospitalDetailsBinding
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private var currentHospitalId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHospitalDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hospitalId = intent.getStringExtra(EXTRA_HOSPITAL_ID)
        val hospital = hospitalId?.let { HospitalRepository.getById(it) }

        if (hospital == null) {
            finish()
            return
        }
        currentHospitalId = hospital.id

        binding.btnBack.setOnClickListener { returnToMainActivity(MainActivity.TAB_LIST) }
        binding.btnMenu.visibility = View.INVISIBLE

        binding.btnNavList.setOnClickListener { returnToMainActivity(MainActivity.TAB_LIST) }
        binding.btnNavMap.setOnClickListener { returnToMainActivity(MainActivity.TAB_MAP) }

        binding.txtHospitalName.text = hospital.name

        val state = OperatingStateCalculator.calculate(hospital.schedule, LocalTime.now())
        renderState(state, hospital.schedule.lunchStart)
    }

    private fun returnToMainActivity(tab: Int) {
        val resultIntent = Intent()
            .putExtra(EXTRA_RESULT_TAB, tab)
            .putExtra(EXTRA_RESULT_HOSPITAL_ID, currentHospitalId)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun renderState(state: HospitalOperatingState, lunchStart: LocalTime?) {
        when (state) {
            is HospitalOperatingState.Open -> {
                setStatus(open = true)
                binding.txtMessage.text = getString(R.string.details_message_open)
                binding.txtTickets.visibility = View.VISIBLE
                binding.txtTickets.text = getString(R.string.details_tickets_left, state.ticketsLeft)
                binding.txtTimeRemaining.visibility = View.VISIBLE
                binding.txtTimeRemaining.text = getString(
                    R.string.details_time_remaining, formatDuration(state.timeUntilClose)
                )
                setLunchLine(lunchStart)
                setGetEnabled(true)
            }
            is HospitalOperatingState.TicketsFull -> {
                setStatus(open = true)
                binding.txtMessage.text = getString(R.string.details_message_tickets_full)
                binding.txtTickets.visibility = View.VISIBLE
                binding.txtTickets.text = getString(R.string.details_tickets_left, 0)
                binding.txtTimeRemaining.visibility = View.VISIBLE
                binding.txtTimeRemaining.text = getString(
                    R.string.details_time_remaining, formatDuration(state.timeUntilClose)
                )
                setLunchLine(lunchStart)
                setGetEnabled(false)
            }
            is HospitalOperatingState.OnLunchBreak -> {
                setStatus(open = true)
                binding.txtMessage.text = getString(
                    R.string.details_message_lunch, state.resumesAt.format(timeFormatter)
                )
                binding.txtTickets.visibility = View.GONE
                binding.txtTimeRemaining.visibility = View.GONE
                setLunchLine(lunchStart)
                setGetEnabled(false)
            }
            is HospitalOperatingState.Closed -> {
                setStatus(open = false)
                binding.txtMessage.text = getString(
                    R.string.details_message_closed, state.opensAt.format(timeFormatter)
                )
                binding.txtTickets.visibility = View.GONE
                binding.txtTimeRemaining.visibility = View.VISIBLE
                binding.txtTimeRemaining.text = getString(
                    R.string.details_time_until_open, formatDuration(state.timeUntilOpen)
                )
                setLunchLine(lunchStart)
                setGetEnabled(false)
            }
        }
    }

    private fun setStatus(open: Boolean) {
        val colorRes = if (open) R.color.status_open else R.color.status_closed
        val textRes = if (open) R.string.details_status_open else R.string.details_status_closed
        binding.txtStatus.text = getString(textRes)
        binding.txtStatus.setTextColor(getColor(colorRes))
        binding.statusDot.background.setTint(getColor(colorRes))
    }

    private fun setLunchLine(lunchStart: LocalTime?) {
        if (lunchStart == null) {
            binding.txtLunch.visibility = View.GONE
        } else {
            binding.txtLunch.visibility = View.VISIBLE
            binding.txtLunch.text = getString(R.string.details_lunch_line, lunchStart.format(timeFormatter))
        }
    }

    private fun setGetEnabled(enabled: Boolean) {
        binding.btnGet.isEnabled = enabled
        binding.btnGet.isClickable = enabled
        binding.btnGet.text = if (enabled) getString(R.string.btn_get) else getString(R.string.btn_get_unavailable)
        binding.btnGet.setBackgroundResource(
            if (enabled) R.drawable.bg_get_button else R.drawable.bg_get_button_disabled
        )
        binding.btnGet.setTextColor(
            if (enabled) getColor(android.R.color.white) else getColor(R.color.text_secondary)
        )
        if (enabled) {
            binding.btnGet.setOnClickListener { onGetPressed() }
        } else {
            binding.btnGet.setOnClickListener(null)
        }
    }

    private fun onGetPressed() {
        // Intentionally left as an integration point for the future
        // server-side atomic ticket reservation system.
    }

    private fun formatDuration(duration: java.time.Duration): String {
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        return when {
            hours > 0 && minutes > 0 -> "$hours h $minutes min"
            hours > 0 -> "$hours h"
            else -> "$minutes min"
        }
    }

    companion object {
        private const val EXTRA_HOSPITAL_ID = "extra_hospital_id"
        const val EXTRA_RESULT_TAB = "extra_result_tab"
        const val EXTRA_RESULT_HOSPITAL_ID = "extra_result_hospital_id"

        fun newIntent(context: Context, hospitalId: String): Intent =
            Intent(context, HospitalDetailsActivity::class.java)
                .putExtra(EXTRA_HOSPITAL_ID, hospitalId)
    }
}