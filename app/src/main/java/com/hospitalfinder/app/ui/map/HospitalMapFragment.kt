package com.hospitalfinder.app.ui.map

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hospitalfinder.app.data.HospitalRepository
import com.hospitalfinder.app.databinding.FragmentHospitalMapBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

/**
 * Displays nearby hospitals as markers on a real interactive map.
 * This is one of the two representations of the same nearby-hospital
 * discovery feature (the other being [com.hospitalfinder.app.ui.list.HospitalListFragment]).
 *
 * Uses osmdroid (OpenStreetMap) rather than Google Maps to avoid a Play
 * Services dependency, keeping the app lighter and usable on more devices.
 */
class HospitalMapFragment : Fragment() {

    private var _binding: FragmentHospitalMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // osmdroid requires configuration (user agent, cache paths) before
        // any MapView is created.
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        Configuration.getInstance().userAgentValue = requireContext().packageName

        _binding = FragmentHospitalMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
        }

        val hospitals = HospitalRepository.getNearbyHospitals()

        val centerPoint = if (hospitals.isNotEmpty()) {
            GeoPoint(hospitals.first().latitude, hospitals.first().longitude)
        } else {
            GeoPoint(9.03, 38.74)
        }
        binding.mapView.controller.setCenter(centerPoint)

        hospitals.forEach { hospital ->
            val marker = Marker(binding.mapView)
            marker.position = GeoPoint(hospital.latitude, hospital.longitude)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = hospital.name
            binding.mapView.overlays.add(marker)
        }

        binding.mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        // Detach overlays and release the map's tile cache references to
        // avoid leaking memory when the fragment's view is destroyed.
        binding.mapView.overlays.clear()
        binding.mapView.onDetach()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HospitalMapFragment()
    }
}