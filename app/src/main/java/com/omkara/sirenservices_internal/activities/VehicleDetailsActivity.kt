package com.omkara.sirenservices_internal.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.VehicleDetailPagerAdapter

class VehicleDetailsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvVehNumber: TextView
    private lateinit var tvVehMakeModel: TextView
    private lateinit var chipStatus: Chip
    private lateinit var btnChangeStatus: MaterialButton

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private lateinit var adapter: VehicleDetailPagerAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private var vehicleId = ""

    private val statusOptions = arrayOf(
        "ACTIVE", "INACTIVE", "UNDER_MAINTENANCE"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_details)

        vehicleId = intent.getStringExtra("vehicle_id") ?: ""

        bindViews()
        setupToolbar()
        setupViewPager()
        loadVehicleDetails()

        btnChangeStatus.setOnClickListener { showStatusDialog() }
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        tvVehNumber = findViewById(R.id.tvVehNumber)
        tvVehMakeModel = findViewById(R.id.tvVehMakeModel)
        chipStatus = findViewById(R.id.chipStatus)
        btnChangeStatus = findViewById(R.id.btnChangeStatus)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupViewPager() {
        adapter = VehicleDetailPagerAdapter(this, vehicleId)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Info"
                1 -> "Compliance"
                2 -> "Service"
                3 -> "Revenue"
                4 -> "Documents"
                else -> "Tab"
            }
        }.attach()
    }

    private fun loadVehicleDetails() {
        firestore.collection("vehicles")
            .document(vehicleId)
            .get()
            .addOnSuccessListener { doc ->

                val info = doc.get("vehicle_info") as? Map<String, Any> ?: emptyMap()
                val number = info["vehicle_number"]?.toString() ?: ""
                val make = info["make"]?: ""
                val model = info["model"]  ?: ""
                val status = info["status"].toString() ?: "ACTIVE"

                tvVehNumber.text = number
                tvVehMakeModel.text = "$make • $model"

                setStatusChip(status)
            }
    }

    private fun setStatusChip(status: String) {
        chipStatus.text = status

        val color = when (status) {
            "ACTIVE" -> getColor(R.color.status_active)
            "INACTIVE" -> getColor(R.color.status_inactive)
            "UNDER_MAINTENANCE" -> getColor(R.color.status_maintenance)
            else -> getColor(R.color.status_inactive)
        }

        chipStatus.setChipBackgroundColor(ColorStateList.valueOf(color))
        chipStatus.setTextColor(Color.WHITE)
    }


    private fun showStatusDialog() {
        AlertDialog.Builder(this)
            .setTitle("Change Vehicle Status")
            .setItems(statusOptions) { _, index ->
                updateStatus(statusOptions[index])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateStatus(newStatus: String) {
        firestore.collection("vehicles")
            .document(vehicleId)
            .update("vehicle_info.status", newStatus)
            .addOnSuccessListener {
                setStatusChip(newStatus)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

}
