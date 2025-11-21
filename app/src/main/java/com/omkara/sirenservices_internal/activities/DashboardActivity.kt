package com.omkara.sirenservices_internal.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.loginsignup.UserRegistation
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    // --- Existing System Stats ---
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalVehicles: TextView
    private lateinit var tvActiveVehicles: TextView
    private lateinit var tvThirdPartyVehicles: TextView

    // --- New Trip Stats ---
    private lateinit var tvOngoingTrips: TextView
    private lateinit var tvCompletedToday: TextView
    private lateinit var tvPendingPayments: TextView
    private lateinit var tvRevenueToday: TextView

    // --- Buttons ---
    private lateinit var btnCreateUser: MaterialButton
    private lateinit var btnAddVehicle: MaterialButton
    private lateinit var btnManageVehicles: MaterialButton
    private lateinit var btnServiceRecords: MaterialButton
    private lateinit var btnLogout: MaterialButton

    // NEW TRIP BUTTONS
    private lateinit var btnOngoingTrips: MaterialButton
    private lateinit var btnCompletedTrips: MaterialButton
    private lateinit var btnCreateTrip: MaterialButton
    private lateinit var btnTips: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        setListeners()
        loadStats()
    }

    private fun initViews() {

        // Existing stats
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalVehicles = findViewById(R.id.tvTotalVehicles)
        tvActiveVehicles = findViewById(R.id.tvActiveVehicles)
        tvThirdPartyVehicles = findViewById(R.id.tvThirdPartyVehicles)

        // Trip stats
        tvOngoingTrips = findViewById(R.id.tvOngoingTrips)
        tvCompletedToday = findViewById(R.id.tvCompletedToday)
        tvPendingPayments = findViewById(R.id.tvPendingPayments)
        tvRevenueToday = findViewById(R.id.tvRevenueToday)

        // Existing buttons
        btnCreateUser = findViewById(R.id.btnCreateUser)
        btnAddVehicle = findViewById(R.id.btnAddVehicle)
        btnManageVehicles = findViewById(R.id.btnManageVehicles)
        btnServiceRecords = findViewById(R.id.btnServiceRecords)
        btnLogout = findViewById(R.id.btnLogout)

        // NEW Trip buttons
        btnOngoingTrips = findViewById(R.id.btnOngoingTrips)
        btnCompletedTrips = findViewById(R.id.btnCompletedTrips)
        btnCreateTrip = findViewById(R.id.btnCreateTrip)
        btnTips = findViewById(R.id.btnTips)
    }

    private fun setListeners() {

        // Users
        btnCreateUser.setOnClickListener {
            startActivity(Intent(this, UserRegistation::class.java))
        }

        // Vehicle Create
        btnAddVehicle.setOnClickListener {
            startActivity(Intent(this, VehicleRegistrationActivity::class.java))
        }

        // Vehicle List
        btnManageVehicles.setOnClickListener {
            startActivity(Intent(this, VehicleListActivity::class.java))
        }

        // TEMP (Replace with Service Activity)
        btnServiceRecords.setOnClickListener {
            startActivity(Intent(this, TripCreateActivity::class.java))
        }

        // NEW: View Ongoing Trips
        btnOngoingTrips.setOnClickListener {
            val intent = Intent(this, TripListActivity::class.java)
            intent.putExtra("tab", "ongoing")
            startActivity(intent)
        }

        // NEW: View Completed Trips
        btnCompletedTrips.setOnClickListener {
            val intent = Intent(this, TripListActivity::class.java)
            intent.putExtra("tab", "completed")
            startActivity(intent)
        }

        // NEW: Create Trip
        btnCreateTrip.setOnClickListener {
            startActivity(Intent(this, TripCreateActivity::class.java))
        }

        // NEW: Tip Management
        btnTips.setOnClickListener {
            startActivity(Intent(this, ListUserActivity::class.java))
        }

        // Logout
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadStats() {

        val todayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

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

        // ============ TRIP STATISTICS ============

        // Ongoing Trips
        db.collection("trips")
            .whereIn("status", listOf("ASSIGNED", "ONGOING"))
            .get()
            .addOnSuccessListener {
                tvOngoingTrips.text = "Ongoing Trips: ${it.size()}"
            }

        // Completed Today
        db.collection("trips")
            .whereEqualTo("status", "COMPLETED")
            .whereEqualTo("trip_date", todayDate)
            .get()
            .addOnSuccessListener {
                tvCompletedToday.text = "Completed Today: ${it.size()}"
            }

        // Pending Payments
        db.collection("trips")
            .whereGreaterThan("pending_amount", 0)
            .get()
            .addOnSuccessListener {
                tvPendingPayments.text = "Pending Payments: ${it.size()}"
            }

        // Revenue Today
        db.collection("trips")
            .whereEqualTo("trip_date", todayDate)
            .get()
            .addOnSuccessListener { snap ->
                var total = 0.0
                for (doc in snap) {
                    val cost = doc.getDouble("trip_cost") ?: 0.0
                    total += cost
                }
                tvRevenueToday.text = "Revenue Today: ₹$total"
            }
    }
}
