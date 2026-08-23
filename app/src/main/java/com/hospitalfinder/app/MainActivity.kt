package com.hospitalfinder.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hospitalfinder.app.databinding.ActivityMainBinding
import com.hospitalfinder.app.databinding.NavDrawerContentBinding
import com.hospitalfinder.app.ui.list.HospitalListFragment
import com.hospitalfinder.app.ui.map.HospitalMapFragment
import com.hospitalfinder.app.ui.menu.DrawerController
import com.hospitalfinder.app.util.setSelectedState

/**
 * Main patient-facing screen: hosts the List/Map fragments (switched via
 * the bottom controls) and the side drawer menu (opened via the top-right
 * hamburger button). Uses the fragment show/hide pattern so switching
 * views is instantaneous and each fragment's state is preserved.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerController: DrawerController

    private var listFragment: HospitalListFragment? = null
    private var mapFragment: HospitalMapFragment? = null
    private var activeTab = TAB_LIST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragments()
        setupBottomNav()
        setupDrawer()

        activeTab = savedInstanceState?.getInt(STATE_ACTIVE_TAB, TAB_LIST) ?: TAB_LIST
        applyTabSelection(activeTab)
    }

    private fun setupFragments() {
        val fm = supportFragmentManager

        listFragment = fm.findFragmentByTag(TAG_LIST) as? HospitalListFragment
            ?: HospitalListFragment.newInstance().also {
                fm.beginTransaction().add(R.id.fragment_container, it, TAG_LIST).commit()
            }

        mapFragment = fm.findFragmentByTag(TAG_MAP) as? HospitalMapFragment
            ?: HospitalMapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.fragment_container, it, TAG_MAP).commit()
            }
    }

    private fun setupBottomNav() {
        binding.btnNavList.setOnClickListener { applyTabSelection(TAB_LIST) }
        binding.btnNavMap.setOnClickListener { applyTabSelection(TAB_MAP) }
    }

    private fun setupDrawer() {
        val drawerBinding = NavDrawerContentBinding.bind(binding.navDrawerContent.root)

        drawerController = DrawerController(
            activityBinding = binding,
            drawerBinding = drawerBinding
        )
        drawerController.setup()
    }

    private fun applyTabSelection(tab: Int) {
        activeTab = tab

        val transaction = supportFragmentManager.beginTransaction()

        if (tab == TAB_LIST) {
            listFragment?.let { transaction.show(it) }
            mapFragment?.let { transaction.hide(it) }
        } else {
            mapFragment?.let { transaction.show(it) }
            listFragment?.let { transaction.hide(it) }
        }

        transaction.commitNowAllowingStateLoss()

        binding.btnNavList.setSelectedState(tab == TAB_LIST)
        binding.btnNavMap.setSelectedState(tab == TAB_MAP)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_ACTIVE_TAB, activeTab)
    }

    override fun onBackPressed() {
        if (::drawerController.isInitialized && drawerController.isDrawerOpen()) {
            drawerController.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG_LIST = "tag_hospital_list"
        private const val TAG_MAP = "tag_hospital_map"
        private const val TAB_LIST = 0
        private const val TAB_MAP = 1
        private const val STATE_ACTIVE_TAB = "state_active_tab"
    }
}