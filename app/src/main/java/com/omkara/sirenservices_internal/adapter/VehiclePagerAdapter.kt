package com.omkara.sirenservices_internal.adapter


import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omkara.sirenservices_internal.fragments.Cmpliance
import com.omkara.sirenservices_internal.fragments.ServiceInfo
import com.omkara.sirenservices_internal.fragments.VehicleInfoFragment

class VehiclePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private val vehicleFragment = VehicleInfoFragment()
    private val complianceFragment = Cmpliance()
    private val serviceFragment = ServiceInfo()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> vehicleFragment
            1 -> complianceFragment
            else -> serviceFragment
        }
    }

    fun getVehicleFragment(): VehicleInfoFragment = vehicleFragment
    fun getComplianceFragment(): Cmpliance = complianceFragment
    fun getServiceFragment(): ServiceInfo = serviceFragment
}
