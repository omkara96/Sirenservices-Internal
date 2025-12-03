package com.omkara.sirenservices_internal.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.VehicleRevenueTripAdapter
import com.omkara.sirenservices_internal.models.TripRevenueModel

class VehicleShowRevenueFragment : Fragment() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var txtDaily: TextView
    private lateinit var txtMonthly: TextView
    private lateinit var txtYearly: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: VehicleRevenueTripAdapter

    private val db = FirebaseFirestore.getInstance()
    private var vehicleId = ""

    companion object {
        fun newInstance(id: String): VehicleShowRevenueFragment {
            val f = VehicleShowRevenueFragment()
            f.arguments = Bundle().apply { putString("vehicle_id", id) }
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicleId = arguments?.getString("vehicle_id") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val v = inflater.inflate(R.layout.fragment_vehicle_show_revenue, container, false)

        swipeRefresh = v.findViewById(R.id.swipeRefresh)
        txtDaily = v.findViewById(R.id.txtDailyRevenue)
        txtMonthly = v.findViewById(R.id.txtMonthlyRevenue)
        txtYearly = v.findViewById(R.id.txtYearlyRevenue)
        recycler = v.findViewById(R.id.recyclerTrips)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = VehicleRevenueTripAdapter(emptyList()) { trip ->
            Toast.makeText(requireContext(), "TODO: Open Trip ${trip.tripId}", Toast.LENGTH_SHORT).show()
        }
        recycler.adapter = adapter

        swipeRefresh.setOnRefreshListener { loadData() }

        loadData()

        return v
    }

    private fun loadData() {
        swipeRefresh.isRefreshing = true

        // 🔥 TODO AFTER TRIP DATA IS AVAILABLE
        // db.collection("vehicles").document(vehicleId).collection("trips")

        // --- TEMP SAMPLE DATA ---
        val sampleTrips = listOf(
            TripRevenueModel("T1001", 1500.0, 300.0),
            TripRevenueModel("T1000", 1200.0, 150.0),
            TripRevenueModel("T0999", 900.0, 100.0)
        )

        // --- Update UI ---
        calculateSummary(sampleTrips)
        adapter.update(sampleTrips)

        swipeRefresh.isRefreshing = false
    }

    private fun calculateSummary(list: List<TripRevenueModel>) {
        val today = list.sumOf { it.revenue - it.expenses } // TODO real filter
        val month = list.sumOf { it.revenue - it.expenses } // TODO real filter
        val year = list.sumOf { it.revenue - it.expenses }  // TODO real filter

        txtDaily.text = "Revenue: ₹$today | Expense: ₹0"
        txtMonthly.text = "Revenue: ₹$month | Expense: ₹0"
        txtYearly.text = "Revenue: ₹$year | Expense: ₹0"
    }
}
