package com.omkara.sirenservices_internal.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.loginsignup.UserRegistation
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import com.omkara.sirenservices_internal.models.TripModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private val TAG = "DashboardActivity"

    private lateinit var drawer: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    // User UI
    private lateinit var tvUsersTotal: com.google.android.material.textview.MaterialTextView
    private lateinit var tvUsersActive: com.google.android.material.textview.MaterialTextView
    private lateinit var tvUsersOccupied: com.google.android.material.textview.MaterialTextView
    private lateinit var btnViewUsers: MaterialButton

    // Vehicle UI
    private lateinit var tvVehiclesTotal: com.google.android.material.textview.MaterialTextView
    private lateinit var tvVehiclesActive: com.google.android.material.textview.MaterialTextView
    private lateinit var tvVehiclesMaint: com.google.android.material.textview.MaterialTextView
    private lateinit var btnViewVehicles: MaterialButton

    // Trips UI - Today
    private lateinit var tvTodayRevenue: com.google.android.material.textview.MaterialTextView
    private lateinit var tvTodaySpend: com.google.android.material.textview.MaterialTextView
    private lateinit var tvTodayPending: com.google.android.material.textview.MaterialTextView
   // private lateinit var btnViewTrips: MaterialButton

    // Week
    private lateinit var tvWeekRevenue: com.google.android.material.textview.MaterialTextView
    private lateinit var tvWeekSpend: com.google.android.material.textview.MaterialTextView
    private lateinit var tvWeekBalance: com.google.android.material.textview.MaterialTextView

    // Month
    private lateinit var tvMonthRevenue: com.google.android.material.textview.MaterialTextView
    private lateinit var tvMonthSpend: com.google.android.material.textview.MaterialTextView
    private lateinit var tvMonthDailyAvg: com.google.android.material.textview.MaterialTextView

    // Year
    private lateinit var tvYearRevenue: com.google.android.material.textview.MaterialTextView
    private lateinit var tvYearSpend: com.google.android.material.textview.MaterialTextView
    private lateinit var tvYearDailyAvg: com.google.android.material.textview.MaterialTextView

    private val db = FirebaseFirestore.getInstance()

    private val nf: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // views
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
     //   btnViewTrips = findViewById(R.id.btnViewTrips)

        tvWeekRevenue = findViewById(R.id.tvWeekRevenue)
        tvWeekSpend = findViewById(R.id.tvWeekSpend)
        tvWeekBalance = findViewById(R.id.tvWeekBalance)

        tvMonthRevenue = findViewById(R.id.tvMonthRevenue)
        tvMonthSpend = findViewById(R.id.tvMonthSpend)
        tvMonthDailyAvg = findViewById(R.id.tvMonthDailyAvg)

        tvYearRevenue = findViewById(R.id.tvYearRevenue)
        tvYearSpend = findViewById(R.id.tvYearSpend)
        tvYearDailyAvg = findViewById(R.id.tvYearDailyAvg)

        setupToolbar()
        setupListeners()

        // initial load
        swipeRefresh.isRefreshing = true
        loadAllStats()
    }

    private fun setupToolbar() {
        // Open drawer on nav icon click
        toolbar.setNavigationOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener {
            loadAllStats()
        }

        btnViewUsers.setOnClickListener {
            // navigate to users list
            startActivity(Intent(this, com.omkara.sirenservices_internal.activities.ListUserActivity::class.java))
        }

        btnViewVehicles.setOnClickListener {
            startActivity(Intent(this, com.omkara.sirenservices_internal.activities.VehicleListActivity::class.java))
        }

//        btnViewTrips.setOnClickListener {
//            startActivity(Intent(this, com.omkara.sirenservices_internal.activities.TripListActivity::class.java))
//        }

        // nav item clicks (optional)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_users -> startActivity(Intent(this, com.omkara.sirenservices_internal.activities.ListUserActivity::class.java))
                R.id.nav_vehicles -> startActivity(Intent(this, com.omkara.sirenservices_internal.activities.VehicleListActivity::class.java))
                R.id.nav_trips -> startActivity(Intent(this, com.omkara.sirenservices_internal.activities.TripListActivity::class.java))
                R.id.nav_billing -> startActivity(Intent(this, com.omkara.sirenservices_internal.loginsignup.UserRegistation::class.java))
                else -> { /* handle other navs */ }
            }
            drawer.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun loadAllStats() {
        swipeRefresh.isRefreshing = true

        loadUserStats()
        loadVehicleStats()
        // load trip stats for different windows
        loadTripStatsForRange(getStartOfDay(), getEndOfDay())   // today
        loadTripStatsForRange(getStartOfWeek(), getEndOfDay(), resultCallback = ::applyWeekStats)
        loadTripStatsForRange(getStartOfMonth(), getEndOfDay(), resultCallback = ::applyMonthStats)
        loadTripStatsForRange(getStartOfYear(), getEndOfDay(), resultCallback = ::applyYearStats)
        // ensure spinner off after last call — we will clear it when today query completes
    }

    private fun loadUserStats() {
        db.collection("users").get()
            .addOnSuccessListener { snap ->
                val total = snap.size()
                tvUsersTotal.text = "Total: $total"

                // active / occupied counts
                val active = snap.documents.count { it.getString("status")?.equals("Active", true) == true }
                val occupied = snap.documents.count { it.getString("status")?.equals("Occupied", true) == true }
                tvUsersActive.text = "Active: $active"
                tvUsersOccupied.text = "Occupied: $occupied"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "loadUserStats failed", e)
                Toast.makeText(this, "Failed loading user stats: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadVehicleStats() {
        db.collection("vehicles").get()
            .addOnSuccessListener { snap ->
                val total = snap.size()
                tvVehiclesTotal.text = "Total: $total"

                val available = snap.documents.count { it.getString("status")?.equals("AVAILABLE", true) == true || it.getString("status")?.equals("ACTIVE", true) == true }
                val maintenance = snap.documents.count { it.getString("status")?.equals("MAINTENANCE", true) == true || it.getString("status")?.equals("MAINTENANCE_REQUESTED", true) == true }
                tvVehiclesActive.text = "Available: $available"
                tvVehiclesMaint.text = "Maintenance: $maintenance"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "loadVehicleStats failed", e)
            }
    }

    /**
     * Query trips in [start, end] (created_at timestamps) and aggregate numeric fields.
     * If resultCallback provided, it will be called with the aggregated map, otherwise assumes 'today' use.
     */
    private fun loadTripStatsForRange(
        start: Date,
        end: Date,
        resultCallback: ((TripAgg) -> Unit)? = null
    ) {
        val startTs = Timestamp(start)
        val endTs = Timestamp(end)

        db.collection("trips")
            .whereGreaterThanOrEqualTo("created_at", startTs)
            .whereLessThanOrEqualTo("created_at", endTs)
            .get()
            .addOnSuccessListener { snap ->
                // aggregate
                var revenue = 0.0
                var spend = 0.0
                var pending = 0.0
                var ongoingCount = 0
                var completedCount = 0

                for (doc in snap.documents) {
                    val t = doc.toObject(TripModel::class.java)
                    if (t != null) {
                        revenue += (t.trip_cost)
                        // spend = fuel + servicing + mechanic
                        spend += (t.fuel_cost + t.servicing_cost + t.mechanic_cost)
                        pending += (t.pending_amount)
                        when (t.status?.uppercase(Locale.getDefault())) {
                            "ONGOING" -> ongoingCount++
                            "ASSIGNED" -> ongoingCount++
                            "COMPLETED" -> completedCount++
                        }
                    } else {
                        // fallback: try numeric fields directly
                        revenue += (doc.getDouble("trip_cost") ?: 0.0)
                        spend += (doc.getDouble("fuel_cost") ?: 0.0)
                        spend += (doc.getDouble("servicing_cost") ?: 0.0)
                        spend += (doc.getDouble("mechanic_cost") ?: 0.0)
                        pending += (doc.getDouble("pending_amount") ?: 0.0)
                        val st = doc.getString("status")?.uppercase(Locale.getDefault())
                        if (st == "COMPLETED") completedCount++ else if (st == "ONGOING" || st == "ASSIGNED") ongoingCount++
                    }
                }

                val agg = TripAgg(
                    revenue = revenue,
                    spend = spend,
                    pending = pending,
                    ongoing = ongoingCount,
                    completed = completedCount,
                    count = snap.size()
                )

                if (resultCallback != null) {
                    resultCallback(agg)
                } else {
                    // default: treat as 'today' snapshot
                    applyTodayStats(agg)
                }

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "loadTripStatsForRange failed", e)
                if (resultCallback == null) swipeRefresh.isRefreshing = false
            }
    }

    // Called when today aggregation finishes
    private fun applyTodayStats(agg: TripAgg) {
        tvTodayRevenue.text = "Revenue: ${nf.format(agg.revenue)}"
        tvTodaySpend.text = "Spent: ${nf.format(agg.spend)}"
        tvTodayPending.text = "Pending: ${nf.format(agg.pending)}"

        // Stop spinner (we call this after today finishes; other ranges update independently)
        swipeRefresh.isRefreshing = false
    }

    private fun applyWeekStats(agg: TripAgg) {
        tvWeekRevenue.text = "Revenue: ${nf.format(agg.revenue)}"
        tvWeekSpend.text = "Spent: ${nf.format(agg.spend)}"
        val balance = agg.revenue - agg.spend
        tvWeekBalance.text = "Balance: ${nf.format(balance)}"
    }

    private fun applyMonthStats(agg: TripAgg) {
        tvMonthRevenue.text = "Revenue: ${nf.format(agg.revenue)}"
        tvMonthSpend.text = "Spent: ${nf.format(agg.spend)}"
        val days = daysBetween(getStartOfMonth(), getEndOfDay()).coerceAtLeast(1)
        tvMonthDailyAvg.text = "Daily Avg: ${nf.format(agg.revenue / days)}"
    }

    private fun applyYearStats(agg: TripAgg) {
        tvYearRevenue.text = "Revenue: ${nf.format(agg.revenue)}"
        tvYearSpend.text = "Spent: ${nf.format(agg.spend)}"
        val days = daysBetween(getStartOfYear(), getEndOfDay()).coerceAtLeast(1)
        tvYearDailyAvg.text = "Daily Avg: ${nf.format(agg.revenue / days)}"
    }

    // ---------- Time helpers ----------
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
        // set to start of week (you can change to Calendar.MONDAY if preferred)
        c.firstDayOfWeek = Calendar.MONDAY
        c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
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

    private fun daysBetween(start: Date, end: Date): Int {
        val diff = end.time - start.time
        return (diff / (1000L * 60 * 60 * 24)).toInt()
    }

    // small aggregation model
    data class TripAgg(
        val revenue: Double = 0.0,
        val spend: Double = 0.0,
        val pending: Double = 0.0,
        val ongoing: Int = 0,
        val completed: Int = 0,
        val count: Int = 0
    )
}
