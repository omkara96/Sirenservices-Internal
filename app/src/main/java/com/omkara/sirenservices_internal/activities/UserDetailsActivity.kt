package com.omkara.sirenservices_internal.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.omkara.sirenservices_internal.R
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.omkara.sirenservices_internal.viewmodels.UserDetailsViewModel
import com.omkara.sirenservices_internal.models.UserModel
import android.widget.ImageView
import android.widget.TextView
import com.omkara.sirenservices_internal.adapter.UserDetailsPagerAdapter

class UserDetailsActivity : AppCompatActivity() {

    private val vm: UserDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        val userId = intent.getStringExtra("USER_ID") ?: return

        val imgProfile = findViewById<ImageView>(R.id.userDetailsProfile)
        val txtName = findViewById<TextView>(R.id.userDetailsName)
        val txtRole = findViewById<TextView>(R.id.userDetailsRole)
        val txtStatus = findViewById<TextView>(R.id.userDetailsStatus)

        val viewPager = findViewById<ViewPager2>(R.id.viewPagerUserDetails)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutUserDetails)

        viewPager.adapter = UserDetailsPagerAdapter(this, userId)

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Profile"
                1 -> "Documents"
                2 -> "Salary"
                3 -> "Attendance"
                4 -> "Trips"
                else -> "Tab"
            }
        }.attach()

        vm.loadUser(userId)

        vm.user.observe(this) { user: UserModel? ->
            if (user == null) return@observe

            imgProfile.load(user.profilePhoto ?: "") {
                placeholder(R.drawable.ic_user)
                error(R.drawable.ic_user)
            }

            txtName.text = "${user.firstName} ${user.lastName ?: ""}"
            txtRole.text = "Role: ${user.role}"
            txtStatus.text = "Status: ${user.status}"
        }
    }
}
