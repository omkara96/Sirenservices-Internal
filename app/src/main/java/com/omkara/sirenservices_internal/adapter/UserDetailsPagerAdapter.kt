package com.omkara.sirenservices_internal.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omkara.sirenservices_internal.fragments.UserAttendanceFragment
import com.omkara.sirenservices_internal.fragments.UserDocumentsFragment
import com.omkara.sirenservices_internal.fragments.UserProfileFragment
import com.omkara.sirenservices_internal.fragments.UserSalaryFragment

class UserDetailsPagerAdapter(
    activity: AppCompatActivity,
    private val userId: String
) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserProfileFragment.newInstance(userId)
            1 -> UserDocumentsFragment.newInstance(userId)
            2 -> UserSalaryFragment.newInstance(userId)
            3 -> UserAttendanceFragment.newInstance(userId)
//            4 -> UserTripsFragment.newInstance(userId)
            else -> Fragment()
        }
    }
}
