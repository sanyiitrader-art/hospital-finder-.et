package com.hospitalfinder.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hospitalfinder.app.auth.LocalAuthRepository
import com.hospitalfinder.app.auth.SessionState
import com.hospitalfinder.app.databinding.ActivityMainBinding
import com.hospitalfinder.app.databinding.NavDrawerContentBinding
import com.hospitalfinder.app.ui.auth.LoginActivity
import com.hospitalfinder.app.ui.list.HospitalListFragment
import com.hospitalfinder.app.ui.map.HospitalMapFragment
import com.hospitalfinder.app.ui.menu.DrawerController
import com.hospitalfinder.app.util.setSelectedState

/**
 * Main patient-facing screen: hosts the List/Map fragments (switched via
 * the bottom controls) and the side drawer menu (opened via the top-right
 * hamburger button). Uses the fragment show/hide pattern so switching
 * views is instantaneous and each fragment's state is preserved.
 *
 * On a fresh process start (SessionState.unlockedThisProcess == false),
 * this activity first routes through LoginActivity (login/PIN gate)
 * before any of the existing UI below is set up. Once unlocked within
 * this process, returning to MainActivity (e.g. via back stack or
 * background/foreground) does not re-trigger the gate.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerController: DrawerController
    private val authRepository by lazy { LocalAuthRepository(applicationContext) }

    private var listFragment: HospitalListFragment? = null
    private var mapFragment: HospitalMapFragment? = null
    private var activeTab = TAB_LIST
    private var mainUiInitialized = false

    private val loginLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (SessionState.unlockedThisProcess) {
                initializeMainUiIfNeeded()
            } else {
                // The user backed out of the login/PIN flow without
                // completing it (neither authenticated nor guest) — there
                // is no valid state to show, so close the app rather than
                // leaving a blank MainActivity or looping the login screen.
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SessionState.unlockedThisProcess) {
            initializeMainUiIfNeeded()
        } else {
            loginLauncher.launch(LoginActivity.newIntent(this))
        }
    }

    private fun initializeMainUiIfNeeded() {
        if (mainUiInitialized) return
        mainUiInitialized = true

        setupFragments()
        setupBottomNav()
        setupDrawer()

        activeTab = TAB_LIST
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
            drawerBinding = drawerBinding,
            authRepository = authRepository,
            onGuestProfileClick = {
                loginLauncher.launch(LoginActivity.newIntentForceLogin(this))
            }
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