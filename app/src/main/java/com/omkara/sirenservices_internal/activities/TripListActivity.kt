package com.omkara.sirenservices_internal.activities



import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapters.TripPagerAdapter
import com.google.android.material.appbar.MaterialToolbar

class TripListActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var pagerAdapter: TripPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarTriplist)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        viewPager = findViewById(R.id.viewPagerTrips)
        tabLayout = findViewById(R.id.tabLayoutTrips)

        pagerAdapter = TripPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        val titles = arrayOf("Ongoing", "Completed")
        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()
    }


}
