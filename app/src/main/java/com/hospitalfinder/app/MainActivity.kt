package com.hospitalfinder.app

import android.app.Activity
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
import com.hospitalfinder.app.ui.details.HospitalDetailsActivity
import com.hospitalfinder.app.ui.list.HospitalListFragment
import com.hospitalfinder.app.ui.map.HospitalMapFragment
import com.hospitalfinder.app.ui.menu.DrawerController
import com.hospitalfinder.app.util.setSelectedState

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerController: DrawerController
    private val authRepository by lazy { LocalAuthRepository(applicationContext) }

    private var listFragment: HospitalListFragment? = null
    private var mapFragment: HospitalMapFragment? = null
    private var activeTab = TAB_LIST
    private var mainUiInitialized = false

    /**
     * Set when the user leaves a hospital-details screen via its Map
     * button, remembering which hospital they were viewing. If the user
     * then taps the List tab while this is set, they are taken back into
     * that same hospital's details screen instead of the plain list.
     * Cleared whenever the user leaves details via its back arrow or its
     * own List button — both of those are the explicit "show me the
     * plain list" action.
     */
    private var pendingDetailsHospitalId: String? = null

    private val loginLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (SessionState.unlockedThisProcess) {
                initializeMainUiIfNeeded()
            } else {
                finish()
            }
        }

    private val detailsLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult

            val requestedTab = data.getIntExtra(HospitalDetailsActivity.EXTRA_RESULT_TAB, -1)
            val hospitalId = data.getStringExtra(HospitalDetailsActivity.EXTRA_RESULT_HOSPITAL_ID)

            pendingDetailsHospitalId = if (requestedTab == TAB_MAP) hospitalId else null

            if (requestedTab != -1) {
                switchToTab(requestedTab)
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
        binding.btnNavList.setOnClickListener {
            val pendingId = pendingDetailsHospitalId
            if (pendingId != null) {
                pendingDetailsHospitalId = null
                openHospitalDetails(pendingId)
            } else {
                applyTabSelection(TAB_LIST)
            }
        }
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

    /**
     * Opens the hospital-details screen for [hospitalId] through this
     * activity's own launcher, so the result (which tab to show next, and
     * whether to remember a pending hospital) is always handled the same
     * way regardless of whether details was opened from the list fragment
     * or re-opened via [pendingDetailsHospitalId].
     */
    fun openHospitalDetails(hospitalId: String) {
        detailsLauncher.launch(HospitalDetailsActivity.newIntent(this, hospitalId))
    }

    /**
     * Deferred via post{} rather than applied synchronously: this runs
     * from an ActivityResultCallback, which can fire while FragmentManager
     * is still mid-dispatch from the activity resuming. A nested
     * commitNow at that exact moment throws "FragmentManager is already
     * executing transactions". Posting defers the switch to the next
     * frame, after that dispatch has settled.
     */
    private fun switchToTab(tab: Int) {
        if (!mainUiInitialized) return
        binding.root.post { applyTabSelection(tab) }
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
        const val TAB_LIST = 0
        const val TAB_MAP = 1
        private const val STATE_ACTIVE_TAB = "state_active_tab"
    }
}