package com.omkara.sirenservices_internal.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omkara.sirenservices_internal.fragments.Cmpliance
import com.omkara.sirenservices_internal.fragments.PhotosStepFragment
import com.omkara.sirenservices_internal.fragments.ReviewStepFragment
import com.omkara.sirenservices_internal.fragments.ServiceInfo
import com.omkara.sirenservices_internal.fragments.VehicleInfoFragment

class VehiclePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val vehicleFragment = VehicleInfoFragment()
    private val complianceFragment = Cmpliance()
    private val serviceFragment = ServiceInfo()
    private val photosFragment = PhotosStepFragment()
    private val reviewFragment = ReviewStepFragment()

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> vehicleFragment
            1 -> complianceFragment
            2 -> serviceFragment
            3 -> photosFragment
            4 -> reviewFragment
            else -> vehicleFragment
        }
    }

    fun getVehicleFragment() = vehicleFragment
    fun getComplianceFragment() = complianceFragment
    fun getServiceFragment() = serviceFragment
    fun getPhotosFragment() = photosFragment
    fun getReviewFragment() = reviewFragment
}
