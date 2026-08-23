package com.hospitalfinder.app.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hospitalfinder.app.data.HospitalRepository
import com.hospitalfinder.app.databinding.FragmentHospitalListBinding

/**
 * Displays nearby hospitals as a scrollable list of cards.
 * This is one of the two representations of the same nearby-hospital
 * discovery feature (the other being [com.hospitalfinder.app.ui.map.HospitalMapFragment]).
 */
class HospitalListFragment : Fragment() {

    private var _binding: FragmentHospitalListBinding? = null
    private val binding get() = _binding!!

    private val adapter = HospitalAdapter()

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
        // Avoid holding a reference to the RecyclerView/binding past the
        // fragment's view lifecycle to prevent memory leaks.
        binding.recyclerHospitals.adapter = null
        _binding = null
    }

    companion object {
        fun newInstance() = HospitalListFragment()
    }
}