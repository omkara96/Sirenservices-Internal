package com.omkara.sirenservices_internal.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.adapter.ServiceHistoryAdapter
import com.omkara.sirenservices_internal.models.ServiceRecord

class ServiceHistoryActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvTotalCost: TextView

    private lateinit var adapter: ServiceHistoryAdapter
    private val list = mutableListOf<ServiceRecord>()

    private val db = FirebaseFirestore.getInstance()
    private lateinit var vehicleId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_history)

        vehicleId = intent.getStringExtra("vehicle_id") ?: "" //Admieal@0306
        initViews()
        loadHistory()
    }

    private fun initViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rv = findViewById(R.id.rvServiceHistory)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvTotalCost = findViewById(R.id.tvTotalCost)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = ServiceHistoryAdapter(list)
        rv.adapter = adapter
    }

    private fun loadHistory() {
        db.collection("vehicles")
            .document(vehicleId)
            .collection("service_records")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->

                list.clear()

                var totalCost = 0.0

                for (d in snap) {
                    val record = d.toObject(ServiceRecord::class.java)
                    list.add(record)
                    totalCost += record.serviceCost
                }

                adapter.updateList(list)

                tvTotalCost.text = "Total Expense: ₹$totalCost"

                tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                tvEmpty.text = "Failed to load service history"
                tvEmpty.visibility = View.VISIBLE
            }
    }
}
