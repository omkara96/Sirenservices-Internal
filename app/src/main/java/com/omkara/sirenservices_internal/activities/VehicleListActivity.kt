package com.omkara.sirenservices_internal.activities

import com.omkara.sirenservices_internal.adapter.VehicleAdapter
import com.omkara.sirenservices_internal.adapter.VehicleItem
import com.omkara.sirenservices_internal.loginsignup.VehicleRegistrationActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.omkara.sirenservices_internal.R
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class VehicleListActivity : AppCompatActivity() {

    private lateinit var edtSearch: EditText
    private lateinit var spFilterStatus: Spinner
    private lateinit var btnClearFilter: Button
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rvVehicles: RecyclerView
    private lateinit var fabAdd: View
    private lateinit var tvEmpty: TextView

    private lateinit var adapter: VehicleAdapter
    private val allVehicles = ArrayList<VehicleItem>()
    private val filteredVehicles = ArrayList<VehicleItem>()

    private val firestore = FirebaseFirestore.getInstance()
    private var vehiclesListener: ListenerRegistration? = null

    private lateinit var progressDialog: AlertDialog

    private val statusOptions = listOf("All", "ACTIVE", "UNDER_MAINTENANCE", "DECOMMISSIONED")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_list)

        edtSearch = findViewById(R.id.edtSearch)
        spFilterStatus = findViewById(R.id.spFilterStatus)
        btnClearFilter = findViewById(R.id.btnClearFilter)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        rvVehicles = findViewById(R.id.rvVehicles)
        fabAdd = findViewById(R.id.fabAdd)
        tvEmpty = findViewById(R.id.tvEmpty)

        progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_progress)
            .setCancelable(false)
            .create()

        adapter = VehicleAdapter(filteredVehicles) { item ->
            val intent = Intent(this, VehicleDetailsActivity::class.java)
            intent.putExtra("vehicle_id", item.id)
            startActivity(intent)
        }

        rvVehicles.layoutManager = LinearLayoutManager(this)
        rvVehicles.adapter = adapter

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFilterStatus.adapter = spinnerAdapter

        (fabAdd as View).setOnClickListener {
            startActivity(Intent(this, VehicleRegistrationActivity::class.java))
        }

        swipeRefresh.setOnRefreshListener { refreshVehicles() }

        btnClearFilter.setOnClickListener {
            spFilterStatus.setSelection(0)
            edtSearch.setText("")
            applyFilters()
        }

        edtSearch.setOnEditorActionListener { _, _, _ ->
            applyFilters()
            true
        }

        spFilterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        startListeningVehicles()
    }

    override fun onDestroy() {
        vehiclesListener?.remove()
        super.onDestroy()
    }

    private fun startListeningVehicles() {
        progressDialog.show()
        vehiclesListener?.remove()
        vehiclesListener = firestore.collection("vehicles")
            .orderBy("created_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                progressDialog.dismiss()
                swipeRefresh.isRefreshing = false

                if (err != null) {
                    Toast.makeText(this, "Error loading vehicles: ${err.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                allVehicles.clear()
                if (snap != null && !snap.isEmpty) {
                    for (doc in snap.documents) {
                        val id = doc.id
                        val vehicleNumber = doc.getString("vehicle_number") ?: ""
                        val make = doc.getString("make") ?: ""
                        val model = doc.getString("model") ?: ""
                        val odometer = doc.getDouble("last_service_odometer") ?: doc.getDouble("odometer_at_registration")
                        val status = doc.getString("status") ?: "ACTIVE"

                        allVehicles.add(VehicleItem(id, vehicleNumber, make, model, odometer, status))
                    }
                }

                applyFilters()
            }
    }

    private fun refreshVehicles() {
        swipeRefresh.isRefreshing = true
        startListeningVehicles()
    }

    private fun applyFilters() {
        val query = edtSearch.text.toString().trim().lowercase()
        val statusFilter = spFilterStatus.selectedItem as String

        filteredVehicles.clear()
        for (v in allVehicles) {
            val matchesSearch = query.isEmpty() ||
                    v.vehicleNumber.lowercase().contains(query) ||
                    v.make.lowercase().contains(query) ||
                    v.model.lowercase().contains(query)

            val matchesStatus = (statusFilter == "All") || (v.status == statusFilter)

            if (matchesSearch && matchesStatus) filteredVehicles.add(v)
        }

        adapter.updateList(filteredVehicles)
        tvEmpty.visibility = if (filteredVehicles.isEmpty()) View.VISIBLE else View.GONE
    }
}
