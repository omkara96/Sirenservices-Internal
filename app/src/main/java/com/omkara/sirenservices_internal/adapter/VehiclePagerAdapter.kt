package com.omkara.sirenservices_internal.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omkara.sirenservices_internal.fragments.RegisterVehicleComplianceFragment
import com.omkara.sirenservices_internal.fragments.RegisterVehicleServiceNSaveFragment
import com.omkara.sirenservices_internal.fragments.RegisterVehicleInfoFragment

class VehiclePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val vehicleFragment = RegisterVehicleInfoFragment()
    private val complianceFragment = RegisterVehicleComplianceFragment()
    private val serviceFragment = RegisterVehicleServiceNSaveFragment()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> vehicleFragment
            1 -> complianceFragment
            2 -> serviceFragment
            else -> vehicleFragment
        }
    }

    fun getVehicleFragment() = vehicleFragment
    fun getComplianceFragment() = complianceFragment
    fun getServiceFragment() = serviceFragment

}
