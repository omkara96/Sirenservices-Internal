package com.omkara.sirenservices_internal.loginsignup

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.VehiclePagerAdapter
import com.omkara.sirenservices_internal.viewmodels.VehicleViewModel

class VehicleRegistrationActivity : AppCompatActivity() {

    private val vm: VehicleViewModel by viewModels()
    private lateinit var pager: ViewPager2
    private lateinit var tabs : TabLayout
    private val tabTitles = arrayOf("Basic Info", "Compliance", "Service n Save")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_registration)

        pager = findViewById(R.id.viewPager)
        val adapter = VehiclePagerAdapter(this)
        pager.adapter = adapter
        pager.offscreenPageLimit = 1
        pager.isUserInputEnabled = false

        tabs = findViewById<TabLayout>(R.id.tabLayout)

        TabLayoutMediator(tabs, pager) { tab, pos ->
            tab.text = tabTitles[pos]
        }.attach()

// ❌ Disable clicking on Tabs
        tabs.touchables.forEach { it.isEnabled = false }


        val tabs = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabs, pager) { tab, pos ->
            tab.text = tabTitles[pos]
        }.attach()

        // Optional: Disable swipe to enforce step-by-step wizard
        // pager.isUserInputEnabled = false
    }

    fun goToStep(index: Int) {
        pager.currentItem = index
    }

    fun nextStep() {
        pager.currentItem = pager.currentItem + 1
    }

    fun prevStep() {
        pager.currentItem = pager.currentItem - 1
    }
}
