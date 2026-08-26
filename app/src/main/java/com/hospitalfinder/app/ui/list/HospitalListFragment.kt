package com.hospitalfinder.app.ui.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hospitalfinder.app.MainActivity
import com.hospitalfinder.app.data.HospitalRepository
import com.hospitalfinder.app.databinding.FragmentHospitalListBinding
import com.hospitalfinder.app.ui.details.HospitalDetailsActivity

/**
 * Displays nearby hospitals as a scrollable list of cards.
 * This is one of the two representations of the same nearby-hospital
 * discovery feature (the other being [com.hospitalfinder.app.ui.map.HospitalMapFragment]).
 *
 * Tapping a card opens HospitalDetailsActivity via this fragment's own
 * ActivityResultLauncher, so that if the user taps the Map control on the
 * details screen, this fragment can ask MainActivity to switch tabs on
 * return — MainActivity's existing show/hide fragment state is reused
 * unchanged, so nothing is lost switching this way.
 */
class HospitalListFragment : Fragment() {

    private var _binding: FragmentHospitalListBinding? = null
    private val binding get() = _binding!!

    private val detailsLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val requestedTab = result.data?.getIntExtra(
                    HospitalDetailsActivity.EXTRA_RESULT_TAB, -1
                ) ?: -1
                if (requestedTab != -1) {
                    (activity as? MainActivity)?.switchToTab(requestedTab)
                }
            }
        }

    private val adapter = HospitalAdapter { hospital ->
        detailsLauncher.launch(HospitalDetailsActivity.newIntent(requireContext(), hospital.id))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHospitalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerHospitals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HospitalListFragment.adapter
            setHasFixedSize(true)
        }

        adapter.submitList(HospitalRepository.getNearbyHospitals())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerHospitals.adapter = null
        _binding = null
    }

    companion object {
        fun newInstance() = HospitalListFragment()
    }
}