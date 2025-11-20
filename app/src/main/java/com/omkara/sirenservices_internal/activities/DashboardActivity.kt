package com.omkara.sirenservices_internal.activities


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.widget.Toast
import com.omkara.sirenservices_internal.loginsignup.UserRegistation

class DashboardActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalVehicles: TextView
    private lateinit var tvActiveVehicles: TextView
    private lateinit var tvThirdPartyVehicles: TextView

    private lateinit var btnCreateUser: MaterialButton
    private lateinit var btnAddVehicle: MaterialButton
    private lateinit var btnManageVehicles: MaterialButton
    private lateinit var btnServiceRecords: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        setListeners()
        loadStats()
    }

    private fun initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalVehicles = findViewById(R.id.tvTotalVehicles)
        tvActiveVehicles = findViewById(R.id.tvActiveVehicles)
        tvThirdPartyVehicles = findViewById(R.id.tvThirdPartyVehicles)

        btnCreateUser = findViewById(R.id.btnCreateUser)
        btnAddVehicle = findViewById(R.id.btnAddVehicle)
        btnManageVehicles = findViewById(R.id.btnManageVehicles)
        btnServiceRecords = findViewById(R.id.btnServiceRecords)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setListeners() {
        btnCreateUser.setOnClickListener {
            startActivity(Intent(this, UserRegistation::class.java))
        }

        btnAddVehicle.setOnClickListener {
            startActivity(Intent(this, VehicleRegistrationActivity::class.java))
        }

        btnManageVehicles.setOnClickListener {
            startActivity(Intent(this, VehicleListActivity::class.java))
        }

        btnServiceRecords.setOnClickListener {
            startActivity(Intent(this, TripCreateActivity::class.java))
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadStats() {
        // Users count
        db.collection("users").get().addOnSuccessListener {
            tvTotalUsers.text = "Users: ${it.size()}"
        }

        // Total vehicles
        db.collection("vehicles").get().addOnSuccessListener {
            tvTotalVehicles.text = "Vehicles: ${it.size()}"
        }

        // Active vehicles
        db.collection("vehicles")
            .whereEqualTo("status", "ACTIVE")
            .get().addOnSuccessListener {
                tvActiveVehicles.text = "Active Vehicles: ${it.size()}"
            }

        // Third-party vehicles
        db.collection("vehicles")
            .whereEqualTo("owner_type", "THIRD_PARTY")
            .get().addOnSuccessListener {
                tvThirdPartyVehicles.text = "3rd Party Vehicles: ${it.size()}"
            }
    }
}
