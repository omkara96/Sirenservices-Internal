package com.omkara.sirenservices_internal.adapter


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omkara.sirenservices_internal.fragments.*
import com.omkara.sirenservices_internal.fragments.details.BasicInfoFragment

class VehicleDetailPagerAdapter(fa: FragmentActivity, private val vehicleId: String)
    : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 5
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> BasicInfoFragment.newInstance(
                vehicleId
            )
//            1 -> ComplianceFragment.newInstance(vehicleId)
//            2 -> ServiceFragment.newInstance(vehicleId)
//            3 -> FinanceFragment.newInstance(vehicleId)
//            else -> DocumentsFragment.newInstance(vehicleId)
            else -> BasicInfoFragment.newInstance(
                vehicleId
            )
        }
    }
}
