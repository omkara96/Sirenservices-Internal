package com.omkara.sirenservices_internal.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.DashboardAlertAdapter
import com.omkara.sirenservices_internal.loginsignup.LoginActivity
import com.omkara.sirenservices_internal.loginsignup.UserRegistation
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.omkara.sirenservices_internal.models.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private val TAG = "DashboardActivity"
    private val ALERT_EXPIRY_DAYS = 7

    /* ---------------- UI ---------------- */
    private lateinit var drawer: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    // Users
    private lateinit var tvUsersTotal: MaterialTextView
    private lateinit var tvUsersActive: MaterialTextView
    private lateinit var tvUsersOccupied: MaterialTextView
    private lateinit var btnViewUsers: MaterialButton

    // Vehicles
    private lateinit var tvVehiclesTotal: MaterialTextView
    private lateinit var tvVehiclesActive: MaterialTextView
    private lateinit var tvVehiclesMaint: MaterialTextView
    private lateinit var btnViewVehicles: MaterialButton

    // Today
    private lateinit var tvTodayRevenue: MaterialTextView
    private lateinit var tvTodaySpend: MaterialTextView
    private lateinit var tvTodayPending: MaterialTextView

    // Week
    private lateinit var tvWeekRevenue: MaterialTextView
    private lateinit var tvWeekSpend: MaterialTextView
    private lateinit var tvWeekBalance: MaterialTextView

    // Month
    private lateinit var tvMonthRevenue: MaterialTextView
    private lateinit var tvMonthSpend: MaterialTextView
    private lateinit var tvMonthDailyAvg: MaterialTextView

    // Year
    private lateinit var tvYearRevenue: MaterialTextView
    private lateinit var tvYearSpend: MaterialTextView
    private lateinit var tvYearDailyAvg: MaterialTextView

    // Alerts
    private lateinit var rvAlerts: RecyclerView
    private lateinit var tvNoAlerts: MaterialTextView
    private lateinit var alertAdapter: DashboardAlertAdapter
    private val alertList = mutableListOf<DashboardAlert>()

    private val complianceDateFormat =
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())


    /* ---------------- Firebase ---------------- */
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val nf = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    /* ================= LIFECYCLE ================= */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        bindViews()
        setupToolbar()
        setupListeners()
        setupAlertsRecycler()

        swipeRefresh.isRefreshing = true
        loadAllStats()
    }

    /* ================= INIT ================= */

    private fun bindViews() {
        drawer = findViewById(R.id.drawer)
        navView = findViewById(R.id.navView)
        toolbar = findViewById(R.id.toolbarDashboard)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        tvUsersTotal = findViewById(R.id.tvUsersTotal)
        tvUsersActive = findViewById(R.id.tvUsersActive)
        tvUsersOccupied = findViewById(R.id.tvUsersOccupied)
        btnViewUsers = findViewById(R.id.btnViewUsers)

        tvVehiclesTotal = findViewById(R.id.tvVehiclesTotal)
        tvVehiclesActive = findViewById(R.id.tvVehiclesActive)
        tvVehiclesMaint = findViewById(R.id.tvVehiclesMaint)
        btnViewVehicles = findViewById(R.id.btnViewVehicles)

        tvTodayRevenue = findViewById(R.id.tvTodayRevenue)
        tvTodaySpend = findViewById(R.id.tvTodaySpend)
        tvTodayPending = findViewById(R.id.tvTodayPending)

        tvWeekRevenue = findViewById(R.id.tvWeekRevenue)
        tvWeekSpend = findViewById(R.id.tvWeekSpend)
        tvWeekBalance = findViewById(R.id.tvWeekBalance)

        tvMonthRevenue = findViewById(R.id.tvMonthRevenue)
        tvMonthSpend = findViewById(R.id.tvMonthSpend)
        tvMonthDailyAvg = findViewById(R.id.tvMonthDailyAvg)

        tvYearRevenue = findViewById(R.id.tvYearRevenue)
        tvYearSpend = findViewById(R.id.tvYearSpend)
        tvYearDailyAvg = findViewById(R.id.tvYearDailyAvg)

        rvAlerts = findViewById(R.id.rvDashboardAlerts)
        tvNoAlerts = findViewById(R.id.tvNoAlerts)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener { loadAllStats() }

        btnViewUsers.setOnClickListener {
            startActivity(Intent(this, ListUserActivity::class.java))
        }

        btnViewVehicles.setOnClickListener {
            startActivity(Intent(this, VehicleListActivity::class.java))
        }

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_add_users -> startActivity(Intent(this, UserRegistation::class.java))
                R.id.nav_add_vehicles -> startActivity(Intent(this, VehicleRegistrationActivity::class.java))
                R.id.nav_add_trip -> startActivity(Intent(this, TripCreateActivity::class.java))
                R.id.nav_manage_users -> startActivity(Intent(this, ListUserActivity::class.java))
                R.id.nav_manage_vehicles -> startActivity(Intent(this, VehicleListActivity::class.java))
                R.id.nav_manage_trips -> startActivity(Intent(this, TripListActivity::class.java))
                R.id.nav_logout -> logout()
            }
            drawer.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupAlertsRecycler() {
        rvAlerts.layoutManager = LinearLayoutManager(this)
        alertAdapter = DashboardAlertAdapter(alertList) { alert ->
            when (alert.type) {
                AlertType.BILL_PENDING -> startActivity(Intent(this, TripListActivity::class.java))
                AlertType.COMPLIANCE_EXPIRED,
                AlertType.COMPLIANCE_EXPIRING -> startActivity(Intent(this, VehicleListActivity::class.java))
            }
        }
        rvAlerts.adapter = alertAdapter
    }

    /* ================= LOAD DATA ================= */

    private fun loadAllStats() {
        alertList.clear()
        loadUserStats()
        loadVehicleStats()
        loadTripStatsForRange(getStartOfDay(), getEndOfDay())
        loadTripStatsForRange(getStartOfWeek(), getEndOfDay(), ::applyWeekStats)
        loadTripStatsForRange(getStartOfMonth(), getEndOfDay(), ::applyMonthStats)
        loadTripStatsForRange(getStartOfYear(), getEndOfDay(), ::applyYearStats)
        loadAlerts()
    }

    private fun loadAlerts() {
        loadComplianceAlerts()
        loadBillPendingAlerts()
    }

    /* ================= ALERTS ================= */

    private fun loadComplianceAlerts() {

        alertList.clear()

        db.collection("vehicles")
            .get()
            .addOnSuccessListener { snap ->

                val today = Calendar.getInstance().time

                for (doc in snap.documents) {

                    val vehicleId = doc.id
                    val vehicleNo = doc.getString("vehicle_number") ?: vehicleId

                    val compliance =
                        doc.toObject(com.omkara.sirenservices_internal.models.VehicleModel::class.java)
                            ?.compliance ?: continue

                    // ---------- Check all compliance sections ----------
                    checkComplianceSection(
                        title = "Insurance",
                        expiryStr = compliance.insurance?.current?.valid_till,
                        vehicleId = vehicleId,
                        vehicleNo = vehicleNo,
                        today = today
                    )

                    checkComplianceSection(
                        title = "PUC",
                        expiryStr = compliance.puc?.current?.valid_till,
                        vehicleId = vehicleId,
                        vehicleNo = vehicleNo,
                        today = today
                    )

                    checkComplianceSection(
                        title = "Permit",
                        expiryStr = compliance.permit?.current?.valid_till,
                        vehicleId = vehicleId,
                        vehicleNo = vehicleNo,
                        today = today
                    )

                    checkComplianceSection(
                        title = "Fitness",
                        expiryStr = compliance.fitness?.current?.valid_till,
                        vehicleId = vehicleId,
                        vehicleNo = vehicleNo,
                        today = today
                    )
                }

                updateAlertsUI()
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to load compliance alerts", it)
                updateAlertsUI()
            }
    }

    private fun checkComplianceSection(
        title: String,
        expiryStr: String?,
        vehicleId: String,
        vehicleNo: String,
        today: Date
    ) {
        val expiryDate = parseComplianceDate(expiryStr) ?: return

        val diffMillis = expiryDate.time - today.time
        val daysLeft = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

        when {
            daysLeft < 0 -> {
                alertList.add(
                    DashboardAlert(
                        type = AlertType.COMPLIANCE_EXPIRED,
                        title = "$title Expired",
                        description = "Vehicle $vehicleNo — expired on $expiryStr",
                        refId = vehicleId,
                        severity = 3
                    )
                )
            }

            daysLeft <= ALERT_EXPIRY_DAYS -> {
                alertList.add(
                    DashboardAlert(
                        type = AlertType.COMPLIANCE_EXPIRING,
                        title = "$title Expiring Soon",
                        description = "Vehicle $vehicleNo — expires in $daysLeft days ($expiryStr)",
                        refId = vehicleId,
                        severity = 2
                    )
                )
            }
        }
    }


    private fun loadBillPendingAlerts() {
        db.collection("trips")
            .whereEqualTo("status", "COMPLETED")
            .whereEqualTo("is_bill_generated", false)
            .get()
            .addOnSuccessListener { snap ->
                for (doc in snap.documents) {
                    alertList.add(
                        DashboardAlert(
                            type = AlertType.BILL_PENDING,
                            title = "Bill Pending",
                            description = "Bill not generated for trip ${doc.getString("trip_number")}",
                            refId = doc.id,        // trip_id
                            severity = 1           // LOW
                        )
                    )
                }
                updateAlertsUI()
            }
    }

    private fun updateAlertsUI() {
        tvNoAlerts.visibility = if (alertList.isEmpty()) MaterialTextView.VISIBLE else MaterialTextView.GONE
        alertAdapter.notifyDataSetChanged()
        swipeRefresh.isRefreshing = false
    }

    /* ================= USER / VEHICLE / TRIP STATS ================= */

    private fun loadUserStats() {
        db.collection("users").get().addOnSuccessListener {
            tvUsersTotal.text = "Total: ${it.size()}"
            tvUsersActive.text = "Active: ${it.count { d -> d.getString("status") == "Active" }}"
            tvUsersOccupied.text = "Occupied: ${it.count { d -> d.getString("status") == "Occupied" }}"
        }
    }

    private fun loadVehicleStats() {
        db.collection("vehicles").get().addOnSuccessListener {
            tvVehiclesTotal.text = "Total: ${it.size()}"
            tvVehiclesActive.text = "Available: ${it.count { d -> d.getString("status") == "ACTIVE" }}"
            tvVehiclesMaint.text = "Maintenance: ${it.count { d -> d.getString("status") == "MAINTENANCE" }}"
        }
    }

    private fun loadTripStatsForRange(
        start: Date,
        end: Date,
        cb: ((TripAgg) -> Unit)? = null
    ) {
        db.collection("trips")
            .whereGreaterThanOrEqualTo("created_at", Timestamp(start))
            .whereLessThanOrEqualTo("created_at", Timestamp(end))
            .get()
            .addOnSuccessListener { snap ->
                var revenue = 0.0
                var spend = 0.0
                var pending = 0.0

                snap.documents.forEach {
                    revenue += it.getDouble("trip_cost") ?: 0.0
                    spend += (it.getDouble("fuel_cost") ?: 0.0)
                    spend += (it.getDouble("servicing_cost") ?: 0.0)
                    spend += (it.getDouble("mechanic_cost") ?: 0.0)
                    pending += it.getDouble("pending_amount") ?: 0.0
                }

                val agg = TripAgg(revenue, spend, pending)
                if (cb != null) cb(agg) else applyTodayStats(agg)
            }
    }

    private fun applyTodayStats(a: TripAgg) {
        tvTodayRevenue.text = "Revenue: ${nf.format(a.revenue)}"
        tvTodaySpend.text = "Spent: ${nf.format(a.spend)}"
        tvTodayPending.text = "Pending: ${nf.format(a.pending)}"
    }

    private fun applyWeekStats(a: TripAgg) {
        tvWeekRevenue.text = "Revenue: ${nf.format(a.revenue)}"
        tvWeekSpend.text = "Spent: ${nf.format(a.spend)}"
        tvWeekBalance.text = "Balance: ${nf.format(a.revenue - a.spend)}"
    }

    private fun applyMonthStats(a: TripAgg) {
        tvMonthRevenue.text = "Revenue: ${nf.format(a.revenue)}"
        tvMonthSpend.text = "Spent: ${nf.format(a.spend)}"
        tvMonthDailyAvg.text = "Daily Avg: ${nf.format(a.revenue / 30)}"
    }

    private fun applyYearStats(a: TripAgg) {
        tvYearRevenue.text = "Revenue: ${nf.format(a.revenue)}"
        tvYearSpend.text = "Spent: ${nf.format(a.spend)}"
        tvYearDailyAvg.text = "Daily Avg: ${nf.format(a.revenue / 365)}"
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    data class TripAgg(
        val revenue: Double,
        val spend: Double,
        val pending: Double
    )

    /* ================= DATE HELPERS ================= */

    private fun getStartOfDay(): Date {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.time
    }

    private fun getEndOfDay(): Date {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.time
    }

    private fun getStartOfWeek(): Date {
        val c = Calendar.getInstance()
        c.firstDayOfWeek = Calendar.MONDAY
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.time
    }

    private fun getStartOfMonth(): Date {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.time
    }

    private fun getStartOfYear(): Date {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_YEAR, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.time
    }

    private fun parseComplianceDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null

        return try {
            complianceDateFormat.parse(dateStr)
        } catch (e: Exception) {
            Log.e("Dashboard", "Invalid compliance date: $dateStr", e)
            null
        }
    }


}
