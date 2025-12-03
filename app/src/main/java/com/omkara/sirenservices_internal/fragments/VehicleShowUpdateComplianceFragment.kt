package com.omkara.sirenservices_internal.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.omkara.sirenservices_internal.R
import com.omkara.sirenservices_internal.activities.ComplianceHistoryActivity
import com.omkara.sirenservices_internal.adapter.VehicleShowUpdtComplianceSectionAdapter
import com.omkara.sirenservices_internal.models.ComplianceCurrent

class VehicleShowUpdateComplianceFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: VehicleShowUpdtComplianceSectionAdapter
    private var vehicleId: String = ""

    companion object {
        fun newInstance(vehicleId: String): VehicleShowUpdateComplianceFragment {
            val f = VehicleShowUpdateComplianceFragment()
            f.arguments = Bundle().apply { putString("vehicle_id", vehicleId) }
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

        val v = inflater.inflate(R.layout.fragment_vehicle_show_update_compliance, container, false)

        swipeRefresh = v.findViewById(R.id.swipeRefresh)
        recycler = v.findViewById(R.id.recyclerCompliance)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Start with empty list
        adapter = VehicleShowUpdtComplianceSectionAdapter(
            mutableListOf(),
            onEditClicked = { type, current -> openEditDialog(type, current) },
            onHistoryClicked = { type -> openComplianceHistory(type) }
        )

        recycler.adapter = adapter

        // Load initial data
        loadCompliance()

        // Enable swipe refresh
        swipeRefresh.setOnRefreshListener {
            loadCompliance()
        }

        return v
    }

    private fun loadCompliance() {
        swipeRefresh.isRefreshing = true

        FirebaseFirestore.getInstance()
            .collection("vehicles")
            .document(vehicleId)
            .get()
            .addOnSuccessListener { doc ->
                swipeRefresh.isRefreshing = false

                if (doc == null || !doc.exists()) return@addOnSuccessListener

                val root = doc.get("compliance") as? Map<String, Any> ?: emptyMap()

                val sections = mutableListOf<Triple<String, ComplianceCurrent?, List<ComplianceCurrent>>>()

                sections += extractSection("insurance", root)
                sections += extractSection("puc", root)
                sections += extractSection("permit", root)
                sections += extractSection("fitness", root)

                adapter.setItems(sections)

            }.addOnFailureListener {
                swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun extractSection(
        type: String,
        root: Map<String, Any>
    ): Triple<String, ComplianceCurrent?, List<ComplianceCurrent>> {

        val map = root[type] as? Map<String, Any> ?: return Triple(type, null, emptyList())

        val currentMap = map["current"] as? Map<String, Any>
        val historyList = map["history"] as? List<Map<String, Any>> ?: emptyList()

        val current = currentMap?.let { fromMap(it) }
        val history = historyList.map { fromMap(it) }

        return Triple(type, current, history)
    }

    private fun fromMap(map: Map<String, Any>): ComplianceCurrent {
        return ComplianceCurrent(
            provider = map["provider"] as? String,
            certificate_no = map["certificate_no"] as? String,
            number = map["number"] as? String,
            type = map["type"] as? String,
            premium = (map["premium"] as? Number)?.toDouble(),
            valid_from = map["valid_from"] as? String,
            valid_till = map["valid_till"] as? String,
            updated_at = map["updated_at"] as? Timestamp
        )
    }

    private fun openEditDialog(type: String, data: ComplianceCurrent?) {
        val dialog = EditComplianceDialog.newInstance(vehicleId, type, data)
        dialog.show(requireActivity().supportFragmentManager, "edit_dialog")

        // Refresh after dialog closes
        requireActivity().supportFragmentManager.setFragmentResultListener(
            "compliance_updated",
            this
        ) { _, _ ->
            loadCompliance()
        }
    }

    private fun openComplianceHistory(type: String) {
        val i = Intent(requireContext(), ComplianceHistoryActivity::class.java)
        i.putExtra("vehicle_id", vehicleId)
        i.putExtra("compliance_type", type)
        startActivity(i)
    }
}
