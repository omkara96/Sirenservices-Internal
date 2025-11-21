package com.omkara.sirenservices_internal.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omkara.sirenservices_internal.fragments.CompletedTripsFragment
import com.omkara.sirenservices_internal.fragments.OngoingTripsFragment

class TripPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OngoingTripsFragment()
            else -> CompletedTripsFragment()
        }
    }
}
